package com.danp1t.repository;

import com.danp1t.model.ImportOperation;
import com.danp1t.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ImportOperationRepository {

    @Inject
    private SessionFactory sessionFactory;

    public ImportOperation save(ImportOperation operation) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(operation);
            session.getTransaction().commit();
            return operation;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения операции импорта: " + e.getMessage(), e);
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
                    "FROM ImportOperation i WHERE i.user = :user ORDER BY i.importDate DESC",
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