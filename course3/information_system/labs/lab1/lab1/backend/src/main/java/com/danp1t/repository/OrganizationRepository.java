package com.danp1t.repository;

import com.danp1t.bean.Organization;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    private EntityManager entityManager;

    public Organization save(Organization organization) {
        EntityTransaction transaction = entityManager.getTransaction();
        System.out.println(organization);
        try {
            transaction.begin();

            if (organization.getCoordinates().getId() == null) {
                entityManager.persist(organization.getCoordinates());
            } else {
                organization.setCoordinates(entityManager.merge(organization.getCoordinates()));
            }

            if (organization.getOfficialAddress().getId() == null) {
                entityManager.persist(organization.getOfficialAddress());
            }
            else {
                organization.setOfficialAddress(entityManager.merge(organization.getOfficialAddress()));
            }
            if (organization.getPostalAddress().getId() == null) {
                entityManager.persist(organization.getPostalAddress());
            }
            else {
                organization.setPostalAddress(entityManager.merge(organization.getPostalAddress()));
            }

            entityManager.persist(organization);
            transaction.commit();
            return organization;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving organization", e);
        }
    }

    public List<Organization> findAll() {
        return entityManager.createQuery(
                        "SELECT o FROM Organization o " +
                                "LEFT JOIN FETCH o.coordinates " +
                                "LEFT JOIN FETCH o.officialAddress " +
                                "LEFT JOIN FETCH o.postalAddress", Organization.class)
                .getResultList();
    }

    public Organization findById(Integer id) {
        return entityManager.createQuery(
                        "SELECT o FROM Organization o " +
                                "LEFT JOIN FETCH o.coordinates " +
                                "LEFT JOIN FETCH o.officialAddress " +
                                "LEFT JOIN FETCH o.postalAddress " +
                                "WHERE o.id = :id", Organization.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public void delete(Organization organization) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(entityManager.contains(organization) ? organization : entityManager.merge(organization));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting organization", e);
        }
    }
}