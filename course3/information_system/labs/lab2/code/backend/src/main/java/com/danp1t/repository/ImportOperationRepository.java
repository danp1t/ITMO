package com.danp1t.repository;

import com.danp1t.model.ImportOperation;
import com.danp1t.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ImportOperationRepository {

    @Inject
    private SessionFactory sessionFactory;

    @Transactional
    public ImportOperation save(ImportOperation operation) {
        try {
            Session session = sessionFactory.getCurrentSession();
            session.persist(operation);
            return operation;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения операции импорта: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<ImportOperation> findAll() {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<ImportOperation> query = session.createQuery(
                    "FROM ImportOperation i LEFT JOIN FETCH i.user ORDER BY i.importDate DESC",
                    ImportOperation.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения операций импорта: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<ImportOperation> findByUser(User user) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<ImportOperation> query = session.createQuery(
                    "FROM ImportOperation i WHERE i.user = :user ORDER BY i.importDate DESC",
                    ImportOperation.class);
            query.setParameter("user", user);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения операций импорта пользователя: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ImportOperation findById(Long id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            return session.get(ImportOperation.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска операции импорта: " + e.getMessage(), e);
        }
    }
}