package com.danp1t.repository;


import com.danp1t.bean.Coordinates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestHibernate {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory("my-persistence-unit");
            em = emf.createEntityManager();

            System.out.println("✅ Hibernate initialized successfully!");

            // Попробуем создать транзакцию
            em.getTransaction().begin();

            // Простая проверка - попытка вставить тестовые данные
            Coordinates coord = new Coordinates();
            coord.setX(10.5f);
            coord.setY(20.7);

            em.persist(coord);
            em.getTransaction().commit();

            System.out.println("✅ Test data inserted successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null) em.close();
            if (emf != null) emf.close();
        }
    }
}