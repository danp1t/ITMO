package com.danp1t.service;

import com.danp1t.model.User;
import com.danp1t.dto.AuthRequestDTO;
import com.danp1t.dto.AuthResponseDTO;
import com.danp1t.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private SessionFactory sessionFactory;

    public AuthResponseDTO register(AuthRequestDTO request) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            User existingUser = session.createQuery(
                            "FROM User WHERE username = :username", User.class)
                    .setParameter("username", request.getUsername())
                    .uniqueResult();

            if (existingUser != null) {
                throw new RuntimeException("Пользователь с таким именем уже существует");
            }

            // Создаем нового пользователя
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.validate();

            session.persist(user);
            session.flush();

            String token = generateToken(user.getId());

            transaction.commit();

            return new AuthResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getRole(),
                    token
            );

        } catch (RuntimeException e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Ошибка при откате транзакции: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Ошибка при регистрации пользователя", e);
        } finally {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception e) {
                    System.err.println("Ошибка при закрытии сессии: " + e.getMessage());
                }
            }
        }
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        String token = generateToken(user.getId());

        return new AuthResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        );
    }

    public User getUserFromToken(String token) {
        try {
            if (token == null || !token.startsWith("user_")) {
                return null;
            }

            String[] parts = token.split("_");

            if (parts.length < 2) {
                return null;
            }

            String userIdStr = parts[1];
            Integer userId = Integer.parseInt(userIdStr);

            return userRepository.findById(userId);
        } catch (Exception e) {
            System.err.println("Error parsing token: " + token + ", error: " + e.getMessage());
            return null;
        }
    }


    private String generateToken(Integer userId) {
        return "user_" + userId + "_" + UUID.randomUUID().toString();
    }
}