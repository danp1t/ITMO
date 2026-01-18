package com.danp1t.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class HibernateSessionProducer {

    private static SessionFactory sessionFactory;
    private static final List<Session> activeSessions = new ArrayList<>();
    private static boolean simulateDBFailure = false;

    @ApplicationScoped
    @Produces
    public SessionFactory createSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            StandardServiceRegistry registry = null;
            try {
                String dbUsername = "s409425";

                if (simulateDBFailure) {
                    dbUsername = "wrong_user_" + System.currentTimeMillis();
                }

                StandardServiceRegistryBuilder registryBuilder =
                        new StandardServiceRegistryBuilder()
                                .configure("hibernate.cfg.xml");

                if (dbUsername != null && !dbUsername.isEmpty()) {
                    registryBuilder.applySetting("hibernate.connection.username", dbUsername);
                }

                registry = registryBuilder.build();

                MetadataSources metadataSources = new MetadataSources(registry);

                String[] mappingFiles = {
                        "com/danp1t/model/Address.hbm.xml",
                        "com/danp1t/model/Coordinates.hbm.xml",
                        "com/danp1t/model/Location.hbm.xml",
                        "com/danp1t/model/Organization.hbm.xml",
                        "com/danp1t/model/User.hbm.xml",
                        "com/danp1t/model/ImportOperation.hbm.xml"
                };

                for (String mappingFile : mappingFiles) {
                    metadataSources.addResource(mappingFile);
                }

                sessionFactory = metadataSources
                        .buildMetadata()
                        .buildSessionFactory();

            } catch (Exception e) {
                e.printStackTrace();
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                throw new RuntimeException("Ошибка запуска Hibernate: " + e.getMessage(), e);
            }
        }
        return sessionFactory;
    }

    @RequestScoped
    @Produces
    public Session createSession(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        synchronized (activeSessions) {
            activeSessions.add(session);
        }
        return session;
    }

    @PreDestroy
    public void close() {
        closeSessionFactory();
    }

    public static void simulateDBFailure(boolean simulate) {
        simulateDBFailure = simulate;
    }

    public static void resetSessionFactory() {
        closeAllSessions();
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        sessionFactory = null;
    }

    public static void closeAllSessions() {
        synchronized (activeSessions) {
            for (Session session : activeSessions) {
                try {
                    if (session != null && session.isOpen()) {
                        session.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            activeSessions.clear();
        }

        if (sessionFactory != null) {
            try {
                sessionFactory.getStatistics().clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeSessionFactory() {
        closeAllSessions();
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}