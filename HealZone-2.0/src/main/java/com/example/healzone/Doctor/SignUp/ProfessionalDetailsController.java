package com.example.healzone.Doctor.SignUp;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.StartView.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import javax.print.Doc;

import static com.example.healzone.Checks.ChecksForDoctor.*;

public class ProfessionalDetailsController {
    //ProfessionalDetails
    @FXML
    private ComboBox specialization;
    @FXML
    private TextField degreesField;
    @FXML
    private TextField licenseNumberField;
    @FXML
    private Label professionalDetailsErrorMessage;
    @FXML
    private void initialize(){
        specialization.setValue(Doctor.getSpecialization());
        degreesField.setText(Doctor.getDegrees());
        licenseNumberField.setText(Doctor.getMedicalLicenseNumber());
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
    public void onProfessionalDetailsBackButtonClicked(javafx.event.ActionEvent event){
        try {
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
}
