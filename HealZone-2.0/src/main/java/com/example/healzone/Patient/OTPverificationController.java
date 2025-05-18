package com.example.healzone.Patient;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import static com.example.healzone.DatabaseConnection.registerPatient;

public class OTPverificationController extends signUpController{
    @FXML
    private Button verifityOTPButton;
    @FXML
    private TextField otpField;
    @FXML
    private Label receiverEmailDisplay;
    @FXML
    public void initialize(){
        receiverEmailDisplay.setText(receiverEmail);
    }

    public  void verifyOTPAndSignup() {
        System.out.println(generatedOTP);
        String enteredOtp = otpField.getText();
        if (enteredOtp.equals(generatedOTP)) {
            System.out.println("OTP verified. Proceed to create account.");
                registerPatient(Patient.getName(), Patient.getFatherName(), Patient.getPhone(), Patient.getEmail(), Patient.getAge(), Patient.getGender(), Patient.getPassword());
                closeVerifyOTPStage();
        } else {
            System.out.println("Invalid OTP.");
            showAlert(Alert.AlertType.ERROR, "Error", "Wrong OTP!");
        }
    }
    private void closeVerifyOTPStage() {
        // Get the current window (Stage) from the sign-up button
        Stage stage = (Stage) verifityOTPButton.getScene().getWindow();
        stage.close();  // Close the current stage
    }
}
