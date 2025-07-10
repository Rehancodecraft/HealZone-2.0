package com.example.healzone.controller.shared;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class MainViewController {

    @FXML
    private StackPane contentPane;
    @FXML
    private ToggleGroup loginToggleGroup;
    @FXML
    private StackPane root;

    public void initialize() {
        root.getProperties().put("controller", this);
        System.out.println("Inside initialize: contentPane = " + contentPane);
        Platform.runLater(() -> loadView("/com/example/healzone/Patient/patient-login.fxml"));
    }

    public void loadPatientLogin() {

        loadView("/com/example/healzone/Patient/patient-login.fxml");
    }

    public void loadDoctorLogin() {

        loadView("/com/example/healzone/Doctor/doctor-login.fxml");
    }

    public void loadPatientSignup() {
        loadView("/com/example/healzone/Patient/patient-signup.fxml");
    }
    public void loadPatientEmailVerification() {
        loadView("/com/example/healzone/ResetPassword/verify-patient-email.fxml");
    }
    public void loadDoctorEmailVerification() {
        loadView("/com/example/healzone/ResetPassword/verify-doctor-email.fxml");
    }
    public void loadEmailVerificationForRegisterPatient() {
        loadView("/com/example/healzone/EmailVerification/VerifyEmailForRegisterPatient.fxml");
    }
    public void loadEmailVerificationForRegisterDoctor() {
        loadView("/com/example/healzone/EmailVerification/VerifyEmailForRegisterDoctor.fxml");
    }
    public void loadDoctorPersonalDetails() {
        loadView("/com/example/healzone/Doctor/Signup/personal-details.fxml");
    }
    public void loadDoctorPracticeInformation() {
        loadView("/com/example/healzone/Doctor/Signup/practice-information.fxml");
    }
    public void loadDoctorProfessionalDetails() {
        loadView("/com/example/healzone/Doctor/Signup/professional-details.fxml");
    }
    public void loadDoctorSecurity() {
        loadView("/com/example/healzone/Doctor/Signup/security.fxml");
    }
    public void loadDoctorTimeTable() {
        loadView("/com/example/healzone/Doctor/Signup/time-table.fxml");
    }
    public void loadOTPVerificationForRegisterPatient(){
        loadView("/com/example/healzone/EmailVerification/OTPverificationForRegisterPatient.fxml");
    }
    public void loadOTPVerificationForRegisterDoctor(){
        loadView("/com/example/healzone/EmailVerification/otp-verification-for-registerDoctor.fxml");
    }
    public void loadDoctorOTPVerification(){
        loadView("/com/example/healzone/ResetPassword/doctor-OTP-verification.fxml");
    }
    public void loadPatientOTPVerification(){
        loadView("/com/example/healzone/ResetPassword/patient-OTP-verification.fxml");
    }
    public void loadPatientResetPassword(){
        loadView("/com/example/healzone/ResetPassword/patient-reset-password.fxml");
    }
    public void loadDoctorResetPassword(){
        loadView("/com/example/healzone/ResetPassword/doctor-reset-password.fxml");
    }

    protected void loadView(String fxmlPath) {
        try {
            if (contentPane == null) {
                System.err.println("contentPane is null in loadView!");
                return;
            }

            // Load the new view
            Parent newView = FXMLLoader.load(getClass().getResource(fxmlPath));
            newView.setOpacity(0);
            newView.setScaleX(0.98);
            newView.setScaleY(0.98);

            // Add to contentPane
            contentPane.getChildren().setAll(newView);

            // Fade + scale animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), newView);
            scaleIn.setFromX(0.98);
            scaleIn.setToX(1);
            scaleIn.setFromY(0.98);
            scaleIn.setToY(1);

            // Play both together
            ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
            transition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setContent(String fxmlPath) {
        try {
            if (contentPane == null) {
                System.err.println("contentPane is null in setContent!");
                return;
            }
            Parent node = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Failed to set content: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
