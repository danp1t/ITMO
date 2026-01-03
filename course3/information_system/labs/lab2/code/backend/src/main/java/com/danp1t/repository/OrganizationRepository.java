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

    public Organization save(Organization organization) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            });
            transaction = session.beginTransaction();
            if (organization.getCoordinates().getId() == null) {
                session.persist(organization.getCoordinates());
            } else {
                organization.setCoordinates(session.merge(organization.getCoordinates()));
            }

            if (organization.getOfficialAddress().getId() == null) {
                session.persist(organization.getOfficialAddress());
            } else {
                organization.setOfficialAddress(session.merge(organization.getOfficialAddress()));
            }

            if (organization.getPostalAddress().getId() == null) {
                session.persist(organization.getPostalAddress());
            } else {
                organization.setPostalAddress(session.merge(organization.getPostalAddress()));
            }

            session.persist(organization);
            transaction.commit();

            return organization;
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка сохранения организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public List<Organization> findAll() {

        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT o FROM Organization o " +
                                    "LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress " +
                                    "LEFT JOIN FETCH o.postalAddress", Organization.class)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска организации: " + e.getMessage(), e);
        }
    }

    public Organization findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {

            return session.createQuery(
                            "SELECT o FROM Organization o " +
                                    "LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress " +
                                    "LEFT JOIN FETCH o.postalAddress " +
                                    "WHERE o.id = :id", Organization.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска организации по ID: " + e.getMessage(), e);
        }
    }

    public Organization update(Organization organization) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            });
            transaction = session.beginTransaction();
            Organization mergedOrganization = session.merge(organization);
            transaction.commit();

            return mergedOrganization;
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка обновления организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public void delete(Organization organization) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            Organization attachedOrganization = session.contains(organization)
                    ? organization
                    : session.merge(organization);

            attachedOrganization.setCoordinates(null);
            attachedOrganization.setOfficialAddress(null);
            attachedOrganization.setPostalAddress(null);

            session.remove(attachedOrganization);

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка удаления организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
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

            if (search != null && !search.trim().isEmpty()) {
                hql.append(" AND LOWER(o.name) LIKE LOWER(:search)");
            }

            if (type != null && !type.trim().isEmpty()) {
                hql.append(" AND o.type = :type");
            }

            // Добавляем сортировку
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

    public Organization saveWithTransaction(Organization organization) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            });
            transaction = session.beginTransaction();


            // Проверка уникальности
            checkUniqueness(organization, session, null);

            // Сохраняем связанные объекты
            if (organization.getCoordinates() != null && organization.getCoordinates().getId() == null) {
                session.persist(organization.getCoordinates());
            } else if (organization.getCoordinates() != null) {
                organization.setCoordinates(session.merge(organization.getCoordinates()));
            }

            if (organization.getOfficialAddress() != null && organization.getOfficialAddress().getId() == null) {
                session.persist(organization.getOfficialAddress());
            } else if (organization.getOfficialAddress() != null) {
                organization.setOfficialAddress(session.merge(organization.getOfficialAddress()));
            }

            if (organization.getPostalAddress() != null && organization.getPostalAddress().getId() == null) {
                session.persist(organization.getPostalAddress());
            } else if (organization.getPostalAddress() != null) {
                organization.setPostalAddress(session.merge(organization.getPostalAddress()));
            }

            session.persist(organization);
            transaction.commit();

            return organization;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка сохранения организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Organization updateWithTransaction(Integer id, Organization organization) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            });
            transaction = session.beginTransaction();


            Organization existingOrganization = session.createQuery(
                            "SELECT o FROM Organization o " +
                                    "LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress " +
                                    "LEFT JOIN FETCH o.postalAddress " +
                                    "WHERE o.id = :id", Organization.class)
                    .setParameter("id", id)
                    .uniqueResult();

            if (existingOrganization == null) {
                throw new IllegalArgumentException("Организация с ID " + id + " не найдена");
            }

            // Обновляем поля
            existingOrganization.setName(organization.getName());
            existingOrganization.setAnnualTurnover(organization.getAnnualTurnover());
            existingOrganization.setEmployeesCount(organization.getEmployeesCount());
            existingOrganization.setRating(organization.getRating());
            existingOrganization.setType(organization.getType());

            // Обновляем связанные объекты
            if (organization.getCoordinates() != null) {
                if (organization.getCoordinates().getId() == null) {
                    session.persist(organization.getCoordinates());
                } else {
                    organization.setCoordinates(session.merge(organization.getCoordinates()));
                }
                existingOrganization.setCoordinates(organization.getCoordinates());
            }

            if (organization.getOfficialAddress() != null) {
                if (organization.getOfficialAddress().getId() == null) {
                    session.persist(organization.getOfficialAddress());
                } else {
                    organization.setOfficialAddress(session.merge(organization.getOfficialAddress()));
                }
                existingOrganization.setOfficialAddress(organization.getOfficialAddress());
            }

            if (organization.getPostalAddress() != null) {
                if (organization.getPostalAddress().getId() == null) {
                    session.persist(organization.getPostalAddress());
                } else {
                    organization.setPostalAddress(session.merge(organization.getPostalAddress()));
                }
                existingOrganization.setPostalAddress(organization.getPostalAddress());
            }

            // Проверка уникальности (исключая текущую организацию)
            checkUniqueness(existingOrganization, session, id);

            existingOrganization.validate();

            Organization mergedOrganization = session.merge(existingOrganization);
            transaction.commit();

            return mergedOrganization;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка обновления организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public boolean deleteWithTransaction(Integer id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            });
            transaction = session.beginTransaction();

            Organization organization = session.createQuery(
                            "SELECT o FROM Organization o " +
                                    "LEFT JOIN FETCH o.coordinates " +
                                    "LEFT JOIN FETCH o.officialAddress " +
                                    "LEFT JOIN FETCH o.postalAddress " +
                                    "WHERE o.id = :id", Organization.class)
                    .setParameter("id", id)
                    .uniqueResult();

            if (organization != null) {
                // Отсоединяем связанные объекты перед удалением
                organization.setCoordinates(null);
                organization.setOfficialAddress(null);
                organization.setPostalAddress(null);
                session.remove(organization);
                transaction.commit();
                return true;
            } else {
                transaction.commit();
                return false;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка удаления организации: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    /**
     * Проверка уникальности организации по трем парам полей
     */
    /**
     * Проверка уникальности организации по тройке полей
     */
    private void checkUniqueness(Organization organization, Session session, Integer excludeId) {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        // Проверяем, что все три поля заполнены
        if (name == null || coords == null || address == null) {
            throw new IllegalArgumentException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        StringBuilder whereClause = new StringBuilder();
        if (excludeId != null) {
            whereClause.append("o.id != :excludeId");
        } else {
            whereClause.append("1=1");
        }

        // Проверка уникальности по тройке полей
        String hql;
        if (address.getId() != null) {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE " + whereClause +
                    " AND o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.id = :addressId";
        } else {
            hql = "SELECT COUNT(o) > 0 FROM Organization o WHERE " + whereClause +
                    " AND o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.street = :street " +
                    "AND (o.postalAddress.zipCode = :zipCode OR (o.postalAddress.zipCode IS NULL AND :zipCode IS NULL))";
        }

        Query<Boolean> query = session.createQuery(hql, Boolean.class);
        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }
        query.setParameter("name", name)
                .setParameter("x", coords.getX())
                .setParameter("y", coords.getY());

        if (address.getId() != null) {
            query.setParameter("addressId", address.getId());
        } else {
            query.setParameter("street", address.getStreet())
                    .setParameter("zipCode", address.getZipCode());
        }

        if (query.uniqueResult()) {
            throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже существует");
        }
    }
}