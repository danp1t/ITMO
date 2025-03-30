package org.example;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ResultService {
    @PersistenceContext(unitName = "lab3_123")
    private EntityManager em;

    public void saveResult(ResultBean result) {
        em.persist(result);
    }
}
