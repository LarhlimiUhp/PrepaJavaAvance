package com.examen.stock.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            String host = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String url = "jdbc:mysql://" + host + ":3306/gestion_stock";

            Configuration configuration = new Configuration().configure();
            configuration.setProperty("hibernate.connection.url", url);

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
