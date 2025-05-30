package com.example.healzone.ResetPassword;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.StartView.MainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.awt.*;

import static com.example.healzone.Checks.ChecksForDoctor.isPasswordMatch;
import static com.example.healzone.Checks.ChecksForDoctor.isPasswordValid;
import static com.example.healzone.DatabaseConnection.Doctors.resetPassword;

public class DoctorResetPasswordController {
    @FXML
    private TextField password;
    @FXML
    private TextField confirmPassword;
    @FXML
    private Label resetPasswordErrorMessage;
    @FXML
    private void onClickResetPasswordButton(ActionEvent event) {
        Doctor.setPassword(password.getText());
        Doctor.setConfirmPassword(confirmPassword.getText());
        if (!isPasswordMatch()) {
            resetPasswordErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!isPasswordValid()) {
            resetPasswordErrorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else {
            resetPassword(Doctor.getPassword(), Doctor.getEmail());
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");
                if (mainController != null) {
                    mainController.loadDoctorLogin();
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
