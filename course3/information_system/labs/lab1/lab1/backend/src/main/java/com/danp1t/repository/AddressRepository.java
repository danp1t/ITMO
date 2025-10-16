package com.danp1t.repository;

import com.danp1t.bean.Address;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AddressRepository {

    @Inject
    private SessionFactory sessionFactory;

    public Address save(Address address) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(address);
            transaction.commit();

            return address;
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving address: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Address findById(Long id) {

        try (Session session = sessionFactory.openSession()) {
            Address address = session.get(Address.class, id);
            return address;
        } catch (Exception e) {
            throw new RuntimeException("Error finding address: " + e.getMessage(), e);
        }
    }

    public List<Address> findAll() {

        try (Session session = sessionFactory.openSession()) {
            List<Address> addresses = session.createQuery("FROM Address", Address.class).list();
            return addresses;
        } catch (Exception e) {
            throw new RuntimeException("Error finding all addresses: " + e.getMessage(), e);
        }
    }

    public Address update(Address address) {

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Address mergedAddress = session.merge(address);
            transaction.commit();

            return mergedAddress;
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating address: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public void delete(Address address) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Address attachedAddress = session.contains(address)
                    ? address
                    : session.merge(address);
            session.remove(attachedAddress);

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting address: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Address address = session.get(Address.class, id);
            if (address != null) {
                session.remove(address);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting address by ID: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
}