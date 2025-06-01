package com.example.healzone.Doctor.SignUp;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.StartView.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import static com.example.healzone.Checks.ChecksForDoctor.*;
import static com.example.healzone.Checks.ChecksForDoctor.isConsultationFeeValid;

public class PracticeInformationController {
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
        hospitalAddressField.setText(Doctor.getHospitalName());
        hospitalAddressField.setText(Doctor.getHospitalAddress());
        consultationFeeField.setText(Doctor.getConsultationFee());
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
}
