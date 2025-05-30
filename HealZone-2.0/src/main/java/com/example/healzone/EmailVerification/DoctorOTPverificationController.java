package com.example.healzone.EmailVerification;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.Doctor.TimeSlot;
import com.example.healzone.Patient.Patient;
import com.example.healzone.Patient.signUpController;
import com.example.healzone.StartView.MainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.DatabaseConnection.Doctors.insertAvailabilityOfDoctor;
import static com.example.healzone.DatabaseConnection.Doctors.insertSecurityDetails;
//import static com.example.healzone.DatabaseConnection.Patients.registerPatient;
import static com.example.healzone.Doctor.Doctor.getEmail;
import static com.example.healzone.EmailVerification.EmailSender.receiverEmail;
import static com.example.healzone.EmailVerification.OTPgenerator.isCooldown;
import static com.example.healzone.EmailVerification.OTPgenerator.validateOTP;

public class DoctorOTPverificationController extends signUpController {
    @FXML
    private Button verifityOTPButton;
    @FXML
    private TextField otpField;
    @FXML
    private Label receiverEmailDisplay;
    @FXML
    private Label errorMessage;
    @FXML
    public void initialize(){
        receiverEmailDisplay.setText(receiverEmail);
    }

    public  void verifyOTPAndSignup() {
        System.out.println(generatedOTP);
        String enteredOtp = otpField.getText();
        if(validateOTP(enteredOtp)){
            System.out.println("OTP verified. Proceed to create account.");
            insertDoctorPersonalDetails(Doctor.getGovtID(), Doctor.getFirstName(), Doctor.getLastName(), getEmail(), Doctor.getPhone());
            insertProfessionalDetails(Doctor.getGovtID(), Doctor.getSpecialization(), Doctor.getDegrees(), Doctor.getMedicalLicenseNumber(), Doctor.getBio());
            insertPracticeInfo(Doctor.getGovtID(), Doctor.getHospitalName(), Doctor.getHospitalAddress(), Doctor.getConsultationFee());
            for (Map.Entry<String, TimeSlot> entry : Doctor.getAvailability().entrySet()) {
                String day = entry.getKey();
                TimeSlot slot = entry.getValue();
                insertAvailabilityOfDoctor(Doctor.getGovtID(), day, slot.getStartTime(), slot.getEndTime());
            }
            insertSecurityDetails(Doctor.getGovtID(), Doctor.getPassword(), true);
            closeVerifyOTPStage();
        } else {
            System.out.println("Invalid OTP.");
            errorMessage.setText("⚠️ Wrong OTP");
        }
    }
    private void closeVerifyOTPStage() {
        // Get the current window (Stage) from the sign-up button
        Stage stage = (Stage) verifityOTPButton.getScene().getWindow();
        stage.close();
    }
    public void onResendOTPLinkClicked(ActionEvent event) throws IOException {
        if(!isCooldown) {
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    mainController.loadDoctorEmailVerification();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading PatientLogin: ");
                e.printStackTrace();
            }
        }else{
            errorMessage.setText("⚠️Try Again after 60 seconds!");
        }
    }
}
