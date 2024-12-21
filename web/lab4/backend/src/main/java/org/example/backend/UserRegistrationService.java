package org.example.backend;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;

@Stateless
public class UserRegistrationService {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("lab4");

    public void registerUser(String login, String password) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Users user = new Users();
            user.setLogin(login);
            user.setPassword(password);
            em.persist(user);
            em.getTransaction().commit();

        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new IllegalArgumentException("Логин уже существует. Пожалуйста, выберите другой.");

        } finally {
            em.close();
        }
    }

}
