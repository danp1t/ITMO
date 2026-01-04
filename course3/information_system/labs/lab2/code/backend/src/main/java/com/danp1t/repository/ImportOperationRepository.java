package com.danp1t.repository;

import com.danp1t.model.ImportOperation;
import com.danp1t.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ImportOperationRepository {

    @Inject
    private SessionFactory sessionFactory;

    public void saveWithSession(ImportOperation operation, Session session) {
        session.persist(operation);
    }

    public void mergeWithSession(ImportOperation operation, Session session) {
        session.merge(operation);
    }

    public List<ImportOperation> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<ImportOperation> query = session.createQuery(
                    "FROM ImportOperation i LEFT JOIN FETCH i.user ORDER BY i.importDate DESC",
                    ImportOperation.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения операций импорта: " + e.getMessage(), e);
        }
    }

    public List<ImportOperation> findByUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Query<ImportOperation> query = session.createQuery(
                    "FROM ImportOperation i LEFT JOIN FETCH i.user WHERE i.user = :user ORDER BY i.importDate DESC",
                    ImportOperation.class);
            query.setParameter("user", user);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения операций импорта пользователя: " + e.getMessage(), e);
        }
    }

    public User findUserById(Integer userId, Session session) {
        try {
            return session.get(User.class, userId);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения пользователя по ID: " + e.getMessage(), e);
        }
    }

    public void createFailedImportOperation(User user, String fileName, String errorMessage, Session session) {
        try {
            User managedUser = session.get(User.class, user.getId());

            ImportOperation failedOperation = new ImportOperation();
            failedOperation.setFileName(fileName);
            failedOperation.setImportDate(LocalDateTime.now());
            failedOperation.setUser(managedUser);
            failedOperation.setStatus("FAILED");
            failedOperation.setErrorMessage(errorMessage);

            session.persist(failedOperation);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания записи об ошибке импорта: " + e.getMessage(), e);
        }
    }
}