package com.example.healzone.controller.doctor.SignUp;

import com.example.healzone.model.DoctorModel;
import com.example.healzone.controller.shared.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctorFields.*;
import static com.example.healzone.Checks.ChecksForDoctorFields.isEmailValid;
import static com.example.healzone.Checks.ChecksForDoctorFields.isGovtIDValid;
import static com.example.healzone.Checks.ChecksForDoctorFields.isPhoneNumberValid;
import static com.example.healzone.repository.DoctorRepository.*;
import static com.example.healzone.model.DoctorModel.getEmail;

public class DoctorPersonalDetailsController {


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

        firstNameField.setText(safeString(DoctorModel.getFirstName()));
        lastNameField.setText(safeString(DoctorModel.getLastName()));
        phoneField.setText(safeString(DoctorModel.getPhone()));
        emailField.setText(safeString(DoctorModel.getEmail()));
        govtIDField.setText(safeString(DoctorModel.getGovtID()));
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
        DoctorModel.setFirstName(firstNameField.getText().trim());
        DoctorModel.setLastName(lastNameField.getText().trim());
        DoctorModel.setPhone(phoneField.getText().trim());
        DoctorModel.setEmail(emailField.getText().trim());
        DoctorModel.setGovtID(govtIDField.getText().trim());

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
        }else if(!checkPhoneNumber(DoctorModel.getPhone())){
            errorMessage.setText("⚠️ This phone number is already registered!");
            return;
        }else if(checkGovtID(DoctorModel.getGovtID())){
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
