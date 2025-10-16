package com.danp1t.repository;

import com.danp1t.bean.Coordinates;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

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
            throw new RuntimeException("Error saving coordinates: " + e.getMessage(), e);
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
            throw new RuntimeException("Error updating coordinates", e);
        } finally {
            session.close();
        }
    }

    public void delete(Coordinates coordinates) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Coordinates attachedCoordinates = session.contains(coordinates)
                    ? coordinates
                    : session.merge(coordinates);
            session.remove(attachedCoordinates);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting coordinates", e);
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
            throw new RuntimeException("Error deleting coordinates by id", e);
        } finally {
            session.close();
        }
    }
}