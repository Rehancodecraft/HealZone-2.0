package com.example.healzone.Patient;

import com.example.healzone.DatabaseConnection.Doctors;
import com.example.healzone.Doctor.DoctorCardData;
import com.example.healzone.Doctor.TimeSlot;
import com.example.healzone.Patient.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.example.healzone.DatabaseConnection.Appointments.getAppointmentNo;
import static com.example.healzone.DatabaseConnection.Appointments.insertNewAppointment;

public class ConfirmAppointmentController {
    @FXML
    private ScrollPane confirmAppointmentScrollpane;
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
    private Label selectedTimeLabel;
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
    @FXML
    private Label startTimeConfirm;
    @FXML
    private Label endTimeConfirm;

    private DoctorCardData doctorData;

    @FXML
    protected void initialize() {
        setupInitialScrollPaneStyle();
    }

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
        startTimeConfirm.setText(data.getStartTime());
        endTimeConfirm.setText(data.getEndTime());

        // Apply circular clip to doctorProfileImage
        Circle clip = new Circle();
        clip.setRadius(40); // Half of fitWidth/fitHeight (80/2)
        clip.centerXProperty().bind(doctorProfileImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(doctorProfileImage.fitHeightProperty().divide(2));
        doctorProfileImage.setClip(clip);

        // Fetch doctor's availability
        Map<String, TimeSlot> availability = Doctors.getDoctorAvailability(data.getGovtId());
        Set<String> availableDays = availability.keySet();

        // Populate available days container
        availableDaysContainer.getChildren().clear();
        for (String day : availableDays) {
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

        // Define date range (June 13–22, 2025)
        LocalDate today = LocalDate.of(2025, 6, 12); // Current date: June 12, 2025
        LocalDate startDate = today.plusDays(1); // June 13, 2025
        LocalDate endDate = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).plusDays(7); // June 22, 2025

        // Restrict DatePicker to June 13–22, 2025, on available days
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                boolean isBeforeStart = date.isBefore(startDate);
                boolean isAfterEnd = date.isAfter(endDate);
                setDisable(empty || isBeforeStart || isAfterEnd || !availableDays.contains(dayOfWeek));
            }
        });

        // Update labels when date is selected
        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDateLabel.setText(newVal.toString());
                selectedDayLabel.setText(newVal.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US));
                selectedTimeLabel.setText(data.getStartTime()+ "—" +data.getEndTime());
            }
        });

        // Cancel button closes the popup
        cancelBtn.setOnAction(e -> ((Stage) confirmationContainer.getScene().getWindow()).close());

        // Confirm button
        confirmAppointmentBtn.setOnAction(e -> {
            LocalDate selectedDate = appointmentDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Error", "Please select an appointment date.");
                return;
            }

            String selectedDay = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
            if (!availableDays.contains(selectedDay)) {
                showAlert("Error", "Selected date is not an available day for the doctor.");
                return;
            }

            if (selectedDate.isBefore(startDate)) {
                showAlert("Error", "Selected date is today or in the past. Please select a date from June 13–22, 2025.");
                return;
            }

            if (selectedDate.isAfter(endDate)) {
                showAlert("Error", "Selected date is beyond the allowed period (June 13–22, 2025).");
                return;
            }

            // Insert appointment with status "Upcoming"
            insertNewAppointment(
                    data.getGovtId(),
                    getAppointmentNo(data.getGovtId()),
                    patientPhoneField.getText(),
                    patientNameField.getText(),
                    selectedDate,
                    "Upcoming"
            );
            System.out.println("Appointment confirmed for " + doctorData.getFullName() + " on " + selectedDate);
            showAlert("Success", "Appointment booked successfully!");
            ((Stage) confirmationContainer.getScene().getWindow()).close();
        });
    }

    private void setupInitialScrollPaneStyle() {
        if (confirmAppointmentScrollpane != null) {
            confirmAppointmentScrollpane.getStyleClass().add("ScrollPane");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Set the owner to the current popup stage
        Stage popupStage = (Stage) confirmationContainer.getScene().getWindow();
        alert.initOwner(popupStage);
        alert.showAndWait();
    }
}