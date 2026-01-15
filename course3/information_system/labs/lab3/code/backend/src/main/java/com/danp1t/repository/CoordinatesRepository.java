package com.danp1t.repository;

import com.danp1t.model.Coordinates;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CoordinatesRepository {


    @Inject
    private SessionFactory sessionFactory;

    public Coordinates save(Coordinates coordinates) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(coordinates);
            transaction.commit();

            return coordinates;
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка сохранения координат: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Coordinates findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Coordinates.class, id);
        }
    }

    public List<Coordinates> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Coordinates", Coordinates.class).list();
        }
    }

    public Coordinates update(Coordinates coordinates) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Coordinates mergedCoordinates = session.merge(coordinates);
            transaction.commit();
            return mergedCoordinates;
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка обновления координат:", e);
        } finally {
            session.close();
        }
    }

    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Coordinates coordinates = session.get(Coordinates.class, id);
            if (coordinates != null) {
                session.remove(coordinates);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка удаления координат по ID:", e);
        } finally {
            session.close();
        }
    }
}