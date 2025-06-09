package com.example.healzone.Doctor.SignUp;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.StartView.MainViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctor.*;
import static com.example.healzone.Checks.ChecksForDoctor.isEmailValid;
import static com.example.healzone.Checks.ChecksForDoctor.isGovtIDValid;
import static com.example.healzone.Checks.ChecksForDoctor.isPhoneNumberValid;
import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.Doctor.Doctor.getEmail;

public class PersonalDetailsController {


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


    @FXML
    private void initialize(){

        firstNameField.setText(safeString(Doctor.getFirstName()));
        lastNameField.setText(safeString(Doctor.getLastName()));
        phoneField.setText(safeString(Doctor.getPhone()));
        emailField.setText(safeString(Doctor.getEmail()));
        govtIDField.setText(safeString(Doctor.getGovtID()));
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > 11) {
                phoneField.setText(oldValue); // Revert to previous value
            }
        });
        govtIDField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") || newValue.length() > 13) {
                govtIDField.setText(oldValue);
            }
        });
    }
    public void onPersonalDetailsNextButtonClicked(javafx.event.ActionEvent event){
        Doctor.setFirstName(firstNameField.getText().trim());
        Doctor.setLastName(lastNameField.getText().trim());
        Doctor.setPhone(phoneField.getText().trim());
        Doctor.setEmail(emailField.getText().trim());
        Doctor.setGovtID(govtIDField.getText().trim());

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
        System.out.println(phoneField.getText());
    }
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
