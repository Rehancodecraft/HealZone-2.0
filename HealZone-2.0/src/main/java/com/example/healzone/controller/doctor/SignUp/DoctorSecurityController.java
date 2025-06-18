package com.example.healzone.controller.doctor.SignUp;

import com.example.healzone.model.DoctorModel;
import com.example.healzone.controller.shared.MainViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctorFields.*;
import static com.example.healzone.model.DoctorModel.*;
import static com.example.healzone.util.EmailSender.receiverEmail;
import static com.example.healzone.util.EmailSender.sendOTPToEmail;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;

public class DoctorSecurityController {
    //Security
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private CheckBox userAgreement;
    @FXML
    private Label securityErrorMessage;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private CheckBox showPasswordBox;
    @FXML
    private TextField visibleConfirmPasswordField;
    @FXML
    private CheckBox showConfirmPasswordBox;
    @FXML
    private void initialize() {
        // Sync content when typing
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(visibleConfirmPasswordField.textProperty());

        // Hide visiblePasswordFields initially
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.setManaged(false);

        // Toggle for Password field
        showPasswordBox.setOnAction(event -> {
            if (showPasswordBox.isSelected()) {
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);

                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        });

        // Toggle for Confirm Password field
        showConfirmPasswordBox.setOnAction(event -> {
            if (showConfirmPasswordBox.isSelected()) {
                visibleConfirmPasswordField.setVisible(true);
                visibleConfirmPasswordField.setManaged(true);

                confirmPasswordField.setVisible(false);
                confirmPasswordField.setManaged(false);
            } else {
                visibleConfirmPasswordField.setVisible(false);
                visibleConfirmPasswordField.setManaged(false);

                confirmPasswordField.setVisible(true);
                confirmPasswordField.setManaged(true);
            }
        });
    }
@FXML
public void onSignUpButtonClicked(ActionEvent event) {
    DoctorModel.setPassword(passwordField.getText());
    DoctorModel.setConfirmPassword(confirmPasswordField.getText());
    if (isSecurityEmpty()) {
        securityErrorMessage.setText("⚠️ Please fill in all fields!");
        return;
    } else if (!isPasswordValid()) {
        securityErrorMessage.setText("⚠️ Password must be at least 8 characters!");
        return;
    } else if (!isPasswordMatch()) {
        securityErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
        return;
    } else if (!userAgreement.isSelected()) {
        securityErrorMessage.setText("⚠️ Please accept the user agreement to continue!");
        return;
    } else {
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                System.out.println(getEmail());
                receiverEmail = getEmail();
                // Load OTP verification view immediately
                mainController.loadOTPVerificationForRegisterDoctor();
                // Send OTP in a background thread
                Task<Void> sendOtpTask = new Task<>() {
                    @Override
                    protected Void call() {
                        sendOTPToEmail(receiverEmail, getFirstName() + getLastName());
                        return null;
                    }
                };
                sendOtpTask.setOnSucceeded(e -> {
//                        showAlert(Alert.AlertType.INFORMATION, "OTP Sent", "OTP has been sent to your email.");
                });
                sendOtpTask.setOnFailed(e -> {
                    // Handle failure
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send OTP. Please try again.");
                    System.err.println("OTP sending failed: " + sendOtpTask.getException());
                });
                new Thread(sendOtpTask).start();
            } else {
                System.err.println("MainViewController not found in root properties.");
                showAlert(Alert.AlertType.ERROR, "Error", "Internal error: Unable to load OTP verification.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
    public void onSecurityBackButtonClicked(javafx.event.ActionEvent event){
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorTimeTable();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
}
