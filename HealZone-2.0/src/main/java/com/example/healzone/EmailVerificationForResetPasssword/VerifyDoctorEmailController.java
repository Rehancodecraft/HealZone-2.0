package com.example.healzone.EmailVerificationForResetPasssword;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.EmailVerificationForRegistration.EmailSender;
import com.example.healzone.Patient.Patient;
import com.example.healzone.ResetPassword.PatientResetPasswordController;
import com.example.healzone.StartView.MainViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;


import static com.example.healzone.Checks.ChecksForDoctor.isGovtIDValid;
import static com.example.healzone.DatabaseConnection.Doctors.checkEmail;
import static com.example.healzone.DatabaseConnection.Doctors.checkGovtID;
import static com.example.healzone.EmailVerificationForRegistration.OTPgenerator.generateOTP;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;
import static com.example.healzone.EmailVerificationForRegistration.EmailSender.*;


public class VerifyDoctorEmailController extends PatientResetPasswordController {
    @FXML
    private TextField VerifyEmailField;
    @FXML
    private Label errorMessage;
    @FXML
    private TextField resetPasswordCNIC;

    @FXML
    protected void onOTPSendButtonClicked(ActionEvent event) {
        try {
            Doctor.setEmail(VerifyEmailField.getText());
            Doctor.setGovtID(resetPasswordCNIC.getText());
            if (!isEmailValid(VerifyEmailField.getText())) {
                errorMessage.setText("⚠️ Please enter a valid email address!");
                return;
            } else if (!checkEmail(VerifyEmailField.getText())) {
                errorMessage.setText("⚠️ This email does not exist!");
                return;
            } else if (!isGovtIDValid()) {
                errorMessage.setText("⚠️ Please enter a valid CNIC!");
                return;
            } else if(!checkGovtID(resetPasswordCNIC.getText())){
                errorMessage.setText("⚠️ CNIC and Email didn't match!");
                return;
            }
            else{
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    receiverEmail = VerifyEmailField.getText();
                    mainController.loadDoctorOTPVerification();
                    Task<Void> sendOtpTask = new Task<>() {
                        @Override
                        protected Void call() {
                            sendOTPToEmail();
                            return null;
                        }
                    };
                    sendOtpTask.setOnSucceeded(e -> {
                        // Update UI on JavaFX Application Thread
//                        showAlert(Alert.AlertType.INFORMATION, "OTP Sent", "OTP has been sent to your email.");
                    });
                    sendOtpTask.setOnFailed(e -> {
                        // Handle failure
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to send OTP. Please try again.");
                        System.err.println("OTP sending failed: " + sendOtpTask.getException());
                    });
                    new Thread(sendOtpTask).start();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }

    public void sendOTPToEmail() {
        String email = VerifyEmailField.getText();
        EmailSender emailSender = new EmailSender();
        boolean success = emailSender.sendOtp(email, generateOTP(), Patient.getName());

        if (success) {
            System.out.println("OTP sent successfully.");
        } else {
            System.out.println("Failed to send OTP.");
        }
    }
    public static boolean isEmailValid(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
