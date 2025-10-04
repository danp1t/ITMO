package org.example.backend;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;


@Stateless
public class ResultService {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("lab4");

    public void registerUser(Double x, Double y, Double r, Boolean hit) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Result result = new Result();
            result.setX(x);
            result.setY(y);
            result.setR(r);
            result.setHit(hit);
            em.persist(result);
            em.getTransaction().commit();

        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new IllegalArgumentException("Ошибка при сохранении результата");

        } finally {
            em.close();
        }
    }

}
