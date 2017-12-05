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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
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
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    Label lbSongYear;
    @FXML
    Label lbSongDuration;
    @FXML
    ImageView iwAlbumImage;
    @FXML
    ImageView iwVolumeImage;
    @FXML
    JFXSlider slVolume;
    @FXML
    ImageView iwPlayPause;
    @FXML
    ImageView iwPreviousSong;
    @FXML
    ImageView iwNextSong;
    @FXML
    ImageView iwRepeatSong;
    @FXML
    ImageView iwShuffleSongs;
    @FXML
    Slider slSongTime;
    @FXML
    ProgressBar pbSongTime;

    private FXMLLoader fxmlLoader = new FXMLLoader();
    private Stage mainStage;
    private Stage addPlaylistStage;
    private AddPlaylistController addPlaylistController;
    private Parent fxmlAddPlaylist;
    private ObservableList<Playlist> playlistObservableList = FXCollections.observableArrayList();
    private int currentPlaylist;
    private MediaPlayer mediaPlayer;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private List<Integer> shuffledIndexes = new ArrayList<>();
    private Track currentTrack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListeners();
    }

    private void initializeListeners() {
        lwSongs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) onSongSelected(newValue.intValue());
        });
        slVolume.valueProperty().addListener((observable, oldValue, newValue) -> onVolumeChanged(oldValue.intValue(), newValue.intValue()));
        slSongTime.valueProperty().addListener(observable -> {
            if (slSongTime.isPressed())
                onTimeChanged();
        });
    }

    private void onSongSelected(int selectedSong) {
        changeDisableProperty(false);
        setCurrentTrack(selectedSong);
        changeSongInformation();
        playSong(getFileURL(currentTrack.getPath()));
        setVolume(slVolume.getValue() / 100);
    }

    private void onVolumeChanged(int oldValue, int newValue) {
        setVolume(slVolume.getValue() / 100);
        if (newValue == 0) {
            iwVolumeImage.setImage(new Image("images/volume_off.png"));
        }
        if (oldValue == 0 && newValue > 0) {
            iwVolumeImage.setImage(new Image("images/volume_on.png"));
        }
    }

    private void onTimeChanged() {
        mediaPlayer.seek(Duration.seconds(slSongTime.getValue()));
        setProgress();
        updateTrackLengthLabel();
    }

    private void changeDisableProperty(boolean isDisabled) {
        slSongTime.setDisable(isDisabled);
        pbSongTime.setDisable(isDisabled);
    }

    private void setCurrentTrack(int index) {
        currentTrack = playlistObservableList.get(currentPlaylist).getTrackObservableList().get(index);
    }

    private void changeSongInformation() {
        lbSongTitle.setText(currentTrack.getTitle());
        lbSongArtist.setText(currentTrack.getArtist());
        lbSongAlbum.setText(currentTrack.getAlbum());
        lbSongYear.setText(currentTrack.getYear() + "");
        Image image = new Image(new ByteArrayInputStream(currentTrack.getImage()));
        iwAlbumImage.setImage(image);
        iwPlayPause.setImage(new Image("images/pause.png"));
    }

    private void playSong(String fileURL) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer(new Media(fileURL));

        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            mediaPlayer.currentTimeProperty().addListener((observableValue, duration, current) -> {
                if (!slSongTime.isPressed()) {
                    slSongTime.setValue(current.toSeconds());
                    setProgress();
                }
                updateTrackLengthLabel();
            });
            slSongTime.setMax(currentTrack.getLength());
        });
        mediaPlayer.setOnEndOfMedia(this::onSongEnding);
    }

    private void setVolume(double value) {
        if (mediaPlayer != null)
            mediaPlayer.setVolume(value);
    }

    private void setProgress() {
        pbSongTime.setProgress(slSongTime.getValue() / slSongTime.getMax());
    }

    private void updateTrackLengthLabel() {
        String currentTime = getLength((int) slSongTime.getValue());
        String totalTime = getLength(currentTrack.getLength());
        lbSongDuration.setText(currentTime + "/" + totalTime);
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

    private String getLength(int lengthInSeconds) {
        int minutes = lengthInSeconds / 60;
        int seconds = lengthInSeconds - minutes * 60;
        if (seconds < 10)
            return minutes + ":0" + seconds;
        return minutes + ":" + seconds;
    }

    private void onSongEnding() {
        int index = lwSongs.getSelectionModel().getSelectedIndex();

        if (isRepeat) {
            onSongSelected(index);
            return;
        }
        if (isShuffle) {
            int nextTrackIndex = shuffledIndexes.indexOf(index);
            if (nextTrackIndex + 1 == shuffledIndexes.size()) {
                lwSongs.getSelectionModel().select(-1);
                clearSongInformation();
                return;
            }
            int nextTrack = shuffledIndexes.get(nextTrackIndex + 1);
            lwSongs.getSelectionModel().select(nextTrack);
            return;
        }

        if (index != playlistObservableList.get(currentPlaylist).getTrackObservableList().size() - 1)
            lwSongs.getSelectionModel().select(index + 1);
        else {
            lwSongs.getSelectionModel().select(-1);
            clearSongInformation();
        }
    }

    private void clearSongInformation() {
        lbSongTitle.setText("");
        lbSongArtist.setText("");
        lbSongAlbum.setText("");
        lbSongYear.setText("");
        iwAlbumImage.setImage(null);
        iwPlayPause.setImage(new Image("images/play.png"));
        lbSongDuration.setText("");
        pbSongTime.setProgress(0);
        slSongTime.setValue(0);
        changeDisableProperty(true);
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
        } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            iwPlayPause.setImage(new Image("images/pause.png"));
            mediaPlayer.play();
        }
    }

    public void onPreviousSongClick(MouseEvent mouseEvent) {
        int currentTrackIndex = lwSongs.getSelectionModel().getSelectedIndex();

        if (isShuffle) {
            int previousTrackIndex = shuffledIndexes.indexOf(currentTrackIndex);
            if (previousTrackIndex - 1 == -1)
                return;
            int previousTrack = shuffledIndexes.get(previousTrackIndex - 1);
            lwSongs.getSelectionModel().select(previousTrack);
            return;
        }

        if (currentTrackIndex == 0)
            return;
        lwSongs.getSelectionModel().select(currentTrackIndex - 1);
    }

    public void onNextSongClick(MouseEvent mouseEvent) {
        int currentTrackIndex = lwSongs.getSelectionModel().getSelectedIndex();

        if (isShuffle) {
            int nextTrackIndex = shuffledIndexes.indexOf(currentTrackIndex);
            if (nextTrackIndex + 1 == shuffledIndexes.size())
                return;
            int nextTrack = shuffledIndexes.get(nextTrackIndex + 1);
            lwSongs.getSelectionModel().select(nextTrack);
            return;
        }

        if (currentTrackIndex == lwSongs.getItems().size() - 1 || currentTrackIndex == -1)
            return;
        lwSongs.getSelectionModel().select(currentTrackIndex + 1);
    }

    public void onRepeatSongClick(MouseEvent mouseEvent) {
        if (isRepeat) {
            iwRepeatSong.setImage(new Image("images/repeat_song.png"));
            isRepeat = false;
        } else {
            iwRepeatSong.setImage(new Image("images/repeat_song_activated.png"));
            isRepeat = true;
        }
    }

    public void onShuffleSongsClick(MouseEvent mouseEvent) {
        if (isShuffle)
            clearShuffle();
        else {
            iwShuffleSongs.setImage(new Image("images/shuffle_song_activated.png"));
            isShuffle = true;
            if (lwSongs.getItems().size() == 0)
                return;
            shuffleSongs();
        }
    }

    private void clearShuffle() {
        iwShuffleSongs.setImage(new Image("images/shuffle_song.png"));
        isShuffle = false;
        shuffledIndexes.clear();
    }

    private void shuffleSongs() {
        int size = lwSongs.getItems().size();
        for (int i = 0; i < size; i++)
            shuffledIndexes.add(i);

        Collections.shuffle(shuffledIndexes);

        int currentTrack = lwSongs.getSelectionModel().getSelectedIndex();
        if (currentTrack == -1) {
            lwSongs.getSelectionModel().select(0);
            currentTrack = 0;
        }
        shuffledIndexes.remove(shuffledIndexes.indexOf(currentTrack));
        shuffledIndexes.add(0, currentTrack);
    }

    public void onAddPlaylistClick(MouseEvent mouseEvent) {
        loadAddPlaylistFXML();
        addPlaylistController.setPlaylist(new Playlist());
        showAddPlaylistDialog();
        loadNewPlaylist();
        isShuffle = false;
        clearShuffle();
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
        playlistObservableList.add(playlist);
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
        Label length = new Label(getLength(track.getLength()));
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
        if (slVolume.getValue() == 0) {
            iwVolumeImage.setImage(new Image("images/volume_on.png"));
            slVolume.setValue(50);
        } else {
            slVolume.setValue(0);
            iwVolumeImage.setImage(new Image("images/volume_off.png"));
        }
    }
}