package com.danp1t.repository;

import com.danp1t.bean.Address;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@ApplicationScoped
public class AddressRepository {

    @Inject
    private EntityManager entityManager;

    public Address save(Address address) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(address);
            transaction.commit();
            return address;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving address", e);
        }
    }

    public Address findById(Long id) {
        return entityManager.find(Address.class, id);
    }

    public List<Address> findAll() {
        return entityManager.createQuery("SELECT c FROM Address c", Address.class)
                .getResultList();
    }

    public void delete(Address address) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(entityManager.contains(address) ? address : entityManager.merge(address));
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting address", e);
        }
    }
}