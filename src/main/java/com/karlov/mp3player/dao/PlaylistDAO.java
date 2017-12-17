package com.karlov.mp3player.dao;

import com.karlov.mp3player.models.Playlist;

import java.util.List;

public interface PlaylistDAO {
    void addPlaylist(Playlist playlist);

    void updatePlaylist(Playlist playlist);

    Playlist getPlaylistById(int playlistId);

    void deletePlaylist(Playlist playlist);

    List<Playlist> getAllPlaylists();
}
