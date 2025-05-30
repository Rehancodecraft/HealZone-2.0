package com.example.healzone.ResetPassword;

import com.example.healzone.Patient.Patient;
import javafx.fxml.FXML;
import java.awt.*;

import static com.example.healzone.Checks.ChecksForPatient.isPasswordMatch;
import static com.example.healzone.Checks.ChecksForPatient.isPasswordValid;
import static com.example.healzone.DatabaseConnection.Patients.resetPassword;

public class PatientResetPasswordController {
    @FXML
    private TextField password;
    @FXML
    private TextField confirmPassword;
    @FXML
    private Label resetPasswordErrorMessage;
    @FXML
    private void onClickResetPasswordButton() {
        Patient.setPassword(password.getText());
        Patient.setConfirmPassword(confirmPassword.getText());
        if (!isPasswordMatch()) {
            resetPasswordErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!isPasswordValid()) {
            resetPasswordErrorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else {
            resetPassword(Patient.getPassword(), Patient.getEmail());
        }
    }
}
