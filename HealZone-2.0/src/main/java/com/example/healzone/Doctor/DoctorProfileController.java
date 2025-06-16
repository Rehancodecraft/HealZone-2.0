package com.example.healzone.Doctor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.time.LocalTime;
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
        dayComboBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

        // Load availability
        loadAvailability();

        // Set initial state
        setEditMode(false);
    }

    private void loadAvailability() {
        availabilityContainer.getChildren().clear();
        Map<String, TimeSlot> availability = Doctor.getAvailability();
        if (availability != null && !availability.isEmpty()) {
            for (Map.Entry<String, TimeSlot> entry : availability.entrySet()) {
                String day = entry.getKey();
                TimeSlot slot = entry.getValue();
                VBox availabilityItem = createAvailabilityItem(day, slot.getStartTime().toString(), slot.getEndTime().toString());
                availabilityContainer.getChildren().add(availabilityItem);
            }
        }
    }

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
        addAvailabilityBox.setVisible(editMode);
        actionButtonsContainer.setVisible(editMode);
        updateProfileButton.setText(editMode ? "Back to View" : "Update Profile");
    }

    @FXML
    private void addAvailability() {
        String day = dayComboBox.getValue();
        String startTime = startTimeField.getText();
        String endTime = endTimeField.getText();

        if (day != null && !startTime.isEmpty() && !endTime.isEmpty()) {
            TimeSlot slot = new TimeSlot(LocalTime.parse(startTime), LocalTime.parse(endTime));
            Doctor.addAvailability(day, slot);
            VBox availabilityItem = createAvailabilityItem(day, startTime, endTime);
            availabilityContainer.getChildren().add(availabilityItem);
            dayComboBox.setValue(null);
            startTimeField.clear();
            endTimeField.clear();
        }
    }

    @FXML
    private void saveProfile() {
        // Update Doctor object with new values
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
        // Password update logic can be added here if needed

        // Save to database (implement in Doctors class if not already present)
        saveToDatabase();

        setEditMode(false);
    }

    private void saveToDatabase() {
        // Implement database update logic here
        // Example: Call Doctors.updateDoctorProfile(Doctor.getGovtID(), ...);
        // This is a placeholder; you need to implement the actual database update
        System.out.println("Saving profile to database for govtId: " + Doctor.getGovtID());
    }

    @FXML
    private void cancelEdit() {
        setEditMode(false);
        initialize(); // Reload original data
    }
}

// Helper class for TimeSlo