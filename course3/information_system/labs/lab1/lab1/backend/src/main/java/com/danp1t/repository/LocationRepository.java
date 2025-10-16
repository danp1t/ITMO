package com.danp1t.repository;

import com.danp1t.bean.Location;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class LocationRepository {

    @Inject
    private SessionFactory sessionFactory;

    public Location save(Location location) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(location);
            transaction.commit();
            return location;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving location", e);
        } finally {
            session.close();
        }
    }

    public Location findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Location.class, id);
        }
    }

    public List<Location> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Location", Location.class).list();
        }
    }

    public Location update(Location location) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Location mergedLocation = session.merge(location);
            transaction.commit();
            return mergedLocation;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating location", e);
        } finally {
            session.close();
        }
    }

    public void delete(Location location) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Location attachedLocation = session.contains(location)
                    ? location
                    : session.merge(location);
            session.remove(attachedLocation);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting location", e);
        } finally {
            session.close();
        }
    }

    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Location location = session.get(Location.class, id);
            if (location != null) {
                session.remove(location);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting location by id", e);
        } finally {
            session.close();
        }
    }
}