package com.example.healzone.Patient;

import com.example.healzone.*;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Parent;



import java.io.IOException;

import static com.example.healzone.Patient.LoginPatient.getCurrentPatient;



public class PatientLoginController  {
    @FXML
    private TextField patientEmail;
    @FXML
    private TextField password;
    @FXML
    private Button loginButton;
    @FXML
    public void onSignUpLinkClicked(ActionEvent event) throws IOException {
//
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadPatientSignup();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
    @FXML
    public void onLoginButtonClicked(ActionEvent event) throws IOException {
        boolean loggedIn = getCurrentPatient(patientEmail.getText(), password.getText());
        if (loggedIn) {
            SessionManager.logIn(Patient.getName());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login Successful");
            closeLogInUpStage();

        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Login Failed");
        }
    }

    private void closeLogInUpStage() {
        // Get the current window (Stage) from the sign-up button
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();  // Close the current stage
    }
    private void showAlert(Alert.AlertType type, String title, String message) {Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);  // Optional: no header
        alert.setContentText(message);


        // Adding a custom stylesheet to the alert
        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());

        // Show the alert without blocking further execution
        alert.show();
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> alert.close());  // Close the alert after 5 seconds
        pause.play();

        // Create a PauseTransition to wait for 5 seconds (5000 milliseconds)

    }

}
