package com.example.healzone.controller.doctor;

import com.example.healzone.repository.DoctorRepository;
import com.example.healzone.model.DoctorModel;
import com.example.healzone.model.TimeSlotModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        govtIdField.setText(DoctorModel.getGovtID() != null ? DoctorModel.getGovtID() : "");
        firstNameField.setText(DoctorModel.getFirstName() != null ? DoctorModel.getFirstName() : "");
        lastNameField.setText(DoctorModel.getLastName() != null ? DoctorModel.getLastName() : "");
        emailField.setText(DoctorModel.getEmail() != null ? DoctorModel.getEmail() : "");
        phoneNumberField.setText(DoctorModel.getPhone() != null ? DoctorModel.getPhone() : "");
        specializationField.setText(DoctorModel.getSpecialization() != null ? DoctorModel.getSpecialization() : "");
        licenseNumberField.setText(DoctorModel.getMedicalLicenseNumber() != null ? DoctorModel.getMedicalLicenseNumber() : "");
        experienceField.setText(DoctorModel.getExperience() != null ? DoctorModel.getExperience() : "");
        degreesField.setText(DoctorModel.getDegrees() != null ? DoctorModel.getDegrees() : "");
        bioTextArea.setText(DoctorModel.getBio() != null ? DoctorModel.getBio() : "");
        hospitalNameField.setText(DoctorModel.getHospitalName() != null ? DoctorModel.getHospitalName() : "");
        consultationFeeField.setText(DoctorModel.getConsultationFee() != null ? DoctorModel.getConsultationFee() : "");
        hospitalAddressArea.setText(DoctorModel.getHospitalAddress() != null ? DoctorModel.getHospitalAddress() : "");

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
            DoctorModel.getAvailability().remove(day);
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
                TimeSlotModel slot = new TimeSlotModel(LocalTime.parse(startTime), LocalTime.parse(endTime));
                DoctorModel.addAvailability(day, slot);
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
        String govtId = DoctorModel.getGovtID();
        if (govtId == null) {
            System.err.println("Error: No doctor logged in.");
            return;
        }

        DoctorModel.setFirstName(firstNameField.getText());
        DoctorModel.setLastName(lastNameField.getText());
        DoctorModel.setEmail(emailField.getText());
        DoctorModel.setPhone(phoneNumberField.getText());
        DoctorModel.setSpecialization(specializationField.getText());
        DoctorModel.setMedicalLicenseNumber(licenseNumberField.getText());
        DoctorModel.setExperience(experienceField.getText());
        DoctorModel.setDegrees(degreesField.getText());
        DoctorModel.setBio(bioTextArea.getText());
        DoctorModel.setHospitalName(hospitalNameField.getText());
        DoctorModel.setConsultationFee(consultationFeeField.getText());
        DoctorModel.setHospitalAddress(hospitalAddressArea.getText());

        // Handle password update if new password is provided
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (!newPassword.isEmpty() && !currentPassword.isEmpty()) {
            // Add logic to verify current password (e.g., query database)
            // Placeholder: Assume verification is done elsewhere
            DoctorRepository.resetPassword(newPassword, DoctorModel.getEmail());
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
        String govtId = DoctorModel.getGovtID();
        if (govtId == null) {
            System.err.println("Error: No doctor logged in.");
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("Saving profile to database for govtId: " + govtId + " at " + timestamp);

        // Update personal details
        DoctorRepository.updatePersonalDetails(govtId, DoctorModel.getFirstName(), DoctorModel.getLastName(), DoctorModel.getEmail(), DoctorModel.getPhone());

        // Update professional details
        DoctorRepository.updateProfessionalDetails(govtId, DoctorModel.getSpecialization(), DoctorModel.getDegrees(), DoctorModel.getMedicalLicenseNumber(), DoctorModel.getBio(), DoctorModel.getExperience());

        // Update practice information
        DoctorRepository.updatePracticeInfo(govtId, DoctorModel.getHospitalName(), DoctorModel.getHospitalAddress(), DoctorModel.getConsultationFee());

        // Update availability (clear existing and insert new)
        DoctorRepository.clearDoctorAvailability(govtId);
        DoctorModel.getAvailability().forEach((day, slot) ->
                DoctorRepository.insertAvailabilityOfDoctor(govtId, day, slot.getStartTime(), slot.getEndTime())
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
            TimeSlotModel slot = new TimeSlotModel(LocalTime.parse(startTime), LocalTime.parse(endTime));
            DoctorModel.addAvailability(day, slot);
            VBox availabilityItem = createAvailabilityItem(day, startTime, endTime);
            availabilityContainer.getChildren().add(availabilityItem);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + e.getMessage());
            // Add user feedback (e.g., alert) if needed
        }
    }
}