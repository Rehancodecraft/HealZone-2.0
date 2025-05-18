package com.example.healzone;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class MainViewController {

    @FXML
    private StackPane contentPane;
    @FXML
    private ToggleGroup loginToggleGroup;

    public void initialize() {
        System.out.println("Inside initialize: contentPane = " + contentPane);
        Platform.runLater(() -> loadView("/com/example/healzone/Patient/PatientLogin.fxml"));
    }

    public void loadPatientLogin() {
        loadView("/com/example/healzone/Patient/PatientLogin.fxml");
    }

    public void loadDoctorLogin() {
        loadView("/com/example/healzone/Doctor/DoctorLogin.fxml");
    }

    public void loadPatientSignup() {
        loadView("/com/example/healzone/Patient/PatientSignUp.fxml");
    }

    public void loadDoctorSignup() {
        loadView("/com/example/healzone/Doctor/DoctorSignUp.fxml");
    }

    protected void loadView(String fxmlPath) {
        try {
            if (contentPane == null) {
                System.err.println("contentPane is null in loadView!");
                return;
            }

            // Load the new view
            Parent newView = FXMLLoader.load(getClass().getResource(fxmlPath));

            // Set initial opacity and scale
            newView.setOpacity(0);
            newView.setScaleX(0.98);
            newView.setScaleY(0.98);

            // Add to contentPane
            contentPane.getChildren().setAll(newView);

            // Fade + scale animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), newView);
            scaleIn.setFromX(0.98);
            scaleIn.setToX(1);
            scaleIn.setFromY(0.98);
            scaleIn.setToY(1);

            // Play both together
            ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
            transition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setContent(String fxmlPath) {
        try {
            if (contentPane == null) {
                System.err.println("contentPane is null in setContent!");
                return;
            }
            Parent node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Failed to set content: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
