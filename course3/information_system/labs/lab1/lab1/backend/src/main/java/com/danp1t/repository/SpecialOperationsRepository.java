package com.danp1t.repository;

import com.danp1t.model.Organization;
import com.danp1t.model.Address;
import com.danp1t.dto.OrganizationDTO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SpecialOperationsRepository {

    @Inject
    private SessionFactory sessionFactory;

    public Double calculateAverageRating() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT AVG(o.rating) FROM Organization o";
            Double result = session.createQuery(hql, Double.class)
                    .uniqueResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка расчета среднего рейтинга организаций: " + e.getMessage(), e);
        }
    }

    public List<OrganizationDTO> findOrganizationsByNameStartingWith(String substring) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(:substring)";
            List<Organization> organizations = session.createQuery(hql, Organization.class)
                    .setParameter("substring", substring + "%")
                    .list();

            return organizations.stream()
                    .map(this::convertToSearchDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска организации по имени: " + e.getMessage(), e);
        }
    }

    public List<OrganizationDTO> findOrganizationsByPostalAddressGreaterThan(Long minAddressId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT o FROM Organization o WHERE o.postalAddress.id > :minAddressId";
            List<Organization> organizations = session.createQuery(hql, Organization.class)
                    .setParameter("minAddressId", minAddressId)
                    .list();

            return organizations.stream()
                    .map(this::convertToSearchDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка фильтрации по адресу: " + e.getMessage(), e);
        }
    }

    private OrganizationDTO convertToSearchDTO(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());

        if (organization.getAnnualTurnover() != null) {
            dto.setAnnualTurnover(organization.getAnnualTurnover().floatValue());
        }

        if (organization.getEmployeesCount() != null) {
            dto.setEmployeesCount(organization.getEmployeesCount().longValue());
        }

        if (organization.getRating() != null) {
            dto.setRating(organization.getRating().intValue());
        }

        dto.setType(organization.getType());

        if (organization.getCoordinates() != null) {
            dto.setCoordinates(organization.getCoordinates().getId());
        }

        if (organization.getOfficialAddress() != null) {
            dto.setOfficialAddress(organization.getOfficialAddress().getId());
        }

        if (organization.getPostalAddress() != null) {
            dto.setPostalAddress(organization.getPostalAddress().getId());
        }

        return dto;
    }

    public Organization mergeOrganizations(Long firstOrgId, Long secondOrgId, String newName, Long newAddressId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            Organization firstOrg = session.createQuery(
                            "SELECT o FROM Organization o LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress LEFT JOIN FETCH o.postalAddress WHERE o.id = :id", Organization.class)
                    .setParameter("id", firstOrgId)
                    .uniqueResult();

            Organization secondOrg = session.createQuery(
                            "SELECT o FROM Organization o LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress LEFT JOIN FETCH o.postalAddress WHERE o.id = :id", Organization.class)
                    .setParameter("id", secondOrgId)
                    .uniqueResult();

            if (firstOrg == null || secondOrg == null) {
                throw new IllegalArgumentException("Одна или обе организации не найдены");
            }

            Address newAddress = session.get(Address.class, newAddressId);
            if (newAddress == null) {
                throw new IllegalArgumentException("Адрес с ID " + newAddressId + " не найден");
            }

            Organization newOrganization = new Organization();
            newOrganization.setName(newName);
            newOrganization.setAnnualTurnover(firstOrg.getAnnualTurnover() + secondOrg.getAnnualTurnover());
            newOrganization.setEmployeesCount(firstOrg.getEmployeesCount() + secondOrg.getEmployeesCount());
            newOrganization.setRating((firstOrg.getRating() + secondOrg.getRating()) / 2);
            newOrganization.setType(firstOrg.getType());
            newOrganization.setCoordinates(firstOrg.getCoordinates());
            newOrganization.setOfficialAddress(firstOrg.getOfficialAddress());
            newOrganization.setPostalAddress(newAddress);

            session.persist(newOrganization);

            session.remove(firstOrg);
            session.remove(secondOrg);

            transaction.commit();
            return newOrganization;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка объединения организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Organization absorbOrganization(Long absorbingOrgId, Long absorbedOrgId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            // Получаем организации
            Organization absorbingOrg = session.createQuery(
                            "SELECT o FROM Organization o LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress LEFT JOIN FETCH o.postalAddress WHERE o.id = :id", Organization.class)
                    .setParameter("id", absorbingOrgId)
                    .uniqueResult();

            Organization absorbedOrg = session.createQuery(
                            "SELECT o FROM Organization o LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress LEFT JOIN FETCH o.postalAddress WHERE o.id = :id", Organization.class)
                    .setParameter("id", absorbedOrgId)
                    .uniqueResult();

            if (absorbingOrg == null || absorbedOrg == null) {
                throw new IllegalArgumentException("Одна или обе организации не найдены");
            }

            long totalEmployees = absorbingOrg.getEmployeesCount() + absorbedOrg.getEmployeesCount();
            absorbingOrg.setEmployeesCount(totalEmployees);
            absorbingOrg.setAnnualTurnover(absorbingOrg.getAnnualTurnover() + absorbedOrg.getAnnualTurnover());
            absorbingOrg.setRating((absorbingOrg.getRating() + absorbedOrg.getRating()) / 2);
            Organization updatedOrganization = session.merge(absorbingOrg);
            session.remove(absorbedOrg);

            transaction.commit();
            return updatedOrganization;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка поглощения организаций: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
}