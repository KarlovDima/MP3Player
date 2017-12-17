package com.karlov.mp3player.dao.implementation;

import com.karlov.mp3player.dao.PlaylistDAO;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.utills.HibernateConnection;
import org.hibernate.Session;

import java.util.List;

public class PlaylistDAOImplementation implements PlaylistDAO {
    @Override
    public void addPlaylist(Playlist playlist) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.save(playlist);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void updatePlaylist(Playlist playlist) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.update(playlist);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public Playlist getPlaylistById(int playlistId) {
        Playlist playlist = null;
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            playlist = session.get(Playlist.class, playlistId);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return playlist;
    }

    @Override
    public void deletePlaylist(Playlist playlist) {
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            session.delete(playlist);
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = null;
        try (Session session = HibernateConnection.getSessionFactory().getCurrentSession()) {
            session.beginTransaction();
            playlists = session.createQuery("from Playlist").getResultList();
            session.getTransaction().commit();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return playlists;
    }
}
