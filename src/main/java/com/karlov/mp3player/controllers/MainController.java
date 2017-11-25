package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    ImageView ivAddPlaylist;

    @FXML
    JFXListView<AnchorPane> lwSongs;

    @FXML
    JFXTextField tfPlaylistName;

    private FXMLLoader fxmlLoader = new FXMLLoader();
    private Stage mainStage;
    private Stage addPlaylistStage;
    private AddPlaylistController addPlaylistController;
    private Parent fxmlAddPlaylist;
    private ObservableList<Playlist> playlistObservableList = FXCollections.observableArrayList();

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void onAddPlaylistClick(MouseEvent mouseEvent) {
        loadAddPlaylistFXML();
        addPlaylistController.setPlaylist(new Playlist());
        showAddPlaylistDialog();
        loadNewPlaylist();
    }

    private void loadNewPlaylist() {
        addPlaylist(addPlaylistController.getPlaylist());
        ObservableList<Track> tracks = playlistObservableList.get(playlistObservableList.size() - 1).getTrackObservableList();
        if (tracks == null)
            return;
        setPlaylistNameToTextField(playlistObservableList.get(playlistObservableList.size() - 1).getName());
        fillPlaylist(tracks);
    }

    private void addPlaylist(Playlist playlist) {
        playlistObservableList.add(addPlaylistController.getPlaylist());
    }

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(playlistObservableList.get(playlistObservableList.size() - 1).getName());
    }

    private void fillPlaylist(ObservableList<Track> tracks) {
        clearSongsInListView();

        for (Track track : tracks) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getChildren().add(getSongNumberLabel());
            anchorPane.getChildren().add(getSongLabel(track));
            anchorPane.getChildren().add(getSongLengthLabel(track));
            AnchorPane.setLeftAnchor(anchorPane.getChildren().get(0), 1.0);
            AnchorPane.setLeftAnchor(anchorPane.getChildren().get(1), 25.0);
            AnchorPane.setRightAnchor(anchorPane.getChildren().get(1), 150.0);
            AnchorPane.setRightAnchor(anchorPane.getChildren().get(2), 1.0);
            lwSongs.getItems().add(anchorPane);
        }
    }

    private void clearSongsInListView() {
        lwSongs.getItems().clear();
    }

    private Label getSongNumberLabel() {
        Label number = new Label(lwSongs.getItems().size() + 1 + ". ");
        number.setFont(Font.font("Tahoma Regular", 17));
        number.setTextFill(Color.WHITE);

        return number;
    }

    private Label getSongLabel(Track track) {
        Label song = new Label(track.getTitle());
        song.setFont(Font.font("Tahoma Regular", 17));
        song.setTextFill(Color.WHITE);
        song.setMaxWidth(100);

        return song;
    }

    private Label getSongLengthLabel(Track track) {
        Label length = new Label(track.getLength());
        length.setFont(Font.font("Tahoma Regular", 17));
        length.setTextFill(Color.WHITE);

        return length;
    }

    private void loadAddPlaylistFXML() {
        try {
            if (fxmlAddPlaylist == null) {
                setFXMLLoaderLocation("add_playlist_dialog.fxml");
                fxmlAddPlaylist = fxmlLoader.load();
                addPlaylistController = fxmlLoader.getController();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFXMLLoaderLocation(String filename) {
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxmls/" + filename));
    }

    private void showAddPlaylistDialog() {
        if (addPlaylistStage == null) {
            addPlaylistStage = new Stage();
            addPlaylistStage.setTitle("New Playlist");
            addPlaylistStage.setMinHeight(151);
            addPlaylistStage.setMinWidth(441);
            addPlaylistStage.setResizable(false);
            addPlaylistStage.setScene(new Scene(fxmlAddPlaylist));
            addPlaylistStage.initModality(Modality.WINDOW_MODAL);
            addPlaylistStage.initOwner(mainStage);
        }
        addPlaylistStage.showAndWait();
    }
}