package com.danp1t.repository;

import com.danp1t.bean.Coordinates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@ApplicationScoped
public class CoordinatesRepository {

    @Inject
    private EntityManager entityManager;

    public Coordinates save(Coordinates coordinates) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(coordinates);
            transaction.commit();
            return coordinates;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving coordinates", e);
        }
    }

    public Coordinates findById(Long id) {
        return entityManager.find(Coordinates.class, id);
    }

    public List<Coordinates> findAll() {
        return entityManager.createQuery("SELECT c FROM Coordinates c", Coordinates.class)
                .getResultList();
    }

    public Coordinates update(Coordinates coordinates) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            Coordinates mergedCoordinates = entityManager.merge(coordinates);
            transaction.commit();
            return mergedCoordinates;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating coordinates", e);
        }
    }

    public void delete(Coordinates coordinates) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(entityManager.contains(coordinates) ? coordinates : entityManager.merge(coordinates));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting coordinates", e);
        }
    }
}