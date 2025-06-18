package com.example.healzone.EmailVerificationForRegistration;

import com.example.healzone.repository.DoctorRepository;
import com.example.healzone.repository.PatientRepository;
import com.example.healzone.model.PatientModel;
import com.example.healzone.controller.shared.MainViewController;
import com.example.healzone.util.EmailSender;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.util.EmailSender.receiverEmail;
import static com.example.healzone.util.OTPGenerator.generateOTP;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;


public class EmailVerificationPatientController {
    @FXML
    private TextField VerifyEmailField;
    @FXML
    private Label errorMessage;

    @FXML
    protected void onOTPSendButtonClicked(ActionEvent event) {
        try {
            PatientModel.setEmail(VerifyEmailField.getText());
            if (!isEmailValid(VerifyEmailField.getText())) {
                errorMessage.setText("⚠️ Please enter a valid email address!");
                return;
            }else if(DoctorRepository.checkEmail(VerifyEmailField.getText()) || PatientRepository.checkEmail(VerifyEmailField.getText())) {
                errorMessage.setText("⚠️ This email is already registered!");
                return;
            }else{
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    receiverEmail = PatientModel.getEmail();
                    mainController.loadOTPVerificationForRegisterPatient();
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
        boolean success = emailSender.sendOtp(email, generateOTP(), PatientModel.getName());

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
