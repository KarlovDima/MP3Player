package com.karlov.mp3player.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.karlov.mp3player.models.Playlist;
import com.karlov.mp3player.utills.MP3Chooser;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
        playlist = MP3Chooser.getPlaylistFromDirectory(getStage(actionEvent));
        if (playlist == null)
            return;
        if (tfPlaylistName.getText() == null || tfPlaylistName.getText().equals(""))
            setPlaylistNameToTextField(playlist.getName());
    }

    private Stage getStage(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        return (Stage) node.getScene().getWindow();
    }

    private void setPlaylistNameToTextField(String name) {
        tfPlaylistName.setText(name);
    }

    @FXML
    public void onSaveButtonClick(ActionEvent actionEvent) {
        if (playlist == null || playlist.getTracksArrayList() == null) {
            showDialog();
        } else {
            playlist.setName(tfPlaylistName.getText());
            closeDialog(actionEvent);
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
        playlist = null;
        closeDialog(actionEvent);
    }

    private void closeDialog(Event event) {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.hide();
    }
}