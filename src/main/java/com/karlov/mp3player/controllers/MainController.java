package com.karlov.mp3player.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    ImageView ivAddPlaylist;

    private FXMLLoader fxmlLoader = new FXMLLoader();
    private Stage mainStage;
    private Stage addPlaylistStage;
    private Parent fxmlAddPlaylist;

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void onAddPlaylistClick(MouseEvent mouseEvent) {
        loadAddPlaylistFXML();
        showAddPlaylistDialog();
    }

    private void loadAddPlaylistFXML() {
        setFXMLLoaderLocation("add_playlist_dialog.fxml");

        try {
            if (fxmlAddPlaylist == null)
                fxmlAddPlaylist = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFXMLLoaderLocation(String filename) {
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxmls/" + filename));
    }

    private void showAddPlaylistDialog() {
        if(addPlaylistStage==null) {
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