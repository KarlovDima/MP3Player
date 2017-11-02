package com.karlov.mp3player.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private ImageView imageView;

    boolean a = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageView.setOnMouseClicked(event -> {
                    if (a) {
                        imageView.setImage(new Image("images/play.png"));
                        a = false;
                    } else {
                        imageView.setImage(new Image("images/pause.png"));
                        a = true;
                    }
                }
        );
    }
}