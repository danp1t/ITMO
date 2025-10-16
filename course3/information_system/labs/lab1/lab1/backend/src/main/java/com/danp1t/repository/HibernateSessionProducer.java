package com.danp1t.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import java.util.logging.Logger;

@ApplicationScoped
public class HibernateSessionProducer {

    private static final Logger logger = Logger.getLogger(HibernateSessionProducer.class.getName());
    private SessionFactory sessionFactory;

    @ApplicationScoped
    @Produces
    public SessionFactory createSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = null;
            try {
                logger.info("Starting Hibernate initialization...");

                // Создаем реестр сервисов Hibernate
                registry = new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml")
                        .build();

                logger.info("StandardServiceRegistry created successfully");

                // Создаем MetadataSources
                MetadataSources metadataSources = new MetadataSources(registry);

                // Добавляем mapping файлы по одному с логированием
                String[] mappingFiles = {
                        "com/danp1t/bean/Address.hbm.xml",
                        "com/danp1t/bean/Coordinates.hbm.xml",
                        "com/danp1t/bean/Location.hbm.xml",
                        "com/danp1t/bean/Organization.hbm.xml"
                };

                for (String mappingFile : mappingFiles) {
                    logger.info("Adding mapping file: " + mappingFile);
                    metadataSources.addResource(mappingFile);
                }

                logger.info("Building SessionFactory...");
                sessionFactory = metadataSources
                        .buildMetadata()
                        .buildSessionFactory();

                logger.info("Hibernate SessionFactory initialized successfully!");

            } catch (Exception e) {
                logger.severe("FAILED to initialize Hibernate: " + e.getMessage());
                e.printStackTrace();

                // В случае ошибки уничтожаем реестр
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
                throw new RuntimeException("Failed to initialize Hibernate: " + e.getMessage(), e);
            }
        }
        return sessionFactory;
    }

    @RequestScoped
    @Produces
    public Session createSession(SessionFactory sessionFactory) {
        return sessionFactory.openSession();
    }

    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed");
        }
    }
}