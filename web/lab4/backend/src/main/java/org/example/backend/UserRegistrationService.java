package org.example.backend;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserRegistrationService {

    @PersistenceContext(unitName = "your-persistence-unit")
    private EntityManager entityManager;

    public void registerUser(String login, String password) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        entityManager.persist(user);
    }
}
