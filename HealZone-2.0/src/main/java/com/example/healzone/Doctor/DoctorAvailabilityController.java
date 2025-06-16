package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Doctors;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class DoctorAvailabilityController {

    // FXML Components
    @FXML private ScrollPane availabilityScrollPane;
    @FXML private Button updateAvailabilityButton;
    @FXML private FontIcon updateIcon;
    @FXML private VBox currentAvailabilityContainer;
    @FXML private Label noAvailabilityLabel;
    @FXML private GridPane weeklyScheduleGrid;
    @FXML private VBox templatesSection;
    @FXML private HBox actionButtonsContainer;
    @FXML private Label statusMessage;

    // Monday controls
    @FXML private ComboBox<String> mondayAvailableCombo;
    @FXML private TextField mondayStartField;
    @FXML private TextField mondayEndField;
    @FXML private Button mondayDeleteButton;

    // Tuesday controls
    @FXML private ComboBox<String> tuesdayAvailableCombo;
    @FXML private TextField tuesdayStartField;
    @FXML private TextField tuesdayEndField;
    @FXML private Button tuesdayDeleteButton;

    // Wednesday controls
    @FXML private ComboBox<String> wednesdayAvailableCombo;
    @FXML private TextField wednesdayStartField;
    @FXML private TextField wednesdayEndField;
    @FXML private Button wednesdayDeleteButton;

    // Thursday controls
    @FXML private ComboBox<String> thursdayAvailableCombo;
    @FXML private TextField thursdayStartField;
    @FXML private TextField thursdayEndField;
    @FXML private Button thursdayDeleteButton;

    // Friday controls
    @FXML private ComboBox<String> fridayAvailableCombo;
    @FXML private TextField fridayStartField;
    @FXML private TextField fridayEndField;
    @FXML private Button fridayDeleteButton;

    // Saturday controls
    @FXML private ComboBox<String> saturdayAvailableCombo;
    @FXML private TextField saturdayStartField;
    @FXML private TextField saturdayEndField;
    @FXML private Button saturdayDeleteButton;

    // Sunday controls
    @FXML private ComboBox<String> sundayAvailableCombo;
    @FXML private TextField sundayStartField;
    @FXML private TextField sundayEndField;
    @FXML private Button sundayDeleteButton;

    // Template buttons
    @FXML private Button mondayToFridayButton;
    @FXML private Button allWeekButton;
    @FXML private Button morningShiftButton;
    @FXML private Button eveningShiftButton;
    @FXML private Button clearAllButton;

    // Action buttons
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private boolean isEditMode = false;
    private Map<String, ComboBox<String>> dayComboMap = new HashMap<>();
    private Map<String, TextField> startTimeMap = new HashMap<>();
    private Map<String, TextField> endTimeMap = new HashMap<>();
    private Map<String, Button> deleteButtonMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize maps for easier access
        initializeMaps();

        // Populate combo boxes
        populateComboBoxes();

        // Load current availability data
        loadCurrentAvailability();

        // Set initial state (read-only)
        setEditMode(false);

        // Log initialization
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        System.out.println("DoctorAvailabilityController initialized at: " +
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void initializeMaps() {
        // Combo boxes
        dayComboMap.put("Monday", mondayAvailableCombo);
        dayComboMap.put("Tuesday", tuesdayAvailableCombo);
        dayComboMap.put("Wednesday", wednesdayAvailableCombo);
        dayComboMap.put("Thursday", thursdayAvailableCombo);
        dayComboMap.put("Friday", fridayAvailableCombo);
        dayComboMap.put("Saturday", saturdayAvailableCombo);
        dayComboMap.put("Sunday", sundayAvailableCombo);

        // Start time fields
        startTimeMap.put("Monday", mondayStartField);
        startTimeMap.put("Tuesday", tuesdayStartField);
        startTimeMap.put("Wednesday", wednesdayStartField);
        startTimeMap.put("Thursday", thursdayStartField);
        startTimeMap.put("Friday", fridayStartField);
        startTimeMap.put("Saturday", saturdayStartField);
        startTimeMap.put("Sunday", sundayStartField);

        // End time fields
        endTimeMap.put("Monday", mondayEndField);
        endTimeMap.put("Tuesday", tuesdayEndField);
        endTimeMap.put("Wednesday", wednesdayEndField);
        endTimeMap.put("Thursday", thursdayEndField);
        endTimeMap.put("Friday", fridayEndField);
        endTimeMap.put("Saturday", saturdayEndField);
        endTimeMap.put("Sunday", sundayEndField);

        // Delete buttons
        deleteButtonMap.put("Monday", mondayDeleteButton);
        deleteButtonMap.put("Tuesday", tuesdayDeleteButton);
        deleteButtonMap.put("Wednesday", wednesdayDeleteButton);
        deleteButtonMap.put("Thursday", thursdayDeleteButton);
        deleteButtonMap.put("Friday", fridayDeleteButton);
        deleteButtonMap.put("Saturday", saturdayDeleteButton);
        deleteButtonMap.put("Sunday", sundayDeleteButton);
    }

    private void populateComboBoxes() {
        String[] availabilityOptions = {"Available", "Not Available"};
        for (ComboBox<String> combo : dayComboMap.values()) {
            combo.getItems().addAll(availabilityOptions);
            combo.setValue("Not Available"); // Default value
        }
    }

    private void loadCurrentAvailability() {
        currentAvailabilityContainer.getChildren().clear();

        Map<String, TimeSlot> availability = Doctor.getAvailability();
        if (availability != null && !availability.isEmpty()) {
            noAvailabilityLabel.setVisible(false);
            for (Map.Entry<String, TimeSlot> entry : availability.entrySet()) {
                String day = entry.getKey();
                TimeSlot slot = entry.getValue();

                // Create availability overview item with formatted times
                createAvailabilityOverviewItem(day, slot);

                // Set the values in the grid with formatted times
                setDaySchedule(day, slot);
            }
        } else {
            noAvailabilityLabel.setVisible(true);
        }
    }

    private void createAvailabilityOverviewItem(String day, TimeSlot slot) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTimeStr = slot.getStartTime().format(formatter);
        String endTimeStr = slot.getEndTime().format(formatter);
        Label availabilityLabel = new Label(day + ": " + startTimeStr + " - " + endTimeStr);
        availabilityLabel.getStyleClass().add("availability-overview-item");
        currentAvailabilityContainer.getChildren().add(availabilityLabel);
    }

    private void setDaySchedule(String day, TimeSlot slot) {
        ComboBox<String> combo = dayComboMap.get(day);
        TextField startField = startTimeMap.get(day);
        TextField endField = endTimeMap.get(day);
        Button deleteButton = deleteButtonMap.get(day);

        if (combo != null && startField != null && endField != null) {
            combo.setValue("Available");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            startField.setText(slot.getStartTime().format(formatter)); // Ensure start time is set
            endField.setText(slot.getEndTime().format(formatter));     // Ensure end time is set
            deleteButton.setVisible(true);
        }
    }

    @FXML
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);

        if (isEditMode) {
            updateAvailabilityButton.setText("Cancel Edit");
            updateIcon.setIconLiteral("fas-times");
            showStatusMessage("Edit mode enabled. Make your changes and click Save.", "info");
        } else {
            updateAvailabilityButton.setText("Update Timings");
            updateIcon.setIconLiteral("fas-edit");
            loadCurrentAvailability(); // Reload original data
            hideStatusMessage();
        }
    }

    private void setEditMode(boolean editMode) {
        for (ComboBox<String> combo : dayComboMap.values()) {
            combo.setDisable(!editMode);
        }
        for (TextField field : startTimeMap.values()) {
            field.setEditable(editMode);
        }
        for (TextField field : endTimeMap.values()) {
            field.setEditable(editMode);
        }
        templatesSection.setVisible(editMode);
        actionButtonsContainer.setVisible(editMode);
        updateDeleteButtonsVisibility();
    }

    private void updateDeleteButtonsVisibility() {
        for (Map.Entry<String, Button> entry : deleteButtonMap.entrySet()) {
            String day = entry.getKey();
            Button deleteButton = entry.getValue();
            ComboBox<String> combo = dayComboMap.get(day);
            boolean isAvailable = "Available".equals(combo.getValue());
            deleteButton.setVisible(isEditMode && isAvailable);
        }
    }

    @FXML
    private void saveAvailability() {
        if (!validateSchedule()) {
            return;
        }

        Doctor.clearAvailability();
        for (Map.Entry<String, ComboBox<String>> entry : dayComboMap.entrySet()) {
            String day = entry.getKey();
            ComboBox<String> combo = entry.getValue();
            if ("Available".equals(combo.getValue())) {
                TextField startField = startTimeMap.get(day);
                TextField endField = endTimeMap.get(day);
                try {
                    LocalTime startTime = LocalTime.parse(startField.getText());
                    LocalTime endTime = LocalTime.parse(endField.getText());
                    TimeSlot slot = new TimeSlot(startTime, endTime);
                    Doctor.addAvailability(day, slot);
                } catch (DateTimeParseException e) {
                    showStatusMessage("Invalid time format for " + day + ". Please use HH:MM format.", "error");
                    return;
                }
            }
        }

        saveToDatabase();
        loadCurrentAvailability();
        setEditMode(false);
        isEditMode = false;
        updateAvailabilityButton.setText("Update Timings");
        updateIcon.setIconLiteral("fas-edit");
        showStatusMessage("Availability schedule saved successfully!", "success");
    }

    private boolean validateSchedule() {
        for (Map.Entry<String, ComboBox<String>> entry : dayComboMap.entrySet()) {
            String day = entry.getKey();
            ComboBox<String> combo = entry.getValue();
            if ("Available".equals(combo.getValue())) {
                TextField startField = startTimeMap.get(day);
                TextField endField = endTimeMap.get(day);
                String startText = startField.getText().trim();
                String endText = endField.getText().trim();
                if (startText.isEmpty() || endText.isEmpty()) {
                    showStatusMessage("Please fill in both start and end times for " + day, "error");
                    return false;
                }
                try {
                    LocalTime startTime = LocalTime.parse(startText);
                    LocalTime endTime = LocalTime.parse(endText);
                    if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                        showStatusMessage("Start time must be before end time for " + day, "error");
                        return false;
                    }
                } catch (DateTimeParseException e) {
                    showStatusMessage("Invalid time format for " + day + ". Please use HH:MM format.", "error");
                    return false;
                }
            }
        }
        return true;
    }

    private void saveToDatabase() {
        String govtId = Doctor.getGovtID();
        if (govtId == null) {
            showStatusMessage("Error: No doctor logged in.", "error");
            return;
        }

        // Clear existing availability for the doctor
        clearDatabaseAvailability(govtId);

        // Insert new availability
        for (Map.Entry<String, ComboBox<String>> entry : dayComboMap.entrySet()) {
            String day = entry.getKey();
            ComboBox<String> combo = entry.getValue();
            if ("Available".equals(combo.getValue())) {
                TextField startField = startTimeMap.get(day);
                TextField endField = endTimeMap.get(day);
                try {
                    LocalTime startTime = LocalTime.parse(startField.getText());
                    LocalTime endTime = LocalTime.parse(endField.getText());
                    Doctors.insertAvailabilityOfDoctor(govtId, day, startTime, endTime);
                } catch (DateTimeParseException e) {
                    showStatusMessage("Invalid time format for " + day + " during save.", "error");
                    return;
                }
            }
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Karachi"));
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("Availability saved to database for govtId: " + govtId + " at " + timestamp);
    }

    private void clearDatabaseAvailability(String govtId) {
        String deleteSql = "DELETE FROM doctor_availability WHERE govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, govtId);
            stmt.executeUpdate();
            System.out.println("Old availability cleared for govtId: " + govtId);
        } catch (SQLException e) {
            System.err.println("Error clearing old availability: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelEdit() {
        setEditMode(false);
        isEditMode = false;
        updateAvailabilityButton.setText("Update Timings");
        updateIcon.setIconLiteral("fas-edit");
        loadCurrentAvailability();
        hideStatusMessage();
    }

    @FXML
    private void applyMondayToFridayTemplate() {
        applyTemplate(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}, "09:00", "17:00");
    }

    @FXML
    private void applyAllWeekTemplate() {
        applyTemplate(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}, "09:00", "17:00");
    }

    @FXML
    private void applyMorningShiftTemplate() {
        applyTemplate(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}, "08:00", "14:00");
    }

    @FXML
    private void applyEveningShiftTemplate() {
        applyTemplate(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}, "14:00", "20:00");
    }

    private void applyTemplate(String[] days, String startTime, String endTime) {
        for (String day : days) {
            ComboBox<String> combo = dayComboMap.get(day);
            TextField startField = startTimeMap.get(day);
            TextField endField = endTimeMap.get(day);
            if (combo != null && startField != null && endField != null) {
                combo.setValue("Available");
                startField.setText(startTime);
                endField.setText(endTime);
            }
        }
        updateDeleteButtonsVisibility();
        showStatusMessage("Template applied successfully!", "info");
    }

    @FXML
    private void clearAllSchedule() {
        for (ComboBox<String> combo : dayComboMap.values()) {
            combo.setValue("Not Available");
        }
        for (TextField field : startTimeMap.values()) {
            field.clear();
        }
        for (TextField field : endTimeMap.values()) {
            field.clear();
        }
        updateDeleteButtonsVisibility();
        showStatusMessage("All schedules cleared!", "info");
    }

    @FXML private void clearMondaySchedule() { clearDaySchedule("Monday"); }
    @FXML private void clearTuesdaySchedule() { clearDaySchedule("Tuesday"); }
    @FXML private void clearWednesdaySchedule() { clearDaySchedule("Wednesday"); }
    @FXML private void clearThursdaySchedule() { clearDaySchedule("Thursday"); }
    @FXML private void clearFridaySchedule() { clearDaySchedule("Friday"); }
    @FXML private void clearSaturdaySchedule() { clearDaySchedule("Saturday"); }
    @FXML private void clearSundaySchedule() { clearDaySchedule("Sunday"); }

    private void clearDaySchedule(String day) {
        ComboBox<String> combo = dayComboMap.get(day);
        TextField startField = startTimeMap.get(day);
        TextField endField = endTimeMap.get(day);
        if (combo != null && startField != null && endField != null) {
            combo.setValue("Not Available");
            startField.clear();
            endField.clear();
        }
        updateDeleteButtonsVisibility();
        showStatusMessage(day + " schedule cleared!", "info");
    }

    private void showStatusMessage(String message, String type) {
        statusMessage.setText(message);
        statusMessage.setVisible(true);
        statusMessage.getStyleClass().removeAll("status-success", "status-error", "status-info");
        switch (type) {
            case "success": statusMessage.getStyleClass().add("status-success"); break;
            case "error": statusMessage.getStyleClass().add("status-error"); break;
            case "info": statusMessage.getStyleClass().add("status-info"); break;
        }
        if (!"error".equals(type)) {
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> hideStatusMessage())
            );
            timeline.play();
        }
    }

    private void hideStatusMessage() {
        statusMessage.setVisible(false);
        statusMessage.setText("");
    }
}