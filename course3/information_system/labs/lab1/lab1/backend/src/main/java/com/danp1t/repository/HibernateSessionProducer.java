package com.danp1t.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class HibernateSessionProducer {

    private SessionFactory sessionFactory;

    @ApplicationScoped
    @Produces
    public SessionFactory createSessionFactory() {
        if (sessionFactory == null) {
            StandardServiceRegistry registry = null;
            try {

                registry = new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml")
                        .build();


                MetadataSources metadataSources = new MetadataSources(registry);

                String[] mappingFiles = {
                        "com/danp1t/model/Address.hbm.xml",
                        "com/danp1t/model/Coordinates.hbm.xml",
                        "com/danp1t/model/Location.hbm.xml",
                        "com/danp1t/model/Organization.hbm.xml"
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
                throw new RuntimeException("Ошибка запуска Hibernate " + e.getMessage(), e);
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
        }
    }
}