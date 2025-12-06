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

    public ImportOperation save(ImportOperation operation) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.persist(operation);
            transaction.commit();
            return operation;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка сохранения операции импорта: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void merge(ImportOperation operation) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            operation = session.merge(operation); // Всегда merge
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка слияния операции импорта: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
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

    public ImportOperation findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(ImportOperation.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска операции импорта: " + e.getMessage(), e);
        }
    }
}