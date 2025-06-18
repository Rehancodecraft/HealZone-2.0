package com.example.healzone.controller.doctor.SignUp;

import com.example.healzone.model.DoctorModel;
import com.example.healzone.controller.shared.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctorFields.*;
import static com.example.healzone.Checks.ChecksForDoctorFields.isConsultationFeeValid;

public class DoctorPracticeInformationController {
    //Practice Information
    @FXML
    private TextField hospitalNameField;
    @FXML
    private TextField hospitalAddressField;
    @FXML
    private TextField consultationFeeField;
    @FXML
    private Label practiceInformationErrorMessage;
    @FXML
    private void initialize(){
        hospitalNameField.setText(safeString(DoctorModel.getHospitalName()));
        hospitalAddressField.setText(safeString(DoctorModel.getHospitalAddress()));
        consultationFeeField.setText(safeString(DoctorModel.getConsultationFee()));
    }
    @FXML
    public void onPracticeInformationNextButtonClicked(javafx.event.ActionEvent event){
        DoctorModel.setHospitalName(hospitalNameField.getText());
        DoctorModel.setHospitalAddress(hospitalAddressField.getText());
        DoctorModel.setConsultationFee(consultationFeeField.getText());
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
    private String safeString(String value) {
        return value != null ? value : "";
    }
}
