package com.danp1t.repository;

import com.danp1t.model.Address;
import com.danp1t.model.Coordinates;
import com.danp1t.model.Organization;
import com.danp1t.model.OrganizationType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.query.Query;

import java.sql.Connection;
import java.util.List;

@ApplicationScoped
public class OrganizationRepository {

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

    public List<Organization> findOrganizationsByNameStartingWith(String substring) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(:substring)";

            return session.createQuery(hql, Organization.class)
                    .setParameter("substring", substring + "%")
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска организации по имени: " + e.getMessage(), e);
        }
    }

    public List<Organization> findOrganizationsByPostalAddressGreaterThan(Long minAddressId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT o FROM Organization o WHERE o.postalAddress.id > :minAddressId";

            return session.createQuery(hql, Organization.class)
                    .setParameter("minAddressId", minAddressId)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка фильтрации по адресу: " + e.getMessage(), e);
        }
    }

    public Organization mergeOrganizations(Long firstOrgId, Long secondOrgId, String newName, Long newAddressId) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            });
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
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            });
            transaction = session.beginTransaction();

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

    public List<Organization> findAllWithFilters(
            int offset, int limit, String search, String type, String sortBy, String sortOrder) {

        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder(
                    "SELECT o FROM Organization o " +
                            "LEFT JOIN FETCH o.coordinates " +
                            "LEFT JOIN FETCH o.officialAddress " +
                            "LEFT JOIN FETCH o.postalAddress " +
                            "WHERE 1=1");

            if (search != null && !search.trim().isEmpty()) {
                hql.append(" AND LOWER(o.name) LIKE LOWER(:search)");
            }

            if (type != null && !type.trim().isEmpty()) {
                hql.append(" AND o.type = :type");
            }

            if (sortBy != null && !sortBy.trim().isEmpty()) {
                String order = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
                hql.append(" ORDER BY o.").append(sortBy).append(" ").append(order);
            } else {
                hql.append(" ORDER BY o.id ASC");
            }

            Query<Organization> query = session.createQuery(hql.toString(), Organization.class);

            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }

            if (type != null && !type.trim().isEmpty()) {
                query.setParameter("type", OrganizationType.valueOf(type));
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска организаций с фильтрами: " + e.getMessage(), e);
        }
    }

    public long countWithFilters(String search, String type) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(o) FROM Organization o WHERE 1=1");

            if (search != null && !search.trim().isEmpty()) {
                hql.append(" AND LOWER(o.name) LIKE LOWER(:search)");
            }

            if (type != null && !type.trim().isEmpty()) {
                hql.append(" AND o.type = :type");
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);

            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }

            if (type != null && !type.trim().isEmpty()) {
                query.setParameter("type", OrganizationType.valueOf(type));
            }

            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка подсчета организаций: " + e.getMessage(), e);
        }
    }
}