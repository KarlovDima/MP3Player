package com.karlov.mp3player.dao;

import com.karlov.mp3player.dao.implementation.PlaylistDAOImplementation;
import com.karlov.mp3player.dao.implementation.TrackDAOImplementation;

public class DAOFactory {
    private static TrackDAO trackDAO;
    private static PlaylistDAO playlistDAO;
    private static DAOFactory daoFactory;

    public static synchronized DAOFactory getInstance() {
        if (daoFactory == null)
            daoFactory = new DAOFactory();
        return daoFactory;
    }

    public TrackDAO getTrackDAO() {
        if (trackDAO == null) {
            trackDAO = new TrackDAOImplementation();
        }
        return trackDAO;
    }

    public PlaylistDAO getPlaylistDAO() {
        if (playlistDAO == null) {
            playlistDAO = new PlaylistDAOImplementation();
        }
        return playlistDAO;
    }
}
