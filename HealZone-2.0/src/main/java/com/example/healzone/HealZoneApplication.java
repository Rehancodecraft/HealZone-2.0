package com.example.healzone;


import com.example.healzone.controller.shared.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static com.example.healzone.config.DatabaseConfig.connectToDatabase;

public class HealZoneApplication extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            connectToDatabase();

            FXMLLoader loader = new FXMLLoader(HealZoneApplication.class.getResource("StartView/MainView.fxml"));
            StackPane root = loader.load();

            MainViewController controller = loader.getController();
            root.getProperties().put("controller", controller);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    public static void main(String[] args) {
        launch(args); // This launches the JavaFX Application Thread
    }
}
