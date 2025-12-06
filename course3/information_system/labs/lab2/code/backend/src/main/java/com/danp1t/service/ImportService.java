package com.danp1t.service;

import com.danp1t.model.*;
import com.danp1t.repository.ImportOperationRepository;
import com.danp1t.repository.OrganizationRepository;
import com.danp1t.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    public ImportOperation importOrganizationsFromXml(InputStream xmlStream, User detachedUser, String fileName) {
        // 1. Получаем пользователя в текущем контексте транзакции
        User user = userRepository.findById(detachedUser.getId());
        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }

        // 2. Создаем операцию импорта
        ImportOperation operation = new ImportOperation();
        operation.setFileName(fileName);
        operation.setImportDate(java.time.LocalDateTime.now());
        operation.setUser(user);
        operation.setStatus("PROCESSING");

        // 3. Сохраняем операцию через репозиторий
        importOperationRepository.save(operation);

        try {
            // 4. Парсим XML
            List<Organization> organizations = parseXml(xmlStream);
            List<Organization> savedOrganizations = new ArrayList<>();

            // 5. Сохраняем организации
            for (Organization organization : organizations) {
                organization.validate();
                // Устанавливаем связи, если они не установлены
                if (organization.getCoordinates() != null) {
                    organization.setCoordinates(organization.getCoordinates());
                }
                if (organization.getOfficialAddress() != null) {
                    organization.setOfficialAddress(organization.getOfficialAddress());
                }
                if (organization.getPostalAddress() != null) {
                    organization.setPostalAddress(organization.getPostalAddress());
                }

                organizationRepository.save(organization);
                savedOrganizations.add(organization);
            }

            // 6. Обновляем операцию
            operation.setStatus("SUCCESS");
            operation.setRecordsAdded(savedOrganizations.size());

            // 7. Сохраняем обновленную операцию
            importOperationRepository.merge(operation);

            return operation;

        } catch (Exception e) {
            operation.setStatus("FAILED");
            operation.setErrorMessage(e.getMessage());
            // Сохраняем операцию с информацией об ошибке
            importOperationRepository.merge(operation);
            throw new RuntimeException("Ошибка импорта: " + e.getMessage(), e);
        }
    }

    private List<Organization> parseXml(InputStream xmlStream) throws Exception {
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
                organizations.add(organization);
            }
        }

        return organizations;
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
        Coordinates coordinates = new Coordinates();
        coordinates.setX(Float.parseFloat(getElementText(coordinatesElement, "x")));
        coordinates.setY(Double.parseDouble(getElementText(coordinatesElement, "y")));
        organization.setCoordinates(coordinates);

        // Official Address (Location)
        Element officialAddressElement = (Element) element.getElementsByTagName("officialAddress").item(0);
        Location officialAddress = new Location();
        officialAddress.setX(Double.parseDouble(getElementText(officialAddressElement, "x")));
        officialAddress.setY(Float.parseFloat(getElementText(officialAddressElement, "y")));
        officialAddress.setZ(Double.parseDouble(getElementText(officialAddressElement, "z")));
        officialAddress.setName(getElementText(officialAddressElement, "name"));
        organization.setOfficialAddress(officialAddress);

        // Postal Address (Address)
        Element postalAddressElement = (Element) element.getElementsByTagName("postalAddress").item(0);
        Address postalAddress = new Address();
        postalAddress.setStreet(getElementText(postalAddressElement, "street"));
        postalAddress.setZipCode(getElementText(postalAddressElement, "zipCode"));
        organization.setPostalAddress(postalAddress);

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