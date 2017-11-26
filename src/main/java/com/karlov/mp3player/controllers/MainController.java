package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    ImageView ivAddPlaylist;
    @FXML
    JFXListView<AnchorPane> lwSongs;
    @FXML
    JFXTextField tfPlaylistName;
    @FXML
    Label lbSongTitle;
    @FXML
    Label lbSongArtist;
    @FXML
    Label lbSongAlbum;
    @FXML
    Label lbSongDuration;
    @FXML
    ImageView iwAlbumImage;

    private FXMLLoader fxmlLoader = new FXMLLoader();
    private Stage mainStage;
    private Stage addPlaylistStage;
    private AddPlaylistController addPlaylistController;
    private Parent fxmlAddPlaylist;
    private ObservableList<Playlist> playlistObservableList = FXCollections.observableArrayList();
    private int currentPlaylist;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListeners();
    }

    private void initializeListeners() {
        lwSongs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) onSongSelected(newValue.intValue());
        });
    }

    private void onSongSelected(int selectedSong) {
        Track track = playlistObservableList.get(currentPlaylist).getTrackObservableList().get(selectedSong);
        changeSongInformation(track);
    }

    private void changeSongInformation(Track track) {
        lbSongTitle.setText(track.getTitle());
        lbSongArtist.setText(track.getArtist());
        lbSongAlbum.setText(track.getAlbum());
        lbSongDuration.setText("0:00/" + track.getLength());
        Image image = new Image(new ByteArrayInputStream(track.getImage()));
        iwAlbumImage.setImage(image);
    }

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
        currentPlaylist = playlistObservableList.size() - 1;
    }

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(name);
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

    public void onPreviousPlaylistClick(MouseEvent mouseEvent) {
        if (playlistObservableList.size() < 2)
            return;
        if (currentPlaylist - 1 == -1)
            currentPlaylist = playlistObservableList.size() - 1;
        else
            currentPlaylist--;

        loadPlaylist();
    }

    public void onNextPlaylistClick(MouseEvent mouseEvent) {
        if (playlistObservableList.size() < 2)
            return;
        if (currentPlaylist + 1 == playlistObservableList.size())
            currentPlaylist = 0;
        else
            currentPlaylist++;

        loadPlaylist();
    }

    private void loadPlaylist() {
        ObservableList<Track> tracks = playlistObservableList.get(currentPlaylist).getTrackObservableList();
        setPlaylistNameToTextField(playlistObservableList.get(currentPlaylist).getName());
        fillPlaylist(tracks);
    }

    public void onDeletePlaylistClick(MouseEvent mouseEvent) {
        if (playlistObservableList.size() == 0)
            return;

        playlistObservableList.remove(currentPlaylist);

        if (playlistObservableList.size() == 0) {
            clearPlaylistInformation();
            return;
        }
        if (currentPlaylist == playlistObservableList.size())
            currentPlaylist--;

        loadPlaylist();
    }

    private void clearPlaylistInformation() {
        tfPlaylistName.setText("");
        lwSongs.getItems().clear();
    }
}