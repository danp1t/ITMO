package com.danp1t.service;

import com.danp1t.error.InvalidXmlException;
import com.danp1t.error.UserNotFoundException;
import com.danp1t.model.*;
import com.danp1t.repository.ImportOperationRepository;
import com.danp1t.error.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ImportService {

    @Inject
    private ImportOperationRepository importOperationRepository;

    @Inject
    private SessionFactory sessionFactory;

    public ImportOperation importOrganizationsFromXml(InputStream xmlStream, User detachedUser, String fileName)
            throws UserNotFoundException, InvalidXmlException {
        Session mainSession = null;
        Transaction mainTransaction = null;

        try {
            mainSession = sessionFactory.openSession();
            mainSession.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            });
            mainTransaction = mainSession.beginTransaction();

            User user = mainSession.get(User.class, detachedUser.getId());
            if (user == null) {
                throw new UserNotFoundException(String.valueOf(detachedUser.getId()));
            }

            ImportOperation operation = new ImportOperation();
            operation.setFileName(fileName);
            operation.setImportDate(java.time.LocalDateTime.now());
            operation.setUser(user);
            operation.setStatus("PROCESSING");

            importOperationRepository.saveWithSession(operation, mainSession);
            mainSession.flush();

            List<Organization> organizations = parseAndValidateXml(xmlStream);

            // Множества для отслеживания уникальности в рамках текущего импорта
            Set<String> nameCoordinatesPairs = new HashSet<>();
            Set<String> nameAddressPairs = new HashSet<>();
            Set<String> coordinatesAddressPairs = new HashSet<>();

            for (Organization organization : organizations) {
                // Проверка уникальности для текущей организации
                checkOrganizationUniqueness(organization, mainSession, nameCoordinatesPairs,
                        nameAddressPairs, coordinatesAddressPairs);

                // Сохраняем пары для проверки дубликатов в рамках текущего импорта
                String name = organization.getName();
                Coordinates coords = organization.getCoordinates();
                Address address = organization.getPostalAddress();

                if (name != null && coords != null) {
                    String nameCoordsKey = name + ":" + coords.getX() + ":" + coords.getY();
                    nameCoordinatesPairs.add(nameCoordsKey);
                }

                if (name != null && address != null && address.getId() != null) {
                    String nameAddressKey = name + ":" + address.getId();
                    nameAddressPairs.add(nameAddressKey);
                }

                if (coords != null && address != null && address.getId() != null) {
                    String coordsAddressKey = coords.getX() + ":" + coords.getY() + ":" + address.getId();
                    coordinatesAddressPairs.add(coordsAddressKey);
                }

                if (organization.getCoordinates() != null) {
                    mainSession.persist(organization.getCoordinates());
                }
                if (organization.getOfficialAddress() != null) {
                    mainSession.persist(organization.getOfficialAddress());
                }
                if (organization.getPostalAddress() != null) {
                    mainSession.persist(organization.getPostalAddress());
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

        } catch (UserNotFoundException e) {
            rollbackTransaction(mainTransaction);
            throw e;

        } catch (InvalidXmlException e) {
            rollbackTransaction(mainTransaction);
            createFailedImportOperation(detachedUser, fileName, e.getMessage());
            throw e;

        } catch (Exception e) {
            rollbackTransaction(mainTransaction);
            String errorMessage = "Неожиданная ошибка при импорте: " + e.getMessage();
            createFailedImportOperation(detachedUser, fileName, errorMessage);
            throw new ImportException(errorMessage, e);

        } finally {
            closeSession(mainSession);
        }
    }

    /**
     * Проверяет уникальность организации по трем парам полей
     */
    private void checkOrganizationUniqueness(Organization organization, Session session,
                                             Set<String> nameCoordinatesPairs,
                                             Set<String> nameAddressPairs,
                                             Set<String> coordinatesAddressPairs) throws InvalidXmlException {

        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        // Проверка 1: (name, coordinates)
        if (name != null && coords != null) {
            // Проверка в рамках текущего импорта
            String nameCoordsKey = name + ":" + coords.getX() + ":" + coords.getY();
            if (nameCoordinatesPairs.contains(nameCoordsKey)) {
                throw new InvalidXmlException("Нарушение уникальности: организация с названием '" + name +
                        "' и координатами (" + coords.getX() + ", " + coords.getY() +
                        ") уже присутствует в импортируемом файле");
            }

            // Проверка в базе данных
            String hql1 = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y";
            boolean existsInDb1 = session.createQuery(hql1, Boolean.class)
                    .setParameter("name", name)
                    .setParameter("x", coords.getX())
                    .setParameter("y", coords.getY())
                    .uniqueResult();

            if (existsInDb1) {
                throw new InvalidXmlException("Нарушение уникальности: организация с названием '" + name +
                        "' и координатами (" + coords.getX() + ", " + coords.getY() +
                        ") уже существует в базе данных");
            }
        }

        // Проверка 2: (name, address)
        if (name != null && address != null && address.getId() != null) {
            // Проверка в рамках текущего импорта
            String nameAddressKey = name + ":" + address.getId();
            if (nameAddressPairs.contains(nameAddressKey)) {
                throw new InvalidXmlException("Нарушение уникальности: организация с названием '" + name +
                        "' и адресом ID=" + address.getId() +
                        " уже присутствует в импортируемом файле");
            }

            // Проверка в базе данных
            String hql2 = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.postalAddress.id = :addressId";
            boolean existsInDb2 = session.createQuery(hql2, Boolean.class)
                    .setParameter("name", name)
                    .setParameter("addressId", address.getId())
                    .uniqueResult();

            if (existsInDb2) {
                throw new InvalidXmlException("Нарушение уникальности: организация с названием '" + name +
                        "' и адресом ID=" + address.getId() +
                        " уже существует в базе данных");
            }
        }

        // Проверка 3: (coordinates, address)
        if (coords != null && address != null && address.getId() != null) {
            // Проверка в рамках текущего импорта
            String coordsAddressKey = coords.getX() + ":" + coords.getY() + ":" + address.getId();
            if (coordinatesAddressPairs.contains(coordsAddressKey)) {
                throw new InvalidXmlException("Нарушение уникальности: организация с координатами (" +
                        coords.getX() + ", " + coords.getY() +
                        ") и адресом ID=" + address.getId() +
                        " уже присутствует в импортируемом файле");
            }

            // Проверка в базе данных
            String hql3 = "SELECT COUNT(o) > 0 FROM Organization o WHERE " +
                    "o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.id = :addressId";
            boolean existsInDb3 = session.createQuery(hql3, Boolean.class)
                    .setParameter("x", coords.getX())
                    .setParameter("y", coords.getY())
                    .setParameter("addressId", address.getId())
                    .uniqueResult();

            if (existsInDb3) {
                throw new InvalidXmlException("Нарушение уникальности: организация с координатами (" +
                        coords.getX() + ", " + coords.getY() +
                        ") и адресом ID=" + address.getId() +
                        " уже существует в базе данных");
            }
        }
    }

    private void rollbackTransaction(Transaction transaction) {
        if (transaction != null) {
            try {
                transaction.rollback();
            } catch (Exception rollbackEx) {
                System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
        }
    }

    private void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("Ошибка при закрытии сессии: " + e.getMessage());
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
            closeSession(errorSession);
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
                postalAddress.setZipCode(getElementText(postalAddressElement, "zipCode")); // zipCode может быть null
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