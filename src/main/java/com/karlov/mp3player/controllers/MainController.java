package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
    @FXML
    ImageView iwVolumeImage;
    @FXML
    JFXSlider slVolumeSlider;
    @FXML
    ImageView iwPlayPause;

    private FXMLLoader fxmlLoader = new FXMLLoader();
    private Stage mainStage;
    private Stage addPlaylistStage;
    private AddPlaylistController addPlaylistController;
    private Parent fxmlAddPlaylist;
    private ObservableList<Playlist> playlistObservableList = FXCollections.observableArrayList();
    private int currentPlaylist;
    private MediaPlayer mediaPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListeners();
    }

    private void initializeListeners() {
        lwSongs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) onSongSelected(newValue.intValue());
        });

        slVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> onVolumeChanged(oldValue.intValue(), newValue.intValue()));
    }

    private void onVolumeChanged(int oldValue, int newValue) {
        if (newValue == 0)
            iwVolumeImage.setImage(new Image("images/volume_off.png"));
        if (oldValue == 0 && newValue > 0)
            iwVolumeImage.setImage(new Image("images/volume_on.png"));
    }

    private void onSongSelected(int selectedSong) {
        Track track = playlistObservableList.get(currentPlaylist).getTrackObservableList().get(selectedSong);
        changeSongInformation(track);
        playSong(getFileURL(track.getPath()));
    }

    private String getFileURL(String path) {
        String cleanURL = cleanURL(path);

        return "file:///" + cleanURL;
    }

    private static String cleanURL(String url) {
        url = url.replace("\\", "/");
        url = url.replaceAll(" ", "%20");
        url = url.replace("[", "%5B");
        url = url.replace("]", "%5D");
        return url;
    }

    private void playSong(String fileURL) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer(new Media(fileURL));
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> onSongEnding());
    }

    private void onSongEnding() {
        int index = lwSongs.getSelectionModel().getSelectedIndex();
        if (index != playlistObservableList.get(currentPlaylist).getTrackObservableList().size() - 1)
            lwSongs.getSelectionModel().select(index + 1);
        else lwSongs.getSelectionModel().select(-1);
    }

    private void changeSongInformation(Track track) {
        lbSongTitle.setText(track.getTitle());
        lbSongArtist.setText(track.getArtist());
        lbSongAlbum.setText(track.getAlbum());
        lbSongDuration.setText("0:00/" + track.getLength());
        Image image = new Image(new ByteArrayInputStream(track.getImage()));
        iwAlbumImage.setImage(image);
        iwPlayPause.setImage(new Image("images/pause.png"));
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void onPlayPauseClick(MouseEvent mouseEvent) {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            iwPlayPause.setImage(new Image("images/play.png"));
            mediaPlayer.pause();
        }
        else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            iwPlayPause.setImage(new Image("images/pause.png"));
            mediaPlayer.play();
        }
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

    public void onVolumeClicked(MouseEvent mouseEvent) {
        if (slVolumeSlider.getValue() == 0) {
            iwVolumeImage.setImage(new Image("images/volume_on.png"));
            slVolumeSlider.setValue(50);
        } else {
            slVolumeSlider.setValue(0);
            iwVolumeImage.setImage(new Image("images/volume_off.png"));
        }
    }
}