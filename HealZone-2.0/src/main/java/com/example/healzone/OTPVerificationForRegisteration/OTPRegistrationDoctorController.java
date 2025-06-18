package com.example.healzone.OTPVerificationForRegisteration;

import com.example.healzone.model.DoctorModel;
//import com.example.healzone.Doctor.DoctorSignUpController;
import com.example.healzone.model.TimeSlotModel;
import com.example.healzone.controller.shared.MainViewController;
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

import static com.example.healzone.repository.DoctorRepository.*;
import static com.example.healzone.repository.DoctorRepository.insertAvailabilityOfDoctor;
import static com.example.healzone.repository.DoctorRepository.insertSecurityDetails;
import static com.example.healzone.model.DoctorModel.getEmail;
import static com.example.healzone.util.EmailSender.receiverEmail;
import static com.example.healzone.util.OTPGenerator.isCooldown;
import static com.example.healzone.util.OTPGenerator.validateOTP;

public class OTPRegistrationDoctorController {
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

    public  void verifyOTPAndSignup(ActionEvent event) {
//        System.out.println(generatedOTP);
        String enteredOtp = otpField.getText();
        if(validateOTP(enteredOtp)){
            System.out.println("OTP verified. Proceed to create account.");
            System.out.println("VerifyOTPsignup running from otp verifaction for register doctor controller");
            insertDoctorPersonalDetails(DoctorModel.getGovtID(), DoctorModel.getFirstName(), DoctorModel.getLastName(), getEmail(), DoctorModel.getPhone());
            insertDoctorProfessionalDetails(DoctorModel.getGovtID(), DoctorModel.getSpecialization(), DoctorModel.getDegrees(), DoctorModel.getMedicalLicenseNumber(), DoctorModel.getBio(), DoctorModel.getExperience());
            insertPracticeInfo(DoctorModel.getGovtID(), DoctorModel.getHospitalName(), DoctorModel.getHospitalAddress(), DoctorModel.getConsultationFee());
            for (Map.Entry<String, TimeSlotModel> entry : DoctorModel.getAvailability().entrySet()) {
                String day = entry.getKey();
                TimeSlotModel slot = entry.getValue();
                insertAvailabilityOfDoctor(DoctorModel.getGovtID(), day, slot.getStartTime(), slot.getEndTime());
            }
            insertSecurityDetails(DoctorModel.getGovtID(), DoctorModel.getPassword(), true);
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");
                if (mainController != null) {
                    mainController.loadDoctorLogin();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading PatientLogin: ");
                e.printStackTrace();
            }
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
                    mainController.loadEmailVerificationForRegisterDoctor();
                    System.out.println("Resend otp running from otpforregisterfordoctorcontroller");
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
