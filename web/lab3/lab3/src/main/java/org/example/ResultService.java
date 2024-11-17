package org.example;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ResultService {

    @PersistenceContext(unitName = "myPersistenceUnit")
    private EntityManager entityManager;

    public void saveResult(ResultBean result) {
        entityManager.persist(result);
    }
}
