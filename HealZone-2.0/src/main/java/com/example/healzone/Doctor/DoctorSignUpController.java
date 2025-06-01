package com.example.healzone.Doctor;

import com.example.healzone.StartView.MainViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import static com.example.healzone.Checks.ChecksForDoctor.*;
import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.Doctor.Doctor.*;
import static com.example.healzone.EmailVerificationForRegistration.EmailSender.receiverEmail;
import static com.example.healzone.EmailVerificationForRegistration.EmailSender.sendOTPToEmail;
import static com.example.healzone.ShowAlert.ShowAlert.showAlert;


public class DoctorSignUpController {
    protected static String generatedOTP;
    //Personal Details
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField govtIDField;
    @FXML
    private Label errorMessage;

    //Practice Information
    @FXML
    private TextField hospitalNameField;
    @FXML
    private TextField hospitalAddressField;
    @FXML
    private TextField consultationFeeField;
    @FXML
    private Label practiceInformationErrorMessage;

    //ProfessionalDetails
    @FXML
    private ComboBox specialization;
    @FXML
    private TextField degreesField;
    @FXML
    private TextField licenseNumberField;
    @FXML
    private Label professionalDetailsErrorMessage;

    //TimeTable
    @FXML
    private CheckBox mon,tue,wed,thur,fri,sat,sun;
    @FXML
    private ComboBox startTime;
    @FXML
    private ComboBox endTime;
    @FXML
    private TextArea bio;
    @FXML
    private Label timeTableErrorMessage;

