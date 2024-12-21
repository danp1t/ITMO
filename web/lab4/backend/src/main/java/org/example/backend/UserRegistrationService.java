package org.example.backend;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Stateless
public class UserRegistrationService {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("lab4");

    public void registerUser(String login, String password) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Users user = new Users();
        user.setLogin(login);
        user.setPassword(password);
        em.persist(user);
        em.getTransaction().commit();
    }
}
