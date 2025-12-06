package com.danp1t.service;

import com.danp1t.model.*;
import com.danp1t.repository.ImportOperationRepository;
import com.danp1t.repository.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

    @Transactional
    public ImportOperation importOrganizationsFromXml(InputStream xmlStream, User user, String fileName) {
        ImportOperation operation = new ImportOperation(fileName, user);
        importOperationRepository.save(operation);

        try {
            List<Organization> organizations = parseXml(xmlStream);
            List<Organization> savedOrganizations = new ArrayList<>();

            // Валидируем и сохраняем организации в транзакции
            for (Organization organization : organizations) {
                organization.validate();
                Organization savedOrg = organizationRepository.save(organization);
                savedOrganizations.add(savedOrg);
            }

            operation.setStatus("SUCCESS");
            operation.setRecordsAdded(savedOrganizations.size());
            importOperationRepository.save(operation);

            return operation;

        } catch (Exception e) {
            operation.setStatus("FAILED");
            operation.setErrorMessage(e.getMessage());
            importOperationRepository.save(operation);

            // Откатываем транзакцию (благодаря @Transactional)
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