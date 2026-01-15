package com.danp1t.repository;

import com.danp1t.model.Address;
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
            throw new RuntimeException("Ошибка сохранения адреса: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    public Address findById(Long id) {

        try (Session session = sessionFactory.openSession()) {
            return session.get(Address.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска адреса: " + e.getMessage(), e);
        }
    }

    public List<Address> findAll() {

        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Address", Address.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска адресов: " + e.getMessage(), e);
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
            throw new RuntimeException("Ошибка обноваления адреса: " + e.getMessage(), e);
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
            throw new RuntimeException("Ошибка удаления адреса по ID: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }
}