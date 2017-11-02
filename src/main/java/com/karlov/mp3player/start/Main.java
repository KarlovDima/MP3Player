package com.karlov.mp3player.start;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxmls/main.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root,907, 459));
        primaryStage.setMaxHeight(495);
        primaryStage.setMinHeight(495);
        primaryStage.setMinWidth(925);
        primaryStage.setMaxWidth(1200);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}

