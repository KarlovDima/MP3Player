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
import java.util.*;

public class MainController implements Initializable {
    @FXML
    ImageView ivAddPlaylist;
    @FXML
    ImageView ivAddTrack;
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
    @FXML
    AnchorPane apSongListViewParent;

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
    private Map<Playlist, JFXListView<AnchorPane>> songsMap = new HashMap<>();
    private Playlist switchedPlaylist;

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        mainStage.setOnCloseRequest(event -> HibernateConnection.getSessionFactory().close());
        setLogo(mainStage);
    }

    private void setLogo(Stage stage) {
        Image image = new Image("images/logo.png");
        stage.getIcons().add(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListeners();
        loadFromDatabase();
    }

    private void initializeListeners() {
        slVolume.valueProperty().addListener((observable, oldValue, newValue) -> onVolumeChanged(oldValue.intValue(), newValue.intValue()));

        slSongTime.valueProperty().addListener(observable -> {
            if (slSongTime.isPressed())
                onTimeChanged();
        });
        tfPlaylistName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                if (switchedPlaylist != null)
                    if (!switchedPlaylist.getName().equals(tfPlaylistName.getText())) {
                        switchedPlaylist.setName(tfPlaylistName.getText());
                        updateDataBase();
                    }
        });
    }

    private void loadFromDatabase() {
        playlistArrayList = DAOFactory.getInstance().getPlaylistDAO().getAllPlaylists();
        if (playlistArrayList.size() == 0)
            return;

        currentPlaylist = playlistArrayList.get(0);
        switchedPlaylist = playlistArrayList.get(0);

        tfPlaylistName.setDisable(false);
        loadPlaylist();
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

    private void setProgress() {
        pbSongTime.setProgress(slSongTime.getValue() / slSongTime.getMax());
    }

    private void updateTrackLengthLabel() {
        String currentTime = getLength((int) slSongTime.getValue());
        String totalTime = getLength(currentTrack.getLength());
        lbSongDuration.setText(currentTime + "/" + totalTime);
    }

    private String getLength(int lengthInSeconds) {
        int minutes = lengthInSeconds / 60;
        int seconds = lengthInSeconds - minutes * 60;
        if (seconds < 10)
            return minutes + ":0" + seconds;
        return minutes + ":" + seconds;
    }

    private void updateDataBase() {
        DAOFactory.getInstance().getPlaylistDAO().updatePlaylist(currentPlaylist);
    }

    private void onSongSelected(int selectedSong) {
        changeSeekBarDisableProperty(false);
        setCurrentTrack(selectedSong);
        clearSelection();
        setCurrentPlaylist();
        if (currentTrack != null && isShuffle && currentTrack.getPlaylist() != currentPlaylist)
            shuffleSongs();
        changeSongInformation();
        playSong(getFileURL(currentTrack.getPath()));
        setVolume(slVolume.getValue() / 100);
    }

    private void changeSeekBarDisableProperty(boolean isDisabled) {
        slSongTime.setDisable(isDisabled);
        pbSongTime.setDisable(isDisabled);
    }

    private void setCurrentTrack(int index) {
        if (songsMap.get(switchedPlaylist).getSelectionModel().isEmpty())
            currentTrack = currentPlaylist.getTracksArrayList().get(index);
        else
            currentTrack = switchedPlaylist.getTracksArrayList().get(index);
    }

    private void clearSelection() {
        if (currentTrack != null && currentTrack.getPlaylist() != currentPlaylist)
            songsMap.get(currentPlaylist).getSelectionModel().clearSelection();
    }

    private void setCurrentPlaylist() {
        currentPlaylist = currentTrack.getPlaylist();
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

    private void onSongEnding() {
        JFXListView<AnchorPane> listView = songsMap.get(currentPlaylist);
        int index = listView.getSelectionModel().getSelectedIndex();

        if (isRepeat) {
            playSong(getFileURL(currentTrack.getPath()));
            return;
        }
        if (isShuffle) {
            onShuffledSongEnding(index);
            return;
        }
        if (index != currentPlaylist.getTracksArrayList().size() - 1)
            listView.getSelectionModel().select(index + 1);
        else {
            listView.getSelectionModel().select(-1);
            clearSongInformation();
        }
    }

    private void onShuffledSongEnding(int index) {
        JFXListView<AnchorPane> listView = songsMap.get(currentPlaylist);
        int nextTrackIndex = shuffledIndexes.indexOf(index);
        if (nextTrackIndex + 1 == shuffledIndexes.size()) {
            listView.getSelectionModel().select(-1);
            clearSongInformation();
            return;
        }
        int nextTrack = shuffledIndexes.get(nextTrackIndex + 1);
        listView.getSelectionModel().select(nextTrack);
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
        JFXListView<AnchorPane> listView = songsMap.get(currentPlaylist);
        if (listView == null)
            return;
        int currentTrackIndex = listView.getSelectionModel().getSelectedIndex();

        if (isShuffle) {
            int previousTrackIndex = shuffledIndexes.indexOf(currentTrackIndex);
            if (previousTrackIndex - 1 == -1)
                return;
            int previousTrack = shuffledIndexes.get(previousTrackIndex - 1);
            listView.getSelectionModel().select(previousTrack);
            return;
        }

        if (currentTrackIndex == 0)
            return;
        listView.getSelectionModel().select(currentTrackIndex - 1);
    }

    public void onNextSongClick() {
        JFXListView<AnchorPane> listView = songsMap.get(currentPlaylist);
        if (listView == null)
            return;
        int currentTrackIndex = listView.getSelectionModel().getSelectedIndex();

        if (isShuffle) {
            int nextTrackIndex = shuffledIndexes.indexOf(currentTrackIndex);
            if (nextTrackIndex + 1 == shuffledIndexes.size())
                return;
            int nextTrack = shuffledIndexes.get(nextTrackIndex + 1);
            listView.getSelectionModel().select(nextTrack);
            return;
        }

        if (currentTrackIndex == listView.getItems().size() - 1 || currentTrackIndex == -1)
            return;
        listView.getSelectionModel().select(currentTrackIndex + 1);
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
        JFXListView<AnchorPane> listView = songsMap.get(switchedPlaylist);
        if (listView == null)
            return;
        if (isShuffle)
            clearShuffle();
        else {
            iwShuffleSongs.setImage(new Image("images/shuffle_song_activated.png"));
            isShuffle = true;
            if (listView.getItems().size() == 0)
                return;
            shuffleSongs();
        }
    }

    private void clearShuffle() {
        iwShuffleSongs.setImage(new Image("images/shuffle_song.png"));
        isShuffle = false;
    }

    private void shuffleSongs() {
        shuffledIndexes.clear();
        JFXListView<AnchorPane> listView = songsMap.get(switchedPlaylist);
        int size = listView.getItems().size();
        for (int i = 0; i < size; i++)
            shuffledIndexes.add(i);

        Collections.shuffle(shuffledIndexes);

        int currentTrack = listView.getSelectionModel().getSelectedIndex();
        if (currentTrack == -1) {
            listView.getSelectionModel().select(0);
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
    }

    private void loadAddPlaylistFXML() {
        try {
            if (fxmlAddPlaylist == null) {
                fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxmls/add_playlist_dialog.fxml"));
                fxmlAddPlaylist = fxmlLoader.load();
                addPlaylistController = fxmlLoader.getController();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            setLogo(addPlaylistStage);
        }
        addPlaylistStage.showAndWait();
    }

    private void loadNewPlaylist(Playlist playlist) {
        addPlaylist(playlist);
        saveToDataBase();
        List<Track> tracks = switchedPlaylist.getTracksArrayList();
        if (tracks == null)
            return;
        setPlaylistNameToTextField(switchedPlaylist.getName());
        fillPlaylist(tracks);
    }

    private void addPlaylist(Playlist playlist) {
        playlistArrayList.add(playlist);
        if (currentPlaylist == null)
            currentPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
        switchedPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);

    }

    private void saveToDataBase() {
        DAOFactory.getInstance().getPlaylistDAO().addPlaylist(switchedPlaylist);
        DAOFactory.getInstance().getTrackDAO().addTracks(switchedPlaylist.getTracksArrayList());
    }

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(name);
    }

    private void fillPlaylist(List<Track> tracks) {
        JFXListView<AnchorPane> listView = songsMap.get(switchedPlaylist);

        if (listView == null) {
            createListView();
            listView = songsMap.get(switchedPlaylist);
            fillTracks(tracks, listView);
        } else
            setListView();
    }

    private void fillTracks(List<Track> tracks, JFXListView<AnchorPane> listView) {
        for (Track track : tracks) {
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getChildren().add(getSongNumberLabel());
            anchorPane.getChildren().add(getSongLabel(track));
            anchorPane.getChildren().add(getSongLengthLabel(track));
            AnchorPane.setLeftAnchor(anchorPane.getChildren().get(0), 1.0);
            AnchorPane.setLeftAnchor(anchorPane.getChildren().get(1), 25.0);
            AnchorPane.setRightAnchor(anchorPane.getChildren().get(1), 150.0);
            AnchorPane.setRightAnchor(anchorPane.getChildren().get(2), 1.0);
            listView.getItems().add(anchorPane);
        }
    }

    private void setListView() {
        apSongListViewParent.getChildren().forEach(node -> node.setVisible(false));
        songsMap.get(switchedPlaylist).setVisible(true);
    }

    private void createListView() {
        JFXListView<AnchorPane> listView = new JFXListView<>();
        listView.setPrefHeight(388);
        listView.setPrefWidth(466);
        listView.setStyle("-fx-background-color: a54a51");
        listView.getStylesheets().add("css/listview.css");
        AnchorPane.setLeftAnchor(listView, 0.0);
        AnchorPane.setRightAnchor(listView, 0.0);

        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) onSongSelected(newValue.intValue());
        });

        apSongListViewParent.getChildren().forEach(node -> node.setVisible(false));
        apSongListViewParent.getChildren().add(listView);
        songsMap.put(switchedPlaylist, listView);
    }

    private Label getSongNumberLabel() {
        Label number = new Label(songsMap.get(switchedPlaylist).getItems().size() + 1 + ". ");
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

    public void onPreviousPlaylistClick() {
        if (playlistArrayList.size() < 2)
            return;
        lbSongTitle.requestFocus();
        if (playlistArrayList.indexOf(switchedPlaylist) == 0)
            switchedPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
        else
            switchedPlaylist = playlistArrayList.get(playlistArrayList.indexOf(switchedPlaylist) - 1);

        loadPlaylist();
    }

    public void onNextPlaylistClick() {
        if (playlistArrayList.size() < 2)
            return;
        lbSongTitle.requestFocus();
        if (playlistArrayList.indexOf(switchedPlaylist) + 1 == playlistArrayList.size())
            switchedPlaylist = playlistArrayList.get(0);
        else
            switchedPlaylist = playlistArrayList.get(playlistArrayList.indexOf(switchedPlaylist) + 1);

        loadPlaylist();
    }

    private void loadPlaylist() {
        List<Track> tracks = switchedPlaylist.getTracksArrayList();
        setPlaylistNameToTextField(switchedPlaylist.getName());
        fillPlaylist(tracks);
    }

    public void onDeletePlaylistClick() {
        if (playlistArrayList.size() == 0)
            return;

        if (mediaPlayer != null)
            if ((mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING || mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED)
                    && switchedPlaylist.getId() == currentTrack.getPlaylist().getId()) {
                setNullMediaPlayerIfNecessary();
                currentTrack = null;
                clearSongInformation();
            }

        int index = playlistArrayList.indexOf(switchedPlaylist);

        deleteFromDatabase();
        playlistArrayList.remove(switchedPlaylist);

        if (playlistArrayList.size() == 0) {
            deleteLastPlaylist();
            return;
        }

        if (index == playlistArrayList.size())
            switchedPlaylist = playlistArrayList.get(playlistArrayList.size() - 1);
        else
            switchedPlaylist = playlistArrayList.get(index);

        loadPlaylist();
    }

    private void deleteLastPlaylist() {
        setNullMediaPlayerIfNecessary();
        currentPlaylist = null;
        switchedPlaylist = null;
        currentTrack = null;
        clearSongInformation();
        clearPlaylistInformation();
    }

    private void deleteFromDatabase() {
        DAOFactory.getInstance().getPlaylistDAO().deletePlaylist(switchedPlaylist);
    }

    private void clearPlaylistInformation() {
        tfPlaylistName.setText("");
        tfPlaylistName.setDisable(true);
        songsMap.clear();
        apSongListViewParent.getChildren().clear();
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