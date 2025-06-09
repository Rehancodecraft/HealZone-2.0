package com.example.healzone.Patient;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.healzone.Doctor.DoctorCardData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
public class ConfirmAppointmentController {
        @FXML
        private VBox confirmationContainer;
        @FXML
        private ImageView doctorProfileImage;
        @FXML
        private Label doctorNameLabel;
        @FXML
        private Label specialtiesLabel;
        @FXML
        private Label credentialsLabel;
        @FXML
        private Label consultationFeeLabel;
        @FXML
        private Label hospitalNameLabel;
        @FXML
        private Label hospitalAddressLabel;
        @FXML
        private FlowPane availableDaysContainer;
        @FXML
        private DatePicker appointmentDatePicker;
        @FXML
        private TextField patientNameField;
        @FXML
        private TextField patientPhoneField;
        @FXML
        private TextField patientAgeField;
        @FXML
        private TextField descriptionField;
        @FXML
        private Label selectedDateLabel;
        @FXML
        private Label selectedDayLabel;
        @FXML
        private Label summaryDoctorLabel;
        @FXML
        private Label summaryHospitalLabel;
        @FXML
        private Label totalFeeLabel;
        @FXML
        private Button confirmAppointmentBtn;
        @FXML
        private Button cancelBtn;

        private DoctorCardData doctorData;


    public void setDataForConfirmAppointment(DoctorCardData data) {
        this.doctorData = data;
        doctorNameLabel.setText(data.getFullName());
        specialtiesLabel.setText(data.getSpecialization());
        credentialsLabel.setText(data.getDegrees());
        consultationFeeLabel.setText("Rs. " + data.getConsultationFee());
        hospitalNameLabel.setText(data.getHospitalName());
        hospitalAddressLabel.setText(data.getHospitalAddress());
        totalFeeLabel.setText("Rs. " + data.getConsultationFee());
        summaryDoctorLabel.setText(data.getFullName());
        summaryHospitalLabel.setText(data.getHospitalName());
        patientNameField.setText(Patient.getName());
        patientAgeField.setText(Patient.getAge());
        patientPhoneField.setText(Patient.getPhone());

        // Populate available days based on doctor's availability (example logic)
        availableDaysContainer.getChildren().clear();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Replace with actual availability from doctorData if available
        for (String day : days) {
            Button dayBtn = new Button(day);
            dayBtn.getStyleClass().setAll("day-btn");
            final String dayText = day;
            dayBtn.setOnAction(e -> {
                availableDaysContainer.getChildren().forEach(btn -> btn.getStyleClass().setAll("day-btn"));
                dayBtn.getStyleClass().setAll("day-btn-selected");
                selectedDayLabel.setText(dayText);
            });
            availableDaysContainer.getChildren().add(dayBtn);
        }

        // Update summary when date is selected
        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDateLabel.setText(newVal.toString());
                selectedDayLabel.setText(newVal.getDayOfWeek().toString());
            }
        });

        // Cancel button closes the popup
        cancelBtn.setOnAction(e -> ((Stage) confirmationContainer.getScene().getWindow()).close());

        // Confirm button (placeholder action)
        confirmAppointmentBtn.setOnAction(e -> {
            System.out.println("Appointment confirmed for " + doctorData.getFullName() + " at " + selectedDateLabel.getText());
            ((Stage) confirmationContainer.getScene().getWindow()).close();
            // Add logic to save appointment to database here
        });
    }
}
