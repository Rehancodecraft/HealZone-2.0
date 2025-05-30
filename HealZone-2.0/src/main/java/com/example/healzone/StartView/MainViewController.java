package com.example.healzone.StartView;

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
        System.out.println("Inside initialize: contentPane = " + contentPane);
        Platform.runLater(() -> loadView("/com/example/healzone/Patient/PatientLogin.fxml"));
    }

    public void loadPatientLogin() {

        loadView("/com/example/healzone/Patient/PatientLogin.fxml");
    }

    public void loadDoctorLogin() {

        loadView("/com/example/healzone/Doctor/DoctorLogin.fxml");
    }

    public void loadPatientSignup() {
        loadView("/com/example/healzone/Patient/PatientSignUp.fxml");
    }
    public void loadPatientEmailVerification() {
        loadView("/com/example/healzone/ResetPassword/VerifyPatientEmail.fxml");
    }
    public void loadDoctorEmailVerification() {
        loadView("/com/example/healzone/ResetPassword/VerifyDoctorEmail.fxml");
    }
    public void loadEmailVerificationForRegister() {
        loadView("/com/example/healzone/ResetPassword/VerifyEmailForRegister.fxml");
    }
    public void loadDoctorPersonalDetails() {
        loadView("/com/example/healzone/Doctor/Signup/PersonalDetails.fxml");
    }
    public void loadDoctorPracticeInformation() {
        loadView("/com/example/healzone/Doctor/Signup/PracticeInformation.fxml");
    }
    public void loadDoctorProfessionalDetails() {
        loadView("/com/example/healzone/Doctor/Signup/ProfessionalDetails.fxml");
    }
    public void loadDoctorSecurity() {
        loadView("/com/example/healzone/Doctor/Signup/Security.fxml");
    }
    public void loadDoctorTimeTable() {
        loadView("/com/example/healzone/Doctor/Signup/TimeTable.fxml");
    }
    public void loadOTPVerification(){
        loadView("/com/example/healzone/EmailVerification/OTPverification.fxml");
    }
    public void loadDoctorOTPVerification(){
        loadView("/com/example/healzone/EmailVerification/DoctorOTPverification.fxml");
    }
    public void loadPatientOTPVerification(){
        loadView("/com/example/healzone/EmailVerification/PatientOTPverification.fxml");
    }
    public void loadHomePage(){
        loadView("/com/example/healzone/StartView/HomePage.fxml");
    }

    protected void loadView(String fxmlPath) {
        try {
            if (contentPane == null) {
                System.err.println("contentPane is null in loadView!");
                return;
            }

            // Load the new view
            Parent newView = FXMLLoader.load(getClass().getResource(fxmlPath));
//            DoctorSignUpController controller = new DoctorSignUpController();
//            if((Doctor.getFirstName() != null) && (Doctor.getLastName() != null) && (Doctor.getPhone() != null) && (Doctor.getEmail()!=null) && (Doctor.getGovtID() !=null)){
//                controller.prefillFieldsFromDoctor();
//            }
            // Set initial opacity and scale
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
