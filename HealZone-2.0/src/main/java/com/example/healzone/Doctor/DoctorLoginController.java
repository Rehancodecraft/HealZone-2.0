package com.example.healzone.Doctor;

import com.example.healzone.Patient.Patient;
import com.example.healzone.SessionManager;
import com.example.healzone.StartView.MainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import static com.example.healzone.Doctor.LoginDoctor.getCurrentDoctor;
import static com.example.healzone.Patient.LoginPatient.getCurrentPatient;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;

public class DoctorLoginController {
    @FXML
    private TextField doctorEmail;
    @FXML
    private TextField password;
    @FXML
    private Label doctorLoginErrorMessage;
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
