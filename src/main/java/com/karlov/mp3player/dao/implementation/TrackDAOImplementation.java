package com.karlov.mp3player.dao.implementation;

import com.karlov.mp3player.dao.TrackDAO;
import com.karlov.mp3player.models.Track;
import com.karlov.mp3player.utills.HibernateConnection;
import org.hibernate.Session;

import java.util.List;

public class TrackDAOImplementation implements TrackDAO {
    @Override
    public void addTrack(Track track) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.save(track);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void addTracks(List<Track> tracks) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            tracks.forEach(session::save);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void updateTrack(Track track) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.update(track);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public Track getTrackById(int trackId) {
        Track track = null;
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            track = session.get(Track.class, trackId);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return track;
    }

    @Override
    public void deleteTrack(Track track) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.delete(track);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public List<Track> getAllTracks() {
        List<Track> tracks = null;
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            tracks = session.createQuery("from Track").getResultList();
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return tracks;
    }
}
