package org.example;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ResultService {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("lab3_123");


    public void saveResult(ResultBean result) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(result);
        em.getTransaction().commit();
    }

}