    //Security
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private CheckBox userAgreement;
    @FXML
    private Label securityErrorMessage;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private CheckBox showPasswordBox;
    @FXML
    private TextField visibleConfirmPasswordField;
    @FXML
    private CheckBox showConfirmPasswordBox;
//    @FXML
//    private void initialize() {
//        // Sync content when typing
//        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());
//        confirmPasswordField.textProperty().bindBidirectional(visibleConfirmPasswordField.textProperty());
//
//        // Hide visiblePasswordFields initially
//        visiblePasswordField.setVisible(false);
//        visiblePasswordField.setManaged(false);
//        visibleConfirmPasswordField.setVisible(false);
//        visibleConfirmPasswordField.setManaged(false);
//
//        // Toggle for Password field
//        showPasswordBox.setOnAction(event -> {
//            if (showPasswordBox.isSelected()) {
//                visiblePasswordField.setVisible(true);
//                visiblePasswordField.setManaged(true);
//
//                passwordField.setVisible(false);
//                passwordField.setManaged(false);
//            } else {
//                visiblePasswordField.setVisible(false);
//                visiblePasswordField.setManaged(false);
//
//                passwordField.setVisible(true);
//                passwordField.setManaged(true);
//            }
//        });
//
//        // Toggle for Confirm Password field
//        showConfirmPasswordBox.setOnAction(event -> {
//            if (showConfirmPasswordBox.isSelected()) {
//                visibleConfirmPasswordField.setVisible(true);
//                visibleConfirmPasswordField.setManaged(true);
//
//                confirmPasswordField.setVisible(false);
//                confirmPasswordField.setManaged(false);
//            } else {
//                visibleConfirmPasswordField.setVisible(false);
//                visibleConfirmPasswordField.setManaged(false);
//
//                confirmPasswordField.setVisible(true);
//                confirmPasswordField.setManaged(true);
//            }
//        });
//    }
    public void onPersonalDetailsNextButtonClicked(javafx.event.ActionEvent event){
        Doctor.setFirstName(firstNameField.getText());
        Doctor.setLastName(lastNameField.getText());
        Doctor.setPhone(phoneField.getText());
        Doctor.setEmail(emailField.getText());
        Doctor.setGovtID(govtIDField.getText());
        if(isPersonalDetailsEmpty()) {
            errorMessage.setText("⚠️ Please fill in all fields!");
            return;
        }else if(!isFirstNameValid()){
            errorMessage.setText("⚠️ Please enter a valid first name!");
            return;
        }else if(!isLastNameValid()) {
            errorMessage.setText("⚠️ Please enter a valid last name!");
            return;
        }else if(!isGovtIDValid()) {
            errorMessage.setText("⚠️ Please enter a valid Govt ID!");
            return;
        }else if(!isPhoneNumberValid()) {
            errorMessage.setText("⚠️ Please enter a valid Phone Number!");
            return;
        }else if(!isEmailValid(getEmail())) {
            errorMessage.setText("⚠️ Please enter a valid Email!");
            return;
        }else if(checkEmail(getEmail())){
            errorMessage.setText("⚠️ This email is already registered!");
            return;
        }else if(!checkPhoneNumber(Doctor.getPhone())){
            errorMessage.setText("⚠️ This phone number is already registered!");
            return;
        }else if(checkGovtID(Doctor.getGovtID())){
            errorMessage.setText("⚠️ This CNIC is already registered!");
            return;
        }else{
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    mainController.loadDoctorProfessionalDetails();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading DoctorLogin: ");
                e.printStackTrace();
            }
        }
        System.out.println(Doctor.getFirstName());
        System.out.println(Doctor.getLastName());
        System.out.println(getEmail());
        System.out.println(Doctor.getPhone());
        System.out.println(Doctor.getGovtID());
    }
    @FXML
    public void onProfessionalDetailsNextButtonClicked(javafx.event.ActionEvent event){
        if(specialization.getValue() == null){
            professionalDetailsErrorMessage.setText("⚠️ Please fill in all fields!");
        }else {
            Doctor.setSpecialization((String) specialization.getValue());
            Doctor.setDegrees(degreesField.getText());
            Doctor.setMedicalLicenseNumber(licenseNumberField.getText());
            if (isProfessionalDetailsEmpty()) {
                professionalDetailsErrorMessage.setText("⚠️ Please fill in all fields!");
                return;
            } else if (!isDegreesValid()) {
                professionalDetailsErrorMessage.setText("⚠️ Please enter a valid Degree(s)!");
                return;
            } else if (!isMedicalLicenseValid()) {
                professionalDetailsErrorMessage.setText("⚠️ Please enter a valid License Number!");
                return;
            } else {

                try {
                    StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                    MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                    if (mainController != null) {
                        mainController.loadDoctorPracticeInformation();
                    } else {
                        System.err.println("MainViewController not found in root properties.");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading DoctorLogin: ");
                    e.printStackTrace();
                }
            }
        }
    }
    @FXML
    public void onPracticeInformationNextButtonClicked(javafx.event.ActionEvent event){
        Doctor.setHospitalName(hospitalNameField.getText());
        Doctor.setHospitalAddress(hospitalAddressField.getText());
        Doctor.setConsultationFee(consultationFeeField.getText());
        if(isPracticeInformationEmpty()){
            practiceInformationErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        }else if(!isHospitalNameValid()) {
            practiceInformationErrorMessage.setText("⚠️ Please enter a Hospital Name!");
            return;
        }else if(!isHospitalAddressValid()) {
            practiceInformationErrorMessage.setText("⚠️ Please enter a valid Hospital Address!");
            return;

        }else if(!isConsultationFeeValid()) {
            practiceInformationErrorMessage.setText("⚠️ Please enter a valid amount of fee!");
            return;
        }else {

            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    mainController.loadDoctorTimeTable();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading PatientLogin: ");
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void onTimeTableNextButtonClicked(javafx.event.ActionEvent event){
        if (bio.getText().isEmpty()) {
            timeTableErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        }else if (startTime.getValue() == null || endTime.getValue() == null) {
            timeTableErrorMessage.setText("⚠️ Time slots can't be empty!");
            return;
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime start = LocalTime.parse((String) startTime.getValue(), timeFormatter);
        LocalTime end = LocalTime.parse((String) endTime.getValue(), timeFormatter);
        if (end.isBefore(start) || end.equals(start)) {
            timeTableErrorMessage.setText("⚠️ End time must be after start time!");
            return;
        }
        Map<CheckBox, String> dayMap = Map.of(
                mon, "Monday",
                tue, "Tuesday",
                wed, "Wednesday",
                thur, "Thursday",
                fri, "Friday",
                sat, "Saturday",
                sun, "Sunday"
        );
        boolean atLeastOneDaySelected = false;
        for (Map.Entry<CheckBox, String> entry : dayMap.entrySet()) {
            CheckBox cb = entry.getKey();
            String day = entry.getValue();
            if (cb.isSelected()) {
                atLeastOneDaySelected = true;
                // ✅ Store availability temporarily in memory
//                Doctor.setAvailability(day, new TimeSlot(start, end));
            }
        }
        if (!atLeastOneDaySelected) {
            timeTableErrorMessage.setText("⚠️ Please select at least one day!");
            return;
        }
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorSecurity();  // next step
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading next scene: ");
            e.printStackTrace();
        }
    }

    @FXML
    public void onSignUpButtonClicked(ActionEvent event) {
        Doctor.setPassword(passwordField.getText());
        Doctor.setConfirmPassword(confirmPasswordField.getText());
        if (isSecurityEmpty()) {
            securityErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        } else if (!isPasswordValid()) {
            securityErrorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else if (!isPasswordMatch()) {
            securityErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!userAgreement.isSelected()) {
            securityErrorMessage.setText("⚠️ Please accept the user agreement to continue!");
            return;
        } else {
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");

                if (mainController != null) {
                    System.out.println(getEmail());
                    receiverEmail = getEmail();
                    // Load OTP verification view immediately
                    mainController.loadOTPVerificationForRegisterDoctor();
                    // Send OTP in a background thread
                    Task<Void> sendOtpTask = new Task<>() {
                        @Override
                        protected Void call() {
                            sendOTPToEmail(receiverEmail, getFirstName() + getLastName());
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
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
    public void onProfessionalDetailsBackButtonClicked(javafx.event.ActionEvent event){
        try {
            firstNameField.setText(Doctor.getFirstName());
            lastNameField.setText(Doctor.getLastName());
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");
            if (mainController != null) {

                mainController.loadDoctorPersonalDetails();

            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
    public void onPracticeInformationBackButtonClicked(javafx.event.ActionEvent event){
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorProfessionalDetails();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
    public void onTimeTableBackButtonClicked(javafx.event.ActionEvent event){
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorPracticeInformation();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
    public void onSecurityBackButtonClicked(javafx.event.ActionEvent event){
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorTimeTable();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }
    public  void prefillFieldsFromDoctor() {
        if((Doctor.getFirstName() != null) && (Doctor.getLastName() != null) && (Doctor.getPhone() != null) && (getEmail()!=null) && (Doctor.getGovtID() !=null)){
            firstNameField.setText(Doctor.getFirstName());
            lastNameField.setText(Doctor.getLastName());
            phoneField.setText(Doctor.getPhone());
            emailField.setText(getEmail());
            govtIDField.setText(Doctor.getGovtID());
        }else {
            return;
        }
    }

}
