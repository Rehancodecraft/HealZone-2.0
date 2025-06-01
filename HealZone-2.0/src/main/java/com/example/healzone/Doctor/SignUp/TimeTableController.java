package com.example.healzone.Doctor.SignUp;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.Doctor.TimeSlot;
import com.example.healzone.StartView.MainViewController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TimeTableController {
    //TimeTable
    @FXML
    private CheckBox mon,tue,wed,thur,fri,sat,sun;
    @FXML
    private ComboBox startTime;
    @FXML
    private ComboBox endTime;
    @FXML
    private TextArea bio;
    @FXML
    private Label timeTableErrorMessage;
    @FXML
    public void onTimeTableNextButtonClicked(javafx.event.ActionEvent event){
        if (bio.getText().isEmpty()) {
            timeTableErrorMessage.setText("⚠️ Please fill in all fields!");
            return;
        }else if (startTime.getValue() == null || endTime.getValue() == null) {
            timeTableErrorMessage.setText("⚠️ Time slots can't be empty!");
            return;
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime start = LocalTime.parse((String) startTime.getValue(), timeFormatter);
        LocalTime end = LocalTime.parse((String) endTime.getValue(), timeFormatter);
        if (end.isBefore(start) || end.equals(start)) {
            timeTableErrorMessage.setText("⚠️ End time must be after start time!");
            return;
        }
        Map<CheckBox, String> dayMap = Map.of(
                mon, "Monday",
                tue, "Tuesday",
                wed, "Wednesday",
                thur, "Thursday",
                fri, "Friday",
                sat, "Saturday",
                sun, "Sunday"
        );
        boolean atLeastOneDaySelected = false;
        for (Map.Entry<CheckBox, String> entry : dayMap.entrySet()) {
            CheckBox cb = entry.getKey();
            String day = entry.getValue();
            if (cb.isSelected()) {
                atLeastOneDaySelected = true;
                // ✅ Store availability temporarily in memory
                Doctor.addAvailability(day, new TimeSlot(start, end));
            }
        }
        if (!atLeastOneDaySelected) {
            timeTableErrorMessage.setText("⚠️ Please select at least one day!");
            return;
        }
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorSecurity();  // next step
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading next scene: ");
            e.printStackTrace();
        }
    }
    public void onTimeTableBackButtonClicked(javafx.event.ActionEvent event){
        try {
            StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
            MainViewController mainController = (MainViewController) root.getProperties().get("controller");

            if (mainController != null) {
                mainController.loadDoctorPracticeInformation();
            } else {
                System.err.println("MainViewController not found in root properties.");
            }
        } catch (Exception e) {
            System.err.println("Error loading PatientLogin: ");
            e.printStackTrace();
        }
    }

}
