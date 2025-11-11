package com.danp1t.repository;

import com.danp1t.model.Organization;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    private SessionFactory sessionFactory;

    public Organization save(Organization organization) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
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
}