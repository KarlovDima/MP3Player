package com.karlov.mp3player.start;

import com.karlov.mp3player.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("fxmls/main.fxml"));
        Parent fxmlMain = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();
        mainController.setMainStage(primaryStage);

        primaryStage.setTitle("MP3 Player");
        primaryStage.setScene(new Scene(fxmlMain, 971, 459));
        primaryStage.setMaxHeight(495);
        primaryStage.setMinHeight(495);
        primaryStage.setMinWidth(987);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

