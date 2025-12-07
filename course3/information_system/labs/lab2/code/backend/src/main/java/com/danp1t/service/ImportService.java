package com.danp1t.service;

import com.danp1t.model.*;
import com.danp1t.repository.ImportOperationRepository;
import com.danp1t.repository.OrganizationRepository;
import com.danp1t.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ImportService {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private ImportOperationRepository importOperationRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private SessionFactory sessionFactory;

    public ImportOperation importOrganizationsFromXml(InputStream xmlStream, User detachedUser, String fileName) {
        Session mainSession = null;
        Transaction mainTransaction = null;

        try {
            mainSession = sessionFactory.openSession();
            mainTransaction = mainSession.beginTransaction();

            User user = mainSession.get(User.class, detachedUser.getId());
            if (user == null) {
                throw new RuntimeException("Пользователь не найден");
            }

            ImportOperation operation = new ImportOperation();
            operation.setFileName(fileName);
            operation.setImportDate(java.time.LocalDateTime.now());
            operation.setUser(user);
            operation.setStatus("PROCESSING");

            importOperationRepository.saveWithSession(operation, mainSession);
            mainSession.flush();

            List<Organization> organizations = parseAndValidateXml(xmlStream);

            for (Organization organization : organizations) {
                if (organization.getCoordinates() != null) {
                    organization.setCoordinates(organization.getCoordinates());
                }
                if (organization.getOfficialAddress() != null) {
                    organization.setOfficialAddress(organization.getOfficialAddress());
                }
                if (organization.getPostalAddress() != null) {
                    organization.setPostalAddress(organization.getPostalAddress());
                }

                mainSession.persist(organization);

                if (organizations.indexOf(organization) % 20 == 0) {
                    mainSession.flush();
                    mainSession.clear();
                }
            }

            operation.setStatus("SUCCESS");
            operation.setRecordsAdded(organizations.size());
            importOperationRepository.mergeWithSession(operation, mainSession);

            mainTransaction.commit();
            return operation;

        } catch (Exception e) {
            if (mainTransaction != null) {
                try {
                    mainTransaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
                }
            }

            createFailedImportOperation(detachedUser, fileName, e.getMessage());

            throw new RuntimeException("Ошибка импорта: " + e.getMessage(), e);
        } finally {
            if (mainSession != null && mainSession.isOpen()) {
                mainSession.close();
            }
        }
    }

    private void createFailedImportOperation(User user, String fileName, String errorMessage) {
        Session errorSession = null;
        Transaction errorTransaction = null;

        try {
            errorSession = sessionFactory.openSession();
            errorTransaction = errorSession.beginTransaction();

            User managedUser = errorSession.get(User.class, user.getId());

            ImportOperation failedOperation = new ImportOperation();
            failedOperation.setFileName(fileName);
            failedOperation.setImportDate(java.time.LocalDateTime.now());
            failedOperation.setUser(managedUser);
            failedOperation.setStatus("FAILED");
            failedOperation.setErrorMessage(errorMessage);

            errorSession.persist(failedOperation);
            errorTransaction.commit();

        } catch (Exception e) {
            if (errorTransaction != null) {
                try {
                    errorTransaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Ошибка при откате транзакции ошибки: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Не удалось сохранить информацию об ошибке импорта: " + e.getMessage());
        } finally {
            if (errorSession != null && errorSession.isOpen()) {
                errorSession.close();
            }
        }
    }

    private List<Organization> parseAndValidateXml(InputStream xmlStream) throws Exception {
        List<Organization> organizations = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);
        document.getDocumentElement().normalize();

        NodeList organizationNodes = document.getElementsByTagName("organization");

        for (int i = 0; i < organizationNodes.getLength(); i++) {
            Node node = organizationNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                Organization organization = parseOrganizationElement(element);
                validateOrganization(organization);

                organizations.add(organization);
            }
        }

        return organizations;
    }

    private void validateOrganization(Organization organization) {
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя организации обязательно");
        }

        if (organization.getName().length() > 255) {
            throw new IllegalArgumentException("Имя организации не должно превышать 255 символов");
        }

        if (organization.getAnnualTurnover() <= 0) {
            throw new IllegalArgumentException("Годовой оборот должен быть положительным числом");
        }

        if (organization.getEmployeesCount() <= 0) {
            throw new IllegalArgumentException("Количество сотрудников должно быть положительным числом");
        }

        if (organization.getCoordinates() == null) {
            throw new IllegalArgumentException("Координаты обязательны");
        }

        if (organization.getCoordinates().getX() == null) {
            throw new IllegalArgumentException("Координата X обязательна");
        }

        if (organization.getType() == null) {
            throw new IllegalArgumentException("Тип организации обязателен");
        }

        if (organization.getAnnualTurnover() > 1000000000) {
            throw new IllegalArgumentException("Годовой оборот не может превышать 1,000,000,000");
        }

        if (organization.getEmployeesCount() > 10_000_000) {
            throw new IllegalArgumentException("Количество сотрудников не может превышать 10,000,000");
        }
    }

    private Organization parseOrganizationElement(Element element) {
        Organization organization = new Organization();

        organization.setName(getElementText(element, "name"));
        organization.setAnnualTurnover(Float.parseFloat(getElementText(element, "annualTurnover")));
        organization.setEmployeesCount(Long.parseLong(Objects.requireNonNull(getElementText(element, "employeesCount"))));
        organization.setRating(Integer.parseInt(Objects.requireNonNull(getElementText(element, "rating"))));

        String typeStr = getElementText(element, "type");
        assert typeStr != null;
        organization.setType(OrganizationType.valueOf(typeStr.toUpperCase()));

        Element coordinatesElement = (Element) element.getElementsByTagName("coordinates").item(0);
        if (coordinatesElement != null) {
            Coordinates coordinates = new Coordinates();
            coordinates.setX(Float.parseFloat(getElementText(coordinatesElement, "x")));
            coordinates.setY(Double.parseDouble(getElementText(coordinatesElement, "y")));
            organization.setCoordinates(coordinates);
        }

        Element officialAddressElement = (Element) element.getElementsByTagName("officialAddress").item(0);
        if (officialAddressElement != null) {
            Location officialAddress = new Location();
            officialAddress.setX(Double.parseDouble(getElementText(officialAddressElement, "x")));
            officialAddress.setY(Float.parseFloat(getElementText(officialAddressElement, "y")));
            officialAddress.setZ(Double.parseDouble(getElementText(officialAddressElement, "z")));
            officialAddress.setName(getElementText(officialAddressElement, "name"));
            organization.setOfficialAddress(officialAddress);
        }

        Element postalAddressElement = (Element) element.getElementsByTagName("postalAddress").item(0);
        if (postalAddressElement != null) {
            Address postalAddress = new Address();
            postalAddress.setStreet(getElementText(postalAddressElement, "street"));
            postalAddress.setZipCode(getElementText(postalAddressElement, "zipCode"));
            organization.setPostalAddress(postalAddress);
        }

        return organization;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }

    public List<ImportOperation> getImportHistory(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return importOperationRepository.findAll();
        } else {
            return importOperationRepository.findByUser(user);
        }
    }
}