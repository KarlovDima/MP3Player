package com.karlov.mp3player.utills;

import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateConnection {
    private static SessionFactory sessionFactory;

    private HibernateConnection() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null)
            sessionFactory = new Configuration()
                    .configure("hibernate/hibernate.cfg.xml")
                    .addAnnotatedClass(Playlist.class)
                    .addAnnotatedClass(Track.class)
                    .buildSessionFactory();
        return sessionFactory;
    }
}
