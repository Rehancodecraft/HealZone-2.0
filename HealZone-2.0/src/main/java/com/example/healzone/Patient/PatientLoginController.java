package com.example.healzone.Patient;

import com.example.healzone.*;
import com.example.healzone.StartView.MainViewController;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;


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
    private Label patientLoginErrorMessage;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private CheckBox showPasswordBox;
    @FXML
    private void initialize() {
        // Sync content when typing
        password.textProperty().bindBidirectional(visiblePasswordField.textProperty());


        // Hide visiblePasswordField initially
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        showPasswordBox.setOnAction(event -> {
            if (showPasswordBox.isSelected()) {
                // Show the plain text field
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);

                password.setVisible(false);
                password.setManaged(false);
            } else {
                // Hide the plain text field
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                password.setVisible(true);
                password.setManaged(true);
            }
        });
    }
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
    public void onForgetPasswordLinkClicked(ActionEvent event) throws IOException {
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadPatientEmailVerification();
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
        if (patientEmail.getText().isEmpty() || password.getText().isEmpty()) {
            patientLoginErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        }

        boolean loggedIn = getCurrentPatient(patientEmail.getText(), password.getText());
        if (loggedIn) {
            SessionManager.logIn(Patient.getName());
            patientLoginErrorMessage.setText("⚠️ Logged IN As " + Patient.getName());

            // Load HomePage in a background thread
            new Thread(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/StartView/HomePage.fxml"));
                    Parent homePage = loader.load();

                    Platform.runLater(() -> {
                        homePage.setOpacity(0);
                        homePage.setScaleX(0.98);
                        homePage.setScaleY(0.98);

                        Scene scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(homePage);

                        // Fade + scale animation
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), homePage);
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);

                        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), homePage);
                        scaleIn.setFromX(0.98);
                        scaleIn.setToX(1);
                        scaleIn.setFromY(0.98);
                        scaleIn.setToY(1);

                        new ParallelTransition(fadeIn, scaleIn).play();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> patientLoginErrorMessage.setText("⚠️ Failed to load home page."));
                }
            }).start();
        } else {
            patientLoginErrorMessage.setText("⚠️ Wrong credentials, click forget password or signup");
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
