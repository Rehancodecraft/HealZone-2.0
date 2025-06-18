package com.example.healzone.controller.patient;

import com.example.healzone.repository.AppointmentRepository;
import com.example.healzone.repository.DoctorRepository;
import com.example.healzone.model.DoctorCardDataModel;
import com.example.healzone.model.TimeSlotModel;
import com.example.healzone.model.PatientModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.example.healzone.repository.AppointmentRepository.getAppointmentNo;
import static com.example.healzone.repository.AppointmentRepository.insertNewAppointment;

public class PatientAppointmentBookingController {
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

    private DoctorCardDataModel doctorData;

    @FXML
    protected void initialize() {
        setupInitialScrollPaneStyle();
    }

    public void setDataForConfirmAppointment(DoctorCardDataModel data) {
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
        patientNameField.setText(PatientModel.getName());
        patientAgeField.setText(PatientModel.getAge());
        patientPhoneField.setText(PatientModel.getPhone());
        startTimeConfirm.setText(data.getStartTime());
        endTimeConfirm.setText(data.getEndTime());

        // Apply circular clip to doctorProfileImage
        Circle clip = new Circle();
        clip.setRadius(40); // Half of fitWidth/fitHeight (80/2)
        clip.centerXProperty().bind(doctorProfileImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(doctorProfileImage.fitHeightProperty().divide(2));
        doctorProfileImage.setClip(clip);

        // Fetch doctor's availability
        Map<String, TimeSlotModel> availability = DoctorRepository.getDoctorAvailability(data.getGovtId());
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

        // Get current date and time
        LocalDate today = LocalDate.now(); // June 17, 2025
        LocalTime currentTime = LocalTime.now(); // 08:52 PM PKT
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a"); // Pattern for "12:00 PM"
        LocalTime doctorEndTime = LocalTime.parse(data.getEndTime(), timeFormatter); // Parse 12-hour format

        // Define date range (start from tomorrow if after end time, otherwise today)
        LocalDate startDate = currentTime.isAfter(doctorEndTime) ? today.plusDays(1) : today;
        LocalDate endDate = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).plusDays(7); // June 22, 2025

        // Restrict DatePicker based on current time and doctor availability
        appointmentDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
                boolean isBeforeStart = date.isBefore(startDate);
                boolean isAfterEnd = date.isAfter(endDate);
                boolean isAvailableDay = availableDays.contains(dayOfWeek);

                // Disable if empty, before start date, after end date, or not an available day
                setDisable(empty || isBeforeStart || isAfterEnd || !isAvailableDay);

                // Additional check for today if after doctor's end time
                if (date.isEqual(today) && currentTime.isAfter(doctorEndTime)) {
                    setDisable(true);
                }
            }
        });

        // Update labels when date is selected
        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedDateLabel.setText(newVal.toString());
                selectedDayLabel.setText(newVal.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US));
                selectedTimeLabel.setText(data.getStartTime() + "—" + data.getEndTime());
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
                showAlert("Error", "Selected date is in the past. Please select a future date.");
                return;
            }

            if (selectedDate.isAfter(endDate)) {
                showAlert("Error", "Selected date is beyond the allowed period (up to June 22, 2025).");
                return;
            }

            // Validate input fields
            String patientName = patientNameField.getText().trim();
            String patientPhone = patientPhoneField.getText().trim();
            String patientAge = patientAgeField.getText().trim();
            String description = descriptionField.getText().trim();

            if (patientName.isEmpty() || patientPhone.isEmpty() || patientAge.isEmpty()) {
                showAlert("Error", "Patient name, phone, and age are required.");
                return;
            }

            if (!patientPhone.matches("\\d{11}")) {
                showAlert("Error", "Phone number must be 11 digits.");
                return;
            }

            // Generate unique appointment number
            String appointmentNumber = getAppointmentNo(data.getGovtId());

            // Insert appointment with status "Upcoming"
            insertNewAppointment(
                    data.getGovtId(),
                    appointmentNumber,
                    patientPhone,
                    patientName,
                    selectedDate,
                    "Upcoming"
            );

            // Verify insertion
            Map<String, Object> insertedAppointment = AppointmentRepository.getAppointmentById(data.getGovtId(), appointmentNumber);
            if (insertedAppointment != null) {
                showAlert("Success", "Appointment booked successfully! Appointment No: " + appointmentNumber);
                ((Stage) confirmationContainer.getScene().getWindow()).close();
            } else {
                showAlert("Error", "Failed to book appointment. Please try again.");
            }
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
        Stage popupStage = (Stage) confirmationContainer.getScene().getWindow();
        alert.initOwner(popupStage);
        alert.showAndWait();
    }
}