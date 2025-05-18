package com.example.healzone.Patient;

import com.example.healzone.EmailSender;
import com.example.healzone.MainViewController;
import com.example.healzone.OTPservice;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.io.IOException;

import static com.example.healzone.DatabaseConnection.*;

public class signUpController {
    protected static String generatedOTP;
    @FXML
    private Hyperlink signUpLink;
    @FXML
    protected TextField ageField;
    @FXML
    protected TextField signupPhoneNumber;
    @FXML
    protected TextField name;
    @FXML
    protected TextField fatherName;
    @FXML
    protected ToggleGroup Gender;
    @FXML
    protected TextField signupPassword;
    @FXML
    protected Button signUpButton;
    @FXML
    protected TextField patientEmail;
    protected static String receiverEmail;
    @FXML
    protected TextField signupConfirmPassword;

    @FXML
    protected void onClickSignInLink(ActionEvent event) {
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

    @FXML
    private void onSignUpButtonClick(ActionEvent event) {
        if (name.getText().isEmpty() || ageField.getText().isEmpty() || signupPassword.getText().isEmpty()
                || fatherName.getText().isEmpty() || getSelectedGender().isEmpty() || signupPhoneNumber.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields!");
            return;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age <= 0 || age > 120) {
                showAlert(Alert.AlertType.ERROR, "Error", "Age must be a positive number!");
                return;
            }
            if (name.getText().matches("\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid name!");
                return;
            }
            if (fatherName.getText().matches("\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Error", " ecuInvalid father name!");
                return;
            }
            if (!signupPhoneNumber.getText().matches("\\d+")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Phone number must contain digits only!");
                return;
            }
            if (signupPhoneNumber.getText().length() != 11) {
                showAlert(Alert.AlertType.ERROR, "Error", "Phone number must be exactly 11 digits!");
                return;
            }
            if (!signupPhoneNumber.getText().startsWith("03")) {
                showAlert(Alert.AlertType.ERROR, "Error", "Phone number must start with 03");
                return;
            }
            if (!checkPhoneNumber(signupPhoneNumber.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "This Phone number is already registered!");
                return;
            }
            if (!checkEmail(patientEmail.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "This Email is already registered!");
                return;
            }
            if (!signupPassword.getText().equals(signupConfirmPassword.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Password and Confirm Password do not match!");
                return;
            }
            if (signupPassword.getText().length() < 8) {
                showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 8 characters!");
                return;
            }
            if (!isValidEmail(patientEmail.getText())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address!");
                return;
            }

            // Set patient data
            Patient.setName(name.getText());
            Patient.setFatherName(fatherName.getText());
            Patient.setPhone(signupPhoneNumber.getText());
            Patient.setEmail(patientEmail.getText());
            Patient.setAge(ageField.getText());
            Patient.setGender(getSelectedGender());
            Patient.setPassword(signupPassword.getText());
            Patient.setConfirmPassword(signupConfirmPassword.getText());

            // Load OTP verification view
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                receiverEmail = patientEmail.getText();
                // Load OTP verification view immediately
                mainController.setContent("/com/example/healzone/Patient/OTPverification.fxml");
                // Send OTP in a background thread
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
            } else {
                System.err.println("MainViewController not found in root properties.");
                showAlert(Alert.AlertType.ERROR, "Error", "Internal error: Unable to load OTP verification.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid age format!");
            e.printStackTrace();
        }
    }

    public void ageFieldCheck() {
        ageField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ageField.setText(newValue.replaceAll("[^\\d]", ""));
            } else if (!newValue.isEmpty()) {
                int value = Integer.parseInt(newValue);
                if (value < 1 || value > 120) {
                    ageField.setText(oldValue);
                }
            }
        });
    }

    public void phoneFieldCheck() {
        signupPhoneNumber.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                signupPhoneNumber.setText(newValue.replaceAll("[^\\d]", ""));
            } else if (newValue.length() > 11) {
                signupPhoneNumber.setText(oldValue);
            }
        });
    }

    protected String getSelectedGender() {
        if (Gender.getSelectedToggle() != null) {
            return ((RadioButton) Gender.getSelectedToggle()).getText();
        }
        return "";
    }

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Scene scene = alert.getDialogPane().getScene();
        scene.getStylesheets().add(getClass().getResource("Alert.css").toExternalForm());

        alert.show();
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(event -> alert.close());
        pause.play();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    public void sendOTPToEmail() {
        String email = patientEmail.getText();
        generatedOTP = OTPservice.generateOTP();
        EmailSender emailSender = new EmailSender();
        boolean success = emailSender.sendOtp(email, generatedOTP, name.getText());

        if (success) {
            System.out.println("OTP sent successfully.");
        } else {
            System.out.println("Failed to send OTP.");
        }
    }
}