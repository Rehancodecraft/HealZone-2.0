package com.example.healzone.Doctor.SignUp;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.StartView.MainViewController;
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
        firstNameField.setText(Doctor.getFirstName());
        lastNameField.setText(Doctor.getLastName());
        phoneField.setText(Doctor.getPhone());
        emailField.setText(Doctor.getEmail());
        govtIDField.setText(Doctor.getGovtID());
    }
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
}
