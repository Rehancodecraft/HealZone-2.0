package com.example.healzone.controller.doctor.SignUp;

import com.example.healzone.model.DoctorModel;
import com.example.healzone.controller.shared.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctorFields.*;

public class DoctorProfessionalDetailsController {
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
    private TextField experience;
    @FXML
    private void initialize(){
        specialization.setValue(safeString(DoctorModel.getSpecialization()));
        degreesField.setText(safeString(DoctorModel.getDegrees()));
        licenseNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && (licenseNumberField.getText() == null || licenseNumberField.getText().isEmpty())) {
                licenseNumberField.setText("PMDC-");
            }
        });
        licenseNumberField.setText(safeString(DoctorModel.getMedicalLicenseNumber()));
    }
    @FXML
    public void onProfessionalDetailsNextButtonClicked(javafx.event.ActionEvent event){
        if(specialization.getValue() == null){
            professionalDetailsErrorMessage.setText("⚠️ Please fill in all fields!");
        }else {
            DoctorModel.setSpecialization((String) specialization.getValue());
            DoctorModel.setDegrees(degreesField.getText());
            DoctorModel.setMedicalLicenseNumber(licenseNumberField.getText());
            DoctorModel.setExperience(experience.getText().trim());
            if (isProfessionalDetailsEmpty()) {
                professionalDetailsErrorMessage.setText("⚠️ Please fill in all fields!");
                return;
            } else if (!isDegreesValid()) {
                professionalDetailsErrorMessage.setText("⚠️ Please enter a valid Degree(s)!");
                return;
            } else if (!isMedicalLicenseValid()) {
                professionalDetailsErrorMessage.setText("⚠️ Please enter a valid License Number!");
                return;
            } else if (isExperienceEmpty()) {
                    professionalDetailsErrorMessage.setText("⚠️ Please fill in all fields!");
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
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
