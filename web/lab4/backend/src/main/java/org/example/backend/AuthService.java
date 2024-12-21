package org.example.backend;


import jakarta.ejb.Stateless;
import jakarta.persistence.*;

@Stateless
public class AuthService {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("lab4");

    public Users    authenticate(String login, String password) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.login = :login", Users.class);
        query.setParameter("login", login);
        Users user = query.getSingleResult();
        System.out.println(password);
        System.out.println(user.getPassword());
        if (user != null && Users.checkPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }


}

