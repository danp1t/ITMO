package com.danp1t.repository;

import com.danp1t.bean.Coordinates;
import com.danp1t.bean.Location;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@ApplicationScoped
public class LocationRepository {

    @Inject
    private EntityManager entityManager;

    public Location save(Location location) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(location);
            transaction.commit();
            return location;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving location", e);
        }
    }

    public Location findById(Long id) {
        return entityManager.find(Location.class, id);
    }

    public List<Location> findAll() {
        return entityManager.createQuery("SELECT c FROM Location c", Location.class)
                .getResultList();
    }

    public void delete(Location location) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(entityManager.contains(location) ? location : entityManager.merge(location));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting location", e);
        }
    }
}