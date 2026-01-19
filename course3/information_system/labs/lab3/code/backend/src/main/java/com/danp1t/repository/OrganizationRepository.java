package com.danp1t.repository;

import com.danp1t.model.*;
import jakarta.persistence.QueryHint;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.jpa.AvailableHints;
import org.hibernate.query.Query;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    private SessionFactory sessionFactory;

    public Organization save(Organization organization, Session session) {
        session.persist(organization);
        return organization;
    }

    public Organization findById(Long id, Session session) {
        return session.get(Organization.class, id);
    }

    public Organization update(Organization organization, Session session) {
        return session.merge(organization);
    }

    public boolean delete(Long id, Session session) {
        Organization organization = session.get(Organization.class, id);
        if (organization != null) {
            session.remove(organization);
            return true;
        }
        return false;
    }

    public void saveCoordinates(Coordinates coordinates, Session session) {
        if (coordinates.getId() == null) {
            session.persist(coordinates);
        } else {
            session.merge(coordinates);
        }
    }

    public void saveLocation(Location location, Session session) {
        if (location.getId() == null) {
            session.persist(location);
        } else {
            session.merge(location);
        }
    }

    public void saveAddress(Address address, Session session) {
        if (address.getId() == null) {
            session.persist(address);
        } else {
            session.merge(address);
        }
    }

    public boolean existsByNameAndCoordinatesAndAddress(String name, Coordinates coordinates, Address address, Session session) {
        String hql;
        if (address.getId() != null) {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.id = :addressId";
        } else {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.street = :street " +
                    "AND (o.postalAddress.zipCode = :zipCode OR (o.postalAddress.zipCode IS NULL AND :zipCode IS NULL))";
        }

        var query = session.createQuery(hql, Boolean.class)
                .setParameter("name", name)
                .setParameter("x", coordinates.getX())
                .setParameter("y", coordinates.getY());

        if (address.getId() != null) {
            query.setParameter("addressId", address.getId());
        } else {
            query.setParameter("street", address.getStreet())
                    .setParameter("zipCode", address.getZipCode());
        }

        return query.uniqueResult();
    }

    public boolean checkUniqueness(String name, Coordinates coordinates, Address address, Long excludeId, Session session) {
        String hql;
        if (address.getId() != null) {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.id = :addressId " +
                    "AND o.id != :excludeId";
        } else {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.street = :street " +
                    "AND (o.postalAddress.zipCode = :zipCode OR (o.postalAddress.zipCode IS NULL AND :zipCode IS NULL)) " +
                    "AND o.id != :excludeId";
        }

        var query = session.createQuery(hql, Boolean.class)
                .setParameter("name", name)
                .setParameter("x", coordinates.getX())
                .setParameter("y", coordinates.getY())
                .setParameter("excludeId", excludeId);

        if (address.getId() != null) {
            query.setParameter("addressId", address.getId());
        } else {
            query.setParameter("street", address.getStreet())
                    .setParameter("zipCode", address.getZipCode());
        }

        return query.uniqueResult();
    }

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

            System.out.println("DEBUG: Построение запроса с параметрами:");
            System.out.println("  search: " + search);
            System.out.println("  type: " + type);
            System.out.println("  sortBy: " + sortBy);
            System.out.println("  sortOrder: " + sortOrder);

            if (search != null && !search.trim().isEmpty()) {
                hql.append(" AND LOWER(o.name) LIKE LOWER(:search)");
                System.out.println("DEBUG: Добавлен поиск по имени");
            }

            if (type != null && !type.trim().isEmpty()) {
                try {
                    OrganizationType orgType = OrganizationType.valueOf(type);
                    hql.append(" AND o.type = :type");
                    System.out.println("DEBUG: Добавлен фильтр по типу: " + orgType);
                } catch (IllegalArgumentException e) {
                    System.err.println("ERROR: Неверный тип организации: " + type);
                    throw new IllegalArgumentException("Неверный тип организации: " + type, e);
                }
            }

            if (sortBy != null && !sortBy.trim().isEmpty()) {
                String[] allowedSortFields = {"id", "name", "annualTurnover", "employeesCount", "rating", "creationDate"};
                boolean isValidField = Arrays.asList(allowedSortFields).contains(sortBy);

                if (!isValidField) {
                    throw new IllegalArgumentException("Недопустимое поле для сортировки: " + sortBy);
                }

                String order = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
                hql.append(" ORDER BY o.").append(sortBy).append(" ").append(order);
                System.out.println("DEBUG: Добавлена сортировка по " + sortBy + " " + order);
            } else {
                hql.append(" ORDER BY o.id ASC");
                System.out.println("DEBUG: Добавлена сортировка по умолчанию (id ASC)");
            }

            System.out.println("DEBUG: Сформированный HQL: " + hql.toString());

            Query<Organization> query = session.createQuery(hql.toString(), Organization.class);

            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search + "%");
                System.out.println("DEBUG: Параметр search установлен: %" + search + "%");
            }

            if (type != null && !type.trim().isEmpty()) {
                query.setParameter("type", OrganizationType.valueOf(type));
                System.out.println("DEBUG: Параметр type установлен: " + OrganizationType.valueOf(type));
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);
            System.out.println("DEBUG: Пагинация: offset=" + offset + ", limit=" + limit);

            List<Organization> result = query.list();
            System.out.println("DEBUG: Найдено организаций: " + result.size());

            return result;

        } catch (Exception e) {
            System.err.println("ERROR в findAllWithFilters:");
            System.err.println("  Сообщение: " + e.getMessage());
            System.err.println("  Причина: " + e.getCause());
            System.err.println("  Стек:");
            e.printStackTrace();
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

    public Address findAddressById(Long addressId, Session session) {
        return session.get(Address.class, addressId);
    }

    public Coordinates findCoordinatesById(Long coordinatesId, Session session) {
        return session.get(Coordinates.class, coordinatesId);
    }

    public Location findLocationById(Long locationId, Session session) {
        return session.get(Location.class, locationId);
    }
}