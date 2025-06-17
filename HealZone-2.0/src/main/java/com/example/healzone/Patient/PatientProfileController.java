package com.example.healzone.Patient;

import com.example.healzone.DatabaseConnection.Patients;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PatientProfileController {

    @FXML private TextField nameField;
    @FXML private TextField fatherNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private HBox actionButtonsContainer;
    @FXML private Button updateProfileButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Initialize gender combo box
        genderComboBox.getItems().addAll("Male", "Female", "Other");

        // Populate fields with current patient data
        nameField.setText(Patient.getName() != null ? Patient.getName() : "");
        fatherNameField.setText(Patient.getFatherName() != null ? Patient.getFatherName() : "");
        phoneNumberField.setText(Patient.getPhone() != null ? Patient.getPhone() : "");
        emailField.setText(Patient.getEmail() != null ? Patient.getEmail() : "");
        ageField.setText(Patient.getAge() != null ? Patient.getAge() : "");
        genderComboBox.setValue(Patient.getGender() != null ? Patient.getGender() : "");

        // Set initial state
        setEditMode(false);

        // Log initialization time (for debugging)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        System.out.println("PatientProfileController initialized at: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @FXML
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);
    }

    private void setEditMode(boolean editMode) {
        nameField.setEditable(editMode);
        fatherNameField.setEditable(editMode);
        phoneNumberField.setEditable(false); // Phone number is primary key, should not be editable
        emailField.setEditable(editMode);
        ageField.setEditable(editMode);
        genderComboBox.setDisable(!editMode);
        currentPasswordField.setEditable(editMode);
        newPasswordField.setEditable(editMode);
        actionButtonsContainer.setVisible(editMode);
        updateProfileButton.setText(editMode ? "Back to View" : "Update Profile");
    }

    @FXML
    private void saveProfile() {
        // Validate and update Patient object
        String phoneNumber = Patient.getPhone();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            System.err.println("Error: No patient logged in.");
            return;
        }

        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Error", "Name cannot be empty.");
            return;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("Error", "Email cannot be empty.");
            return;
        }

        // Validate age (should be numeric)
        String ageText = ageField.getText().trim();
        if (!ageText.isEmpty()) {
            try {
                int age = Integer.parseInt(ageText);
                if (age < 0 || age > 150) {
                    showAlert("Error", "Please enter a valid age (0-150).");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Age must be a valid number.");
                return;
            }
        }

        // Update Patient object
        Patient.setName(nameField.getText().trim());
        Patient.setFatherName(fatherNameField.getText().trim());
        Patient.setEmail(emailField.getText().trim());
        Patient.setAge(ageField.getText().trim());
        Patient.setGender(genderComboBox.getValue());

        // Handle password update if new password is provided
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (!newPassword.isEmpty() && !currentPassword.isEmpty()) {
            // Verify current password before updating
            if (Patients.verifyPassword(phoneNumber, currentPassword)) {
                Patients.resetPassword(newPassword, Patient.getEmail());
                currentPasswordField.clear();
                newPasswordField.clear();
                showAlert("Success", "Password updated successfully.");
            } else {
                showAlert("Error", "Current password is incorrect.");
                return;
            }
        }

        // Save to database
        if (saveToDatabase()) {
            // Refresh patient data from database
            refreshPatientData(phoneNumber);

            // Update displayName on homepage
            updateHomepageDisplayName();

            showAlert("Success", "Profile updated successfully.");
            setEditMode(false);
        } else {
            showAlert("Error", "Failed to update profile. Please try again.");
        }
    }

    private boolean saveToDatabase() {
        String phoneNumber = Patient.getPhone();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            System.err.println("Error: No patient logged in.");
            return false;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("Saving profile to database for phoneNumber: " + phoneNumber + " at " + timestamp);

        try {
            // Update patient details in database
            return Patients.updatePatientDetails(
                    phoneNumber,
                    Patient.getName(),
                    Patient.getFatherName(),
                    Patient.getEmail(),
                    Patient.getAge(),
                    Patient.getGender()
            );
        } catch (Exception e) {
            System.err.println("Error saving patient profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void refreshPatientData(String phoneNumber) {
        // Assuming email and password are used for verification (adjust based on your login logic)
        String email = Patient.getEmail(); // Use current email, which should be updated
        String password = Patient.getPassword(); // Use current password
        if (email != null && password != null) {
            Patients.getCurrentPatientDetails(email, password); // This updates the static Patient fields
        } else {
            System.err.println("Error: Email or password is null for refreshing patient data.");
        }
    }

    private void updateHomepageDisplayName() {
        // Assuming PatientHomePageController is accessible or we can use a static method/event
        // For simplicity, we'll use a static reference or assume the controller is passed
        // A better approach would be using an event bus or dependency injection
        if (PatientHomePageController.getInstance() != null) {
            PatientHomePageController.getInstance().updateDisplayName(Patient.getName());
        }
    }

    @FXML
    private void cancelEdit() {
        setEditMode(false);
        initialize(); // Reload original data
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setter methods to allow other controllers to set initial values
    public void setNameField(String text) {
        nameField.setText(text);
    }

    public void setFatherNameField(String text) {
        fatherNameField.setText(text);
    }

    public void setPhoneNumberField(String text) {
        phoneNumberField.setText(text);
    }

    public void setEmailField(String text) {
        emailField.setText(text);
    }

    public void setAgeField(String text) {
        ageField.setText(text);
    }

    public void setGenderComboBox(String value) {
        genderComboBox.setValue(value);
    }
}