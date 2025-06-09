package com.example.healzone.Doctor;

import com.example.healzone.Patient.Patient;
import com.example.healzone.SessionManager;
import com.example.healzone.StartView.MainViewController;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

import static com.example.healzone.Doctor.LoginDoctor.getCurrentDoctor;

public class DoctorLoginController {
    @FXML
    private TextField doctorEmail;
    @FXML
    private TextField password;
    @FXML
    private Label doctorLoginErrorMessage;
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
                mainController.loadDoctorPersonalDetails();
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
        if (doctorEmail.getText().isEmpty() || password.getText().isEmpty()) {
            doctorLoginErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        } else {

            boolean loggedIn = getCurrentDoctor(doctorEmail.getText(), password.getText());
            if (loggedIn) {
                SessionManager.logIn(Patient.getName());
//                showAlert(Alert.AlertType.INFORMATION, "Success", "Login Successful");
                  doctorLoginErrorMessage.setText("Logged In successfully"+Doctor.getFirstName()+" "+Doctor.getLastName());
                System.out.println("LoggedIn");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/StartView/HomePage.fxml"));
                Parent homePage = loader.load();
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

                ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
                transition.play();
            } else {
                doctorLoginErrorMessage.setText("⚠️ Wrong credentials, click forget password or signup");
            }
        }
    }
    @FXML
    public void onForgetPasswordLinkClicked(ActionEvent event) throws IOException {
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorEmailVerification();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
}
