package com.example.healzone.Patient;

import com.example.healzone.StartView.MainViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForPatient.*;
import static com.example.healzone.DatabaseConnection.DatabaseConnection.*;
import static com.example.healzone.DatabaseConnection.Patients.*;
import static com.example.healzone.EmailVerification.EmailSender.*;
import static com.example.healzone.Patient.Patient.getName;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;

public class signUpController {
    protected static String generatedOTP;
    @FXML
    private Label errorMessage;
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
        Patient.setName(name.getText().trim());
        Patient.setFatherName(fatherName.getText().trim());
        Patient.setPhone(signupPhoneNumber.getText());
        Patient.setEmail(patientEmail.getText().trim());
        Patient.setAge(ageField.getText());
        Patient.setGender(getSelectedGender());
        Patient.setPassword(signupPassword.getText());
        Patient.setConfirmPassword(signupConfirmPassword.getText());
        if (isEmpty()) {
            errorMessage.setText("⚠️ Please fill in all fields!");
            return;
        } else if (!isAgeValid()) {
            errorMessage.setText("⚠️ Age must be a positive number and less than 120");
        } else if (!isNameValid()) {
            errorMessage.setText("⚠️ Please enter a valid name!");
            return;
        } else if (!isFatherNameValid()) {
            errorMessage.setText("⚠️ Please enter a valid father name!");
            return;
        } else if (!isPhoneNumberValid()) {
            errorMessage.setText("⚠️ Phone number must be of type(eg.03016567902)!");
            return;
        } else if (checkPhoneNumber(Patient.getPhone())) {
            errorMessage.setText("⚠️ This Phone number is already registered!");
            return;
        } else if (checkEmail(Patient.getEmail())) {
            errorMessage.setText("⚠️ This Email is already registered!");
            return;
        } else if (!isPasswordMatch()) {
            errorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!isPasswordValid()) {
            errorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else if (!isEmailValid()) {
            errorMessage.setText("⚠️ Please enter a valid email address!");
            return;
        } else {
            try {
                // Load OTP verification view
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    receiverEmail = patientEmail.getText();
                    // Load OTP verification view immediately
                    mainController.loadOTPVerification();
                    // Send OTP in a background thread
                    Task<Void> sendOtpTask = new Task<>() {
                        @Override
                        protected Void call() {
                            sendOTPToEmail(receiverEmail,getName());
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
            } catch (NumberFormatException e) {
                errorMessage.setText("⚠️ Age must be a number");
                e.printStackTrace();
            }
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
}