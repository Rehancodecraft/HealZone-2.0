package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Doctors;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class DoctorProfileController {

    @FXML private TextField govtIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField specializationField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField experienceField;
    @FXML private TextField degreesField;
    @FXML private TextArea bioTextArea;
    @FXML private TextField hospitalNameField;
    @FXML private TextField consultationFeeField;
    @FXML private TextArea hospitalAddressArea;
    @FXML private VBox availabilityContainer;
    @FXML private ComboBox<String> dayComboBox;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private HBox addAvailabilityBox;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private HBox actionButtonsContainer;
    @FXML private Button updateProfileButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Populate fields with current doctor data
        govtIdField.setText(Doctor.getGovtID() != null ? Doctor.getGovtID() : "");
        firstNameField.setText(Doctor.getFirstName() != null ? Doctor.getFirstName() : "");
        lastNameField.setText(Doctor.getLastName() != null ? Doctor.getLastName() : "");
        emailField.setText(Doctor.getEmail() != null ? Doctor.getEmail() : "");
        phoneNumberField.setText(Doctor.getPhone() != null ? Doctor.getPhone() : "");
        specializationField.setText(Doctor.getSpecialization() != null ? Doctor.getSpecialization() : "");
        licenseNumberField.setText(Doctor.getMedicalLicenseNumber() != null ? Doctor.getMedicalLicenseNumber() : "");
        experienceField.setText(Doctor.getExperience() != null ? Doctor.getExperience() : "");
        degreesField.setText(Doctor.getDegrees() != null ? Doctor.getDegrees() : "");
        bioTextArea.setText(Doctor.getBio() != null ? Doctor.getBio() : "");
        hospitalNameField.setText(Doctor.getHospitalName() != null ? Doctor.getHospitalName() : "");
        consultationFeeField.setText(Doctor.getConsultationFee() != null ? Doctor.getConsultationFee() : "");
        hospitalAddressArea.setText(Doctor.getHospitalAddress() != null ? Doctor.getHospitalAddress() : "");

        // Initialize day combo box
//        dayComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        // Load availability
//        loadAvailability();

        // Set initial state
        setEditMode(false);

        // Log initialization time (for debugging)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        System.out.println("DoctorProfileController initialized at: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

//    private void loadAvailability() {
//        availabilityContainer.getChildren().clear();
//        Map<String, TimeSlot> availability = Doctor.getAvailability();
//        if (availability != null && !availability.isEmpty()) {
//            for (Map.Entry<String, TimeSlot> entry : availability.entrySet()) {
//                String day = entry.getKey();
//                TimeSlot slot = entry.getValue();
//                VBox availabilityItem = createAvailabilityItem(day, slot.getStartTime().toString(), slot.getEndTime().toString());
//                availabilityContainer.getChildren().add(availabilityItem);
//            }
//        }
//    }

    private VBox createAvailabilityItem(String day, String startTime, String endTime) {
        VBox item = new VBox(5);
        item.getStyleClass().add("availability-item");
        Label label = new Label(day + ": " + startTime + " - " + endTime);
        label.getStyleClass().add("availability-label");
        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setOnAction(e -> {
            Doctor.getAvailability().remove(day);
            availabilityContainer.getChildren().remove(item);
        });
        item.getChildren().addAll(label, removeButton);
        return item;
    }

    @FXML
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);
    }

    private void setEditMode(boolean editMode) {
        govtIdField.setEditable(false); // Always non-editable
        firstNameField.setEditable(editMode);
        lastNameField.setEditable(editMode);
        emailField.setEditable(editMode);
        phoneNumberField.setEditable(editMode);
        specializationField.setEditable(editMode);
        licenseNumberField.setEditable(editMode);
        experienceField.setEditable(editMode);
        degreesField.setEditable(editMode);
        bioTextArea.setEditable(editMode);
        hospitalNameField.setEditable(editMode);
        consultationFeeField.setEditable(editMode);
        hospitalAddressArea.setEditable(editMode);
        currentPasswordField.setEditable(editMode);
        newPasswordField.setEditable(editMode);
//        addAvailabilityBox.setVisible(editMode);
        actionButtonsContainer.setVisible(editMode);
        updateProfileButton.setText(editMode ? "Back to View" : "Update Profile");
    }

    @FXML
    private void addAvailability() {
        String day = dayComboBox.getValue();
        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();

        if (day != null && !startTime.isEmpty() && !endTime.isEmpty()) {
            try {
                TimeSlot slot = new TimeSlot(LocalTime.parse(startTime), LocalTime.parse(endTime));
                Doctor.addAvailability(day, slot);
                VBox availabilityItem = createAvailabilityItem(day, startTime, endTime);
                availabilityContainer.getChildren().add(availabilityItem);
                dayComboBox.setValue(null);
                startTimeField.clear();
                endTimeField.clear();
            } catch (DateTimeParseException e) {
                System.err.println("Invalid time format: " + e.getMessage());
                // Add user feedback (e.g., alert) if needed
            }
        }
    }

    @FXML
    private void saveProfile() {
        // Validate and update Doctor object
        String govtId = Doctor.getGovtID();
        if (govtId == null) {
            System.err.println("Error: No doctor logged in.");
            return;
        }

        Doctor.setFirstName(firstNameField.getText());
        Doctor.setLastName(lastNameField.getText());
        Doctor.setEmail(emailField.getText());
        Doctor.setPhone(phoneNumberField.getText());
        Doctor.setSpecialization(specializationField.getText());
        Doctor.setMedicalLicenseNumber(licenseNumberField.getText());
        Doctor.setExperience(experienceField.getText());
        Doctor.setDegrees(degreesField.getText());
        Doctor.setBio(bioTextArea.getText());
        Doctor.setHospitalName(hospitalNameField.getText());
        Doctor.setConsultationFee(consultationFeeField.getText());
        Doctor.setHospitalAddress(hospitalAddressArea.getText());

        // Handle password update if new password is provided
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (!newPassword.isEmpty() && !currentPassword.isEmpty()) {
            // Add logic to verify current password (e.g., query database)
            // Placeholder: Assume verification is done elsewhere
            Doctors.resetPassword(newPassword, Doctor.getEmail());
            currentPasswordField.clear();
            newPasswordField.clear();
        }

        // Save to database
        saveToDatabase();

        // Reload availability to reflect any manual additions
//        loadAvailability();
        setEditMode(false);
    }

    private void saveToDatabase() {
        String govtId = Doctor.getGovtID();
        if (govtId == null) {
            System.err.println("Error: No doctor logged in.");
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("Saving profile to database for govtId: " + govtId + " at " + timestamp);

        // Update personal details
        Doctors.updatePersonalDetails(govtId, Doctor.getFirstName(), Doctor.getLastName(), Doctor.getEmail(), Doctor.getPhone());

        // Update professional details
        Doctors.updateProfessionalDetails(govtId, Doctor.getSpecialization(), Doctor.getDegrees(), Doctor.getMedicalLicenseNumber(), Doctor.getBio(), Doctor.getExperience());

        // Update practice information
        Doctors.updatePracticeInfo(govtId, Doctor.getHospitalName(), Doctor.getHospitalAddress(), Doctor.getConsultationFee());

        // Update availability (clear existing and insert new)
        Doctors.clearDoctorAvailability(govtId);
        Doctor.getAvailability().forEach((day, slot) ->
                Doctors.insertAvailabilityOfDoctor(govtId, day, slot.getStartTime(), slot.getEndTime())
        );
    }

    @FXML
    private void cancelEdit() {
        setEditMode(false);
        initialize(); // Reload original data
    }

    // Added setter methods to allow DoctorHomePageController to set initial values
    public void setGovtIdField(String text) { govtIdField.setText(text); }
    public void setFirstNameField(String text) { firstNameField.setText(text); }
    public void setLastNameField(String text) { lastNameField.setText(text); }
    public void setEmailField(String text) { emailField.setText(text); }
    public void setPhoneNumberField(String text) { phoneNumberField.setText(text); }
    public void setSpecializationField(String text) { specializationField.setText(text); }
    public void setLicenseNumberField(String text) { licenseNumberField.setText(text); }
    public void setExperienceField(String text) { experienceField.setText(text); }
    public void setDegreesField(String text) { degreesField.setText(text); }
    public void setBioTextArea(String text) { bioTextArea.setText(text); }
    public void setHospitalNameField(String text) { hospitalNameField.setText(text); }
    public void setConsultationFeeField(String text) { consultationFeeField.setText(text); }
    public void setHospitalAddressArea(String text) { hospitalAddressArea.setText(text); }

    public void addAvailabilityItem(String day, String startTime, String endTime) {
        try {
            TimeSlot slot = new TimeSlot(LocalTime.parse(startTime), LocalTime.parse(endTime));
            Doctor.addAvailability(day, slot);
            VBox availabilityItem = createAvailabilityItem(day, startTime, endTime);
            availabilityContainer.getChildren().add(availabilityItem);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + e.getMessage());
            // Add user feedback (e.g., alert) if needed
        }
    }
}