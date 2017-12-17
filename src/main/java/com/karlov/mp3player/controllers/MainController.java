package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.dao.DAOFactory;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import com.karlov.mp3player.utills.HibernateConnection;
import com.karlov.mp3player.utills.MP3Chooser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
    ImageView ivAddTrack;
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
    private List<Playlist> playlistArrayList = new ArrayList<>();
    private Playlist currentPlaylist;
    private MediaPlayer mediaPlayer;
    private boolean isRepeat = false;
    private boolean isShuffle = false;
    private List<Integer> shuffledIndexes = new ArrayList<>();
    private Track currentTrack;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListeners();
        loadFromDatabase();
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

        tfPlaylistName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                if (currentPlaylist != null)
                    if (!currentPlaylist.getName().equals(tfPlaylistName.getText())) {
                        currentPlaylist.setName(tfPlaylistName.getText());
                        updateDataBase();
                    }
        });
    }

    private void loadFromDatabase() {
        playlistArrayList = DAOFactory.getInstance().getPlaylistDAO().getAllPlaylists();
        if (playlistArrayList.size() == 0)
            return;
        currentPlaylist = playlistArrayList.get(0);

        tfPlaylistName.setDisable(false);
        loadPlaylist();
    }

    private void onSongSelected(int selectedSong) {
        changeSeekBarDisableProperty(false);
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

    private void changeSeekBarDisableProperty(boolean isDisabled) {
        slSongTime.setDisable(isDisabled);
        pbSongTime.setDisable(isDisabled);
    }

    private void setCurrentTrack(int index) {
        currentTrack = currentPlaylist.getTracksArrayList().get(index);
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
        setNullMediaPlayerIfNecessary();
        mediaPlayer = new MediaPlayer(new Media(fileURL));
        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            mediaPlayer.currentTimeProperty().addListener((observableValue, duration, current) -> {
                if (!slSongTime.isPressed()) {
                    slSongTime.setValue(current.toSeconds());
                    setProgress();
                }
                if (mediaPlayer != null)
                    updateTrackLengthLabel();
            });
            slSongTime.setMax(currentTrack.getLength());
        });
        mediaPlayer.setOnEndOfMedia(this::onSongEnding);
    }

    private void setNullMediaPlayerIfNecessary() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
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
            onShuffledSongEnding(index);
            return;
        }

        if (index != currentPlaylist.getTracksArrayList().size() - 1)
            lwSongs.getSelectionModel().select(index + 1);
        else {
            lwSongs.getSelectionModel().select(-1);
            clearSongInformation();
        }
    }

    private void onShuffledSongEnding(int index) {
        int nextTrackIndex = shuffledIndexes.indexOf(index);
        if (nextTrackIndex + 1 == shuffledIndexes.size()) {
            lwSongs.getSelectionModel().select(-1);
            clearSongInformation();
            return;
        }
        int nextTrack = shuffledIndexes.get(nextTrackIndex + 1);
        lwSongs.getSelectionModel().select(nextTrack);
    }

    private void clearSongInformation() {
        lbSongTitle.setText("");
        lbSongArtist.setText("");
        lbSongAlbum.setText("");
        lbSongYear.setText("");
        iwAlbumImage.setImage(null);
        iwPlayPause.setImage(new Image("images/play.png"));
        pbSongTime.setProgress(0);
        slSongTime.setValue(0);
        lbSongDuration.setText("");
        changeSeekBarDisableProperty(true);
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        mainStage.setOnCloseRequest(event -> HibernateConnection.getSessionFactory().close());
    }

    public void onPlayPauseClick() {
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

    public void onPreviousSongClick() {
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

    public void onNextSongClick() {
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

    public void onRepeatSongClick() {
        if (isRepeat) {
            iwRepeatSong.setImage(new Image("images/repeat_song.png"));
            isRepeat = false;
        } else {
            iwRepeatSong.setImage(new Image("images/repeat_song_activated.png"));
            isRepeat = true;
        }
    }

    public void onShuffleSongsClick() {
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

    public void onAddTrackClick(MouseEvent mouseEvent) {
        Playlist playlist = MP3Chooser.getPlaylistFromFiles(getStage(mouseEvent));
        if (playlist == null)
            return;
        tfPlaylistName.setDisable(false);
        loadNewPlaylist(playlist);
        clearShuffle();
    }

    private Stage getStage(MouseEvent mouseEvent) {
        Node node = (Node) mouseEvent.getSource();
        return (Stage) node.getScene().getWindow();
    }

    public void onAddPlaylistClick() {
        loadAddPlaylistFXML();
        addPlaylistController.setPlaylist(new Playlist());
        showAddPlaylistDialog();
        Playlist playlist = addPlaylistController.getPlaylist();
        if (playlist == null || playlist.getTracksArrayList() == null)
            return;
        tfPlaylistName.setDisable(false);
        loadNewPlaylist(playlist);
        clearShuffle();
    }

    private void loadNewPlaylist(Playlist playlist) {
        addPlaylist(playlist);
        saveToDataBase();
        List<Track> tracks = currentPlaylist.getTracksArrayList();
        if (tracks == null)
            return;
        setPlaylistNameToTextField(currentPlaylist.getName());
        fillPlaylist(tracks);
    }

    private void addPlaylist(Playlist playlist) {
        playlistArrayList.add(playlist);
        currentPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
    }

    private void saveToDataBase() {
        DAOFactory.getInstance().getPlaylistDAO().addPlaylist(currentPlaylist);
        DAOFactory.getInstance().getTrackDAO().addTracks(currentPlaylist.getTracksArrayList());
    }

    private void updateDataBase() {
        DAOFactory.getInstance().getPlaylistDAO().updatePlaylist(currentPlaylist);
    }

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(name);
    }

    private void fillPlaylist(List<Track> tracks) {
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

    public void onPreviousPlaylistClick() {
        if (playlistArrayList.size() < 2)
            return;
        lbSongTitle.requestFocus();
        if (playlistArrayList.indexOf(currentPlaylist) == 0)
            currentPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
        else
            currentPlaylist = playlistArrayList.get(playlistArrayList.indexOf(currentPlaylist) - 1);

        loadPlaylist();
    }

    public void onNextPlaylistClick() {
        if (playlistArrayList.size() < 2)
            return;
        lbSongTitle.requestFocus();
        if (playlistArrayList.indexOf(currentPlaylist) + 1 == playlistArrayList.size())
            currentPlaylist = playlistArrayList.get(0);
        else
            currentPlaylist = playlistArrayList.get(playlistArrayList.indexOf(currentPlaylist) + 1);

        loadPlaylist();
    }

    private void loadPlaylist() {
        List<Track> tracks = currentPlaylist.getTracksArrayList();
        setPlaylistNameToTextField(currentPlaylist.getName());
        fillPlaylist(tracks);
    }

    public void onDeletePlaylistClick() {
        if (playlistArrayList.size() == 0)
            return;
        int index = playlistArrayList.indexOf(currentPlaylist);

        deleteFromDatabase();
        playlistArrayList.remove(currentPlaylist);

        if (playlistArrayList.size() == 0) {
            setNullMediaPlayerIfNecessary();
            currentPlaylist = null;
            currentTrack = null;
            clearSongInformation();
            clearPlaylistInformation();
            return;
        }

        if (index == playlistArrayList.size())
            currentPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
        else
            currentPlaylist = playlistArrayList.get(index);

        loadPlaylist();
    }

    private void deleteFromDatabase() {
        DAOFactory.getInstance().getPlaylistDAO().deletePlaylist(currentPlaylist);
    }

    private void clearPlaylistInformation() {
        tfPlaylistName.setText("");
        tfPlaylistName.setDisable(true);
        lwSongs.getItems().clear();
    }

    public void onVolumeClicked() {
        if (slVolume.getValue() == 0) {
            iwVolumeImage.setImage(new Image("images/volume_on.png"));
            slVolume.setValue(50);
        } else {
            slVolume.setValue(0);
            iwVolumeImage.setImage(new Image("images/volume_off.png"));
        }
    }
}