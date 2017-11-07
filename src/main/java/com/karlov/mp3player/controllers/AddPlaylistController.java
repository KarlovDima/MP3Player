package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.models.Track;
import com.karlov.mp3player.models.Tracklist;
import com.mpatric.mp3agic.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AddPlaylistController {
    @FXML
    JFXTextField tfPlaylistName;

    @FXML
    JFXButton btnPlaylistPath;

    @FXML
    JFXButton btnSave;

    @FXML
    JFXButton btnCancel;

    Tracklist tracklist = new Tracklist();


    @FXML
    public void onPathButtonClick(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();

        File selectedDirectory = getSelectedDirectory(stage);

        setTracklistNameToTextField(selectedDirectory.getName());
        File[] mp3Files = getFilesInDirectory(selectedDirectory);
        tracklist.setName(selectedDirectory.getName());
        tracklist.setPath(selectedDirectory.getPath());
        tracklist.setTrackObservableList(getTracksObservableList(mp3Files));
    }

    private ObservableList<Track> getTracksObservableList(File[] files){
        ObservableList<Track> tracks = FXCollections.observableArrayList();
        Track track;
        ID3v2 id3v2Tag;
        for (File file:files) {
            try {
                Mp3File mp3File=new Mp3File(file);
                id3v2Tag=mp3File.getId3v2Tag();
                track=new Track();
                track.setAlbum(id3v2Tag.getAlbum());
                track.setArtist(id3v2Tag.getArtist());
                track.setTitle(id3v2Tag.getTitle());
                track.setPath(file.getPath());
                track.setYear(Integer.parseInt(id3v2Tag.getYear()));
                tracks.add(track);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedTagException e) {
                e.printStackTrace();
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        }
        return tracks;
    }

    private File getSelectedDirectory(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Directory selection");
        return chooser.showDialog(stage);
    }

    private File[] getFilesInDirectory(File directory) {
        return directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
    }

    private void setTracklistNameToTextField(String name) {
        tfPlaylistName.setText(name);
    }

    @FXML
    public void onSaveButtonClick(ActionEvent actionEvent) {
    }

    @FXML
    public void onCancelButtonClick(ActionEvent actionEvent) {
    }
}