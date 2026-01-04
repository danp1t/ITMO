package com.danp1t.repository;

import com.danp1t.model.ImportOperation;
import com.danp1t.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
}