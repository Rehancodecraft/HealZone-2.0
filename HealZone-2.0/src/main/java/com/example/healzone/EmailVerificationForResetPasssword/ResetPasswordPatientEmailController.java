package com.example.healzone.EmailVerificationForResetPasssword;

import com.example.healzone.util.EmailSender;
import com.example.healzone.model.PatientModel;

import com.example.healzone.controller.shared.MainViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;


import static com.example.healzone.Checks.ChecksForPatientFields.isPhoneNumberValid;
import static com.example.healzone.repository.PatientRepository.checkEmail;
import static com.example.healzone.repository.PatientRepository.checkPhoneNumber;
import static com.example.healzone.util.OTPGenerator.generateOTP;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;


public class ResetPasswordPatientEmailController {
    @FXML
    private TextField VerifyEmailField;
    @FXML
    private TextField resetPasswordPhone;
    @FXML
    private Label errorMessage;
    @FXML
    private void initialize(){
        resetPasswordPhone.textProperty().addListener((observable,oldValue,newValue) ->{
            if (!newValue.matches("\\d*") || newValue.length() > 11) {
                resetPasswordPhone.setText(oldValue);
            }
        });
    }

    @FXML
    protected void onOTPSendButtonClicked(ActionEvent event) {
        try {
            PatientModel.setEmail(VerifyEmailField.getText());
            PatientModel.setPhone(resetPasswordPhone.getText());
            if (!isEmailValid(VerifyEmailField.getText())) {
                errorMessage.setText("⚠️ Please enter a valid email address!");
                return;
            }else if (!checkEmail(VerifyEmailField.getText(),resetPasswordPhone.getText())) {
                errorMessage.setText("⚠️ Email and Phone didn't match!");
                return;
            }else if(!isPhoneNumberValid()){
                errorMessage.setText("⚠️ Please enter a valid phone number!");
                return;
            }
            else if(!checkPhoneNumber(resetPasswordPhone.getText())){
                errorMessage.setText("⚠️ This Phone does not exist!");
                return;
            }
            else{
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    PatientModel.setEmail(VerifyEmailField.getText());
                    mainController.loadPatientOTPVerification();
                    Task<Void> sendOtpTask = new Task<>() {
                        @Override
                        protected Void call() {
                            sendOTPToEmail();
                            return null;
                        }
                    };
                    sendOtpTask.setOnSucceeded(e -> {
                        // Update UI on JavaFX Application Thread
                        showAlert(Alert.AlertType.INFORMATION, "OTP Sent", "OTP has been sent to your email.");
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
        EmailSender emailSender = new EmailSender();
        boolean success = emailSender.sendOtp(PatientModel.getEmail(), generateOTP(), PatientModel.getName());

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
