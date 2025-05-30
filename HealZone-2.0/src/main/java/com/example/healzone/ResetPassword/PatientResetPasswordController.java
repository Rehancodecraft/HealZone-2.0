package com.example.healzone.ResetPassword;

import com.example.healzone.Patient.Patient;
import com.example.healzone.StartView.MainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.awt.*;

import static com.example.healzone.Checks.ChecksForPatient.isPasswordMatch;
import static com.example.healzone.Checks.ChecksForPatient.isPasswordValid;
import static com.example.healzone.DatabaseConnection.Patients.resetPassword;

public class PatientResetPasswordController {
    @FXML
    private TextField password;
    @FXML
    private TextField confirmPassword;
    @FXML
    private Label resetPasswordErrorMessage;
    @FXML
    private void onClickResetPasswordButton(ActionEvent event) {
        Patient.setPassword(password.getText());
        Patient.setConfirmPassword(confirmPassword.getText());
        if (!isPasswordMatch()) {
            resetPasswordErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!isPasswordValid()) {
            resetPasswordErrorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else {
            resetPassword(Patient.getPassword(), Patient.getEmail());
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");
                if (mainController != null) {
                    mainController.loadPatientLogin();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading PatientLogin: ");
                e.printStackTrace();
            }
        }
    }
}
