package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.models.Track;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
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
    @FXML
    StackPane stackPane;

    private Playlist playlist;

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        tfPlaylistName.setText(playlist.getName());
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    @FXML
    public void onPathButtonClick(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();

        File selectedDirectory = getSelectedDirectory(stage);

        if (selectedDirectory == null)
            return;

        if (tfPlaylistName.getText().equals(""))
            setPlaylistNameToTextField(selectedDirectory.getName());
        File[] mp3Files = getFilesInDirectory(selectedDirectory);
        playlist.setPath(selectedDirectory.getPath());
        playlist.setTrackObservableList(getTracksObservableList(mp3Files));
    }

    private ObservableList<Track> getTracksObservableList(File[] files) {
        ObservableList<Track> tracks = FXCollections.observableArrayList();
        Track track;
        ID3v2 id3v2Tag;
        for (File file : files) {
            try {
                Mp3File mp3File = new Mp3File(file);
                id3v2Tag = mp3File.getId3v2Tag();
                track = new Track();
                track.setAlbum(id3v2Tag.getAlbum());
                track.setArtist(id3v2Tag.getArtist());
                track.setTitle(id3v2Tag.getTitle());
                track.setPath(file.getPath());
                track.setYear(Integer.parseInt(id3v2Tag.getYear()));
                track.setImage(id3v2Tag.getAlbumImage());
                track.setLength((int) mp3File.getLengthInSeconds());
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

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(name);
    }

    @FXML
    public void onSaveButtonClick(ActionEvent actionEvent) {
        if (playlist.getPath() == null) {
            showDialog();
        } else {
            playlist.setName(tfPlaylistName.getText());
            onCancelButtonClick(actionEvent);
        }
    }

    private void showDialog() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setPrefWidth(150);
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);

        content.setHeading(new Text("Error"));
        content.setBody(new Text("Choose the directory with your music"));
        JFXButton button = new JFXButton("OK");
        button.setOnAction(event -> dialog.close());
        content.setActions(button);
        dialog.show();
    }

    @FXML
    public void onCancelButtonClick(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.hide();
    }
}