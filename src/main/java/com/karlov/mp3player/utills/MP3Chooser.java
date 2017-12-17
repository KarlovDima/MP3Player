package com.karlov.mp3player.utills;

import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MP3Chooser {
    private static FileChooser fileChooser;
    private static DirectoryChooser directoryChooser;
    private static Playlist playlist;

    private MP3Chooser() {
    }

    public static Playlist getPlaylistFromFiles(Stage stage) {
        playlist = new Playlist();
        fileChooser = new FileChooser();
        configureFileChooser();
        List<File> files = getSelectedFiles(stage);
        if (files == null)
            return null;
        playlist.setName("Untitled");
        playlist.setTracksArrayList(getTracksArrayList(files));

        return playlist;
    }

    private static List<File> getSelectedFiles(Stage stage) {
        return fileChooser.showOpenMultipleDialog(stage);
    }

    private static void configureFileChooser() {
        fileChooser.setTitle("Choose mp3 files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3", "*.mp3"));
    }

    public static Playlist getPlaylistFromDirectory(Stage stage) {
        playlist = new Playlist();
        directoryChooser = new DirectoryChooser();
        configureDirectoryChooser();
        File directory = getSelectedDirectory(stage);
        if (directory == null)
            return null;
        List<File> mp3Files = Arrays.asList(getFilesInDirectory(directory));
        playlist.setName(directory.getName());
        playlist.setTracksArrayList(getTracksArrayList(mp3Files));

        return playlist;
    }

    private static void configureDirectoryChooser() {
        directoryChooser.setTitle("Directory selection");
    }

    private static File getSelectedDirectory(Stage stage) {
        return directoryChooser.showDialog(stage);
    }

    private static File[] getFilesInDirectory(File directory) {
        return directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
    }

    private static List<Track> getTracksArrayList(List<File> files) {
        List<Track> tracks = new ArrayList<>();
        for (File file : files)
            tracks.add(getTrack(file));
        return tracks;
    }

    private static Track getTrack(File file) {
        Track track = new Track();
        try {
            Mp3File mp3File = new Mp3File(file);
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            track.setPlaylist(playlist);
            track.setAlbum(id3v2Tag.getAlbum());
            track.setArtist(id3v2Tag.getArtist());
            track.setTitle(id3v2Tag.getTitle());
            track.setPath(file.getPath());
            track.setYear(Integer.parseInt(id3v2Tag.getYear()));
            track.setImage(id3v2Tag.getAlbumImage());
            track.setLength((int) mp3File.getLengthInSeconds());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        return track;
    }
}
