package com.example.healzone.ShowAlert;

import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.util.Duration;

public class ShowAlert {
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(ShowAlert.class.getResource("/com/example/healzone/Design/Alert.css").toExternalForm());

        alert.show();
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }
}
