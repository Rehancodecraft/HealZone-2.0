package com.example.healzone;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import static com.example.healzone.DatabaseConnection.connectToDatabase;

public class Main extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            connectToDatabase();

            // Load MainView.fxml and let FXMLLoader create the controller
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
            StackPane root = loader.load();

            // Get the controller instance created by FXMLLoader
            MainViewController controller = loader.getController();

            // Store controller in root's properties for later use
            root.getProperties().put("controller", controller);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    public static void main(String[] args) {
        launch(args); // This launches the JavaFX Application Thread
    }
}
