package com.karlov.mp3player.dao;

import com.karlov.mp3player.models.Track;

import java.util.List;

public interface TrackDAO {
    void addTrack(Track track);

    void addTracks(List<Track> tracks);

    void updateTrack(Track track);

    Track getTrackById(int trackId);

    void deleteTrack(Track track);

    List<Track> getAllTracks();
}
