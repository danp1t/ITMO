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
            // 1. Открываем основную сессию и начинаем транзакцию
            mainSession = sessionFactory.openSession();
            mainTransaction = mainSession.beginTransaction();

            // 2. Получаем пользователя в текущем контексте
            User user = mainSession.get(User.class, detachedUser.getId());
            if (user == null) {
                throw new RuntimeException("Пользователь не найден");
            }

            // 3. Создаем операцию импорта
            ImportOperation operation = new ImportOperation();
            operation.setFileName(fileName);
            operation.setImportDate(java.time.LocalDateTime.now());
            operation.setUser(user);
            operation.setStatus("PROCESSING");

            // 4. Сохраняем операцию в текущей сессии
            importOperationRepository.saveWithSession(operation, mainSession);
            mainSession.flush(); // Синхронизируем с БД

            // 5. Парсим XML и валидируем все данные ПЕРЕД сохранением
            List<Organization> organizations = parseAndValidateXml(xmlStream);

            // 6. Сохраняем ВСЕ организации в ОДНОЙ транзакции
            for (Organization organization : organizations) {
                // Устанавливаем связи
                if (organization.getCoordinates() != null) {
                    organization.setCoordinates(organization.getCoordinates());
                }
                if (organization.getOfficialAddress() != null) {
                    organization.setOfficialAddress(organization.getOfficialAddress());
                }
                if (organization.getPostalAddress() != null) {
                    organization.setPostalAddress(organization.getPostalAddress());
                }

                // Сохраняем организацию
                mainSession.persist(organization);

                // Периодически сбрасываем кэш для предотвращения утечек памяти
                if (organizations.indexOf(organization) % 20 == 0) {
                    mainSession.flush();
                    mainSession.clear();
                }
            }

            // 7. Обновляем операцию
            operation.setStatus("SUCCESS");
            operation.setRecordsAdded(organizations.size());
            importOperationRepository.mergeWithSession(operation, mainSession);

            // 8. Коммитим основную транзакцию
            mainTransaction.commit();
            return operation;

        } catch (Exception e) {
            // 9. Откатываем основную транзакцию при ошибке
            if (mainTransaction != null) {
                try {
                    mainTransaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
                }
            }

            // 10. Создаем запись об ошибке в ОТДЕЛЬНОЙ транзакции
            createFailedImportOperation(detachedUser, fileName, e.getMessage());

            throw new RuntimeException("Ошибка импорта: " + e.getMessage(), e);
        } finally {
            // 11. Всегда закрываем сессию
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

            // Получаем пользователя в новой сессии
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
        // Защита от XXE атак
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);
        document.getDocumentElement().normalize();

        NodeList organizationNodes = document.getElementsByTagName("organization");

        for (int i = 0; i < organizationNodes.getLength(); i++) {
            Node node = organizationNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                Organization organization = parseOrganizationElement(element);

                // Валидация согласно ограничениям предметной области
                validateOrganization(organization);

                organizations.add(organization);
            }
        }

        return organizations;
    }

    private void validateOrganization(Organization organization) {
        // Проверка обязательных полей
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

        // Проверка координат
        if (organization.getCoordinates() == null) {
            throw new IllegalArgumentException("Координаты обязательны");
        }

        if (organization.getCoordinates().getX() == null) {
            throw new IllegalArgumentException("Координата X обязательна");
        }

        // Проверка типа организации
        if (organization.getType() == null) {
            throw new IllegalArgumentException("Тип организации обязателен");
        }

        // Дополнительные проверки из ЛР1
        if (organization.getAnnualTurnover() > 1000000000) {
            throw new IllegalArgumentException("Годовой оборот не может превышать 1,000,000,000");
        }

        if (organization.getEmployeesCount() > 10000) {
            throw new IllegalArgumentException("Количество сотрудников не может превышать 10,000");
        }
    }

    private Organization parseOrganizationElement(Element element) {
        Organization organization = new Organization();

        // Основные поля
        organization.setName(getElementText(element, "name"));
        organization.setAnnualTurnover(Float.parseFloat(getElementText(element, "annualTurnover")));
        organization.setEmployeesCount(Long.parseLong(Objects.requireNonNull(getElementText(element, "employeesCount"))));
        organization.setRating(Integer.parseInt(Objects.requireNonNull(getElementText(element, "rating"))));

        String typeStr = getElementText(element, "type");
        assert typeStr != null;
        organization.setType(OrganizationType.valueOf(typeStr.toUpperCase()));

        // Coordinates
        Element coordinatesElement = (Element) element.getElementsByTagName("coordinates").item(0);
        if (coordinatesElement != null) {
            Coordinates coordinates = new Coordinates();
            coordinates.setX(Float.parseFloat(getElementText(coordinatesElement, "x")));
            coordinates.setY(Double.parseDouble(getElementText(coordinatesElement, "y")));
            organization.setCoordinates(coordinates);
        }

        // Official Address (Location)
        Element officialAddressElement = (Element) element.getElementsByTagName("officialAddress").item(0);
        if (officialAddressElement != null) {
            Location officialAddress = new Location();
            officialAddress.setX(Double.parseDouble(getElementText(officialAddressElement, "x")));
            officialAddress.setY(Float.parseFloat(getElementText(officialAddressElement, "y")));
            officialAddress.setZ(Double.parseDouble(getElementText(officialAddressElement, "z")));
            officialAddress.setName(getElementText(officialAddressElement, "name"));
            organization.setOfficialAddress(officialAddress);
        }

        // Postal Address (Address)
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