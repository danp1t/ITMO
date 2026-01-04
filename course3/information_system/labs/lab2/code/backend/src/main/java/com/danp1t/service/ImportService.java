package com.danp1t.service;

import com.danp1t.error.InvalidXmlException;
import com.danp1t.error.UserNotFoundException;
import com.danp1t.model.*;
import com.danp1t.repository.ImportOperationRepository;
import com.danp1t.repository.OrganizationRepository;
import com.danp1t.error.*;
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
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportService {

    @Inject
    private ImportOperationRepository importOperationRepository;

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private SessionFactory sessionFactory;

    public ImportOperation importOrganizationsFromXml(InputStream xmlStream, User detachedUser, String fileName)
            throws UserNotFoundException, InvalidXmlException {

        List<Organization> organizations = parseAndValidateXml(xmlStream);
        Set<String> uniqueTriplets = new HashSet<>();

        for (Organization organization : organizations) {
            checkUniquenessInFile(organization, uniqueTriplets);
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            });
            transaction = session.beginTransaction();

            User user = importOperationRepository.findUserById(detachedUser.getId(), session);
            if (user == null) {
                throw new UserNotFoundException(String.valueOf(detachedUser.getId()));
            }

            ImportOperation operation = new ImportOperation();
            operation.setFileName(fileName);
            operation.setImportDate(LocalDateTime.now());
            operation.setUser(user);
            operation.setStatus("PROCESSING");
            operation.setRecordsAdded(organizations.size());

            importOperationRepository.saveWithSession(operation, session);
            session.flush();

            for (Organization organization : organizations) {
                checkUniquenessInDatabase(organization, session);

                saveRelatedEntities(organization, session);

                organizationRepository.save(organization, session);

                if (organizations.indexOf(organization) % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            operation.setStatus("SUCCESS");
            importOperationRepository.mergeWithSession(operation, session);

            transaction.commit();
            return operation;

        } catch (UserNotFoundException | InvalidXmlException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            String errorMessage = "Неожиданная ошибка при импорте: " + e.getMessage();

            Session errorSession = sessionFactory.openSession();
            Transaction errorTransaction = errorSession.beginTransaction();
            try {
                importOperationRepository.createFailedImportOperation(detachedUser, fileName, errorMessage, errorSession);
                errorTransaction.commit();
            } catch (Exception ex) {
                if (errorTransaction != null) {
                    errorTransaction.rollback();
                }
                throw ex;
            } finally {
                errorSession.close();
            }

            throw new ImportException(errorMessage, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void checkUniquenessInFile(Organization organization, Set<String> uniqueTriplets) throws InvalidXmlException {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        if (name == null || coords == null || address == null) {
            throw new InvalidXmlException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        String addressKey = address.getStreet() + ":" + (address.getZipCode() != null ? address.getZipCode() : "");
        String uniqueKey = name + ":" + coords.getX() + ":" + coords.getY() + ":" + addressKey;

        if (uniqueTriplets.contains(uniqueKey)) {
            throw new InvalidXmlException("Нарушение уникальности в файле: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже присутствует в импортируемом файле");
        }

        uniqueTriplets.add(uniqueKey);
    }

    private void checkUniquenessInDatabase(Organization organization, Session session) throws InvalidXmlException {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        if (name == null || coords == null || address == null) {
            throw new InvalidXmlException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        boolean existsInDb = organizationRepository.existsByNameAndCoordinatesAndAddress(
                name, coords, address, session);

        if (existsInDb) {
            throw new InvalidXmlException("Нарушение уникальности: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже существует в базе данных");
        }
    }

    private void saveRelatedEntities(Organization organization, Session session) {
        if (organization.getCoordinates() != null && organization.getCoordinates().getId() == null) {
            organizationRepository.saveCoordinates(organization.getCoordinates(), session);
        }
        if (organization.getOfficialAddress() != null && organization.getOfficialAddress().getId() == null) {
            organizationRepository.saveLocation(organization.getOfficialAddress(), session);
        }
        if (organization.getPostalAddress() != null && organization.getPostalAddress().getId() == null) {
            organizationRepository.saveAddress(organization.getPostalAddress(), session);
        }
    }

    private List<Organization> parseAndValidateXml(InputStream xmlStream) throws InvalidXmlException {
        try {
            List<Organization> organizations = new ArrayList<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlStream);
            document.getDocumentElement().normalize();

            NodeList organizationNodes = document.getElementsByTagName("organization");

            if (organizationNodes.getLength() == 0) {
                throw new InvalidXmlException("XML не содержит элементов organization");
            }

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

        } catch (ParserConfigurationException e) {
            throw new InvalidXmlException("Ошибка конфигурации парсера XML", e);
        } catch (SAXException e) {
            throw new InvalidXmlException("Некорректный формат XML", e);
        } catch (IOException e) {
            throw new InvalidXmlException("Ошибка чтения XML потока", e);
        } catch (NumberFormatException e) {
            throw new InvalidXmlException("Некорректный числовой формат в XML", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidXmlException("Некорректное значение в XML", e);
        } catch (Exception e) {
            throw new InvalidXmlException("Ошибка при обработке XML: " + e.getMessage(), e);
        }
    }

    private void validateOrganization(Organization organization) throws InvalidXmlException {
        try {
            if (organization.getCoordinates() != null) {
                organization.getCoordinates().validate();
            }
            if (organization.getOfficialAddress() != null) {
                organization.getOfficialAddress().validate();
            }
            if (organization.getPostalAddress() != null) {
                organization.getPostalAddress().validate();
            }
            organization.validate();
        } catch (Exception e) {
            throw new InvalidXmlException("Ошибка валидации организации: " + e.getMessage(), e);
        }
    }

    private Organization parseOrganizationElement(Element element) throws InvalidXmlException {
        try {
            Organization organization = new Organization();

            organization.setName(getRequiredElementText(element, "name"));
            organization.setAnnualTurnover(parseFloat(getRequiredElementText(element, "annualTurnover")));
            organization.setEmployeesCount(parseLong(getRequiredElementText(element, "employeesCount")));
            organization.setRating(parseInt(getRequiredElementText(element, "rating")));

            String typeStr = getRequiredElementText(element, "type");
            organization.setType(OrganizationType.valueOf(typeStr.toUpperCase()));

            Element coordinatesElement = (Element) element.getElementsByTagName("coordinates").item(0);
            if (coordinatesElement != null) {
                Coordinates coordinates = new Coordinates();
                coordinates.setX(parseFloat(getRequiredElementText(coordinatesElement, "x")));
                coordinates.setY(parseDouble(getRequiredElementText(coordinatesElement, "y")));
                organization.setCoordinates(coordinates);
            }

            Element officialAddressElement = (Element) element.getElementsByTagName("officialAddress").item(0);
            if (officialAddressElement != null) {
                Location officialAddress = new Location();
                officialAddress.setX(parseDouble(getRequiredElementText(officialAddressElement, "x")));
                officialAddress.setY(parseFloat(getRequiredElementText(officialAddressElement, "y")));
                officialAddress.setZ(parseDouble(getRequiredElementText(officialAddressElement, "z")));
                officialAddress.setName(getRequiredElementText(officialAddressElement, "name"));
                organization.setOfficialAddress(officialAddress);
            }

            Element postalAddressElement = (Element) element.getElementsByTagName("postalAddress").item(0);
            if (postalAddressElement != null) {
                Address postalAddress = new Address();
                postalAddress.setStreet(getRequiredElementText(postalAddressElement, "street"));
                postalAddress.setZipCode(getElementText(postalAddressElement, "zipCode"));
                organization.setPostalAddress(postalAddress);
            }

            return organization;

        } catch (NullPointerException e) {
            throw new InvalidXmlException("Отсутствует обязательный элемент в XML", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidXmlException("Некорректное значение типа организации", e);
        }
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            String text = nodeList.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }

    private String getRequiredElementText(Element parent, String tagName) throws InvalidXmlException {
        String text = getElementText(parent, tagName);
        if (text == null || text.isEmpty()) {
            throw new InvalidXmlException("Обязательный элемент '" + tagName + "' отсутствует или пуст");
        }
        return text;
    }

    private float parseFloat(String value) throws InvalidXmlException {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new InvalidXmlException("Некорректное числовое значение: " + value, e);
        }
    }

    private double parseDouble(String value) throws InvalidXmlException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InvalidXmlException("Некорректное числовое значение: " + value, e);
        }
    }

    private long parseLong(String value) throws InvalidXmlException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new InvalidXmlException("Некорректное числовое значение: " + value, e);
        }
    }

    private int parseInt(String value) throws InvalidXmlException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidXmlException("Некорректное числовое значение: " + value, e);
        }
    }

    public List<ImportOperation> getImportHistory(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return importOperationRepository.findAll();
        } else {
            return importOperationRepository.findByUser(user);
        }
    }
}