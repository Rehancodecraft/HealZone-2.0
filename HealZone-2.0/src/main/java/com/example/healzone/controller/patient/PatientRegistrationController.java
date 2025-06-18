package com.example.healzone.controller.patient;

import com.example.healzone.controller.shared.MainViewController;
import com.example.healzone.model.PatientModel;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForPatientFields.*;
import static com.example.healzone.repository.PatientRepository.*;
import static com.example.healzone.util.EmailSender.*;
import static com.example.healzone.model.PatientModel.getName;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;

public class PatientRegistrationController {
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
        signupPassword.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        signupConfirmPassword.textProperty().bindBidirectional(visibleConfirmPasswordField.textProperty());
        signupPhoneNumber.textProperty().addListener((observable,oldValue,newValue) ->{
            if (!newValue.matches("\\d*") || newValue.length() > 11) {
                signupPhoneNumber.setText(oldValue);
            }
        });


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

                signupPassword.setVisible(false);
                signupPassword.setManaged(false);
            } else {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                signupPassword.setVisible(true);
                signupPassword.setManaged(true);
            }
        });

        // Toggle for Confirm Password field
        showConfirmPasswordBox.setOnAction(event -> {
            if (showConfirmPasswordBox.isSelected()) {
                visibleConfirmPasswordField.setVisible(true);
                visibleConfirmPasswordField.setManaged(true);

                signupConfirmPassword.setVisible(false);
                signupConfirmPassword.setManaged(false);
            } else {
                visibleConfirmPasswordField.setVisible(false);
                visibleConfirmPasswordField.setManaged(false);

                signupConfirmPassword.setVisible(true);
                signupConfirmPassword.setManaged(true);
            }
        });
    }

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
        PatientModel.setName(name.getText().trim());
        PatientModel.setFatherName(fatherName.getText().trim());
        PatientModel.setPhone(signupPhoneNumber.getText());
        PatientModel.setEmail(patientEmail.getText().trim());
        PatientModel.setAge(ageField.getText());
        PatientModel.setGender(getSelectedGender());
        PatientModel.setPassword(signupPassword.getText());
        PatientModel.setConfirmPassword(signupConfirmPassword.getText());
        if (isEmpty()) {
            errorMessage.setText("⚠️ Please fill in all fields!");
            return;
        } else if (!isAgeValid()) {
            errorMessage.setText("⚠️ Age must be a positive number and less than 120");
            return;
        } else if (!isNameValid()) {
            errorMessage.setText("⚠️ Please enter a valid name!");
            return;
        } else if (!isFatherNameValid()) {
            errorMessage.setText("⚠️ Please enter a valid father name!");
            return;
        } else if (!isPhoneNumberValid()) {
            errorMessage.setText("⚠️ Phone number must be of type(eg.03016567902)!");
            return;
        } else if (checkPhoneNumber(PatientModel.getPhone())) {
            errorMessage.setText("⚠️ This Phone number is already registered!");
            return;
        } else if (checkEmail(PatientModel.getEmail())) {
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
                    mainController.loadOTPVerificationForRegisterPatient();
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