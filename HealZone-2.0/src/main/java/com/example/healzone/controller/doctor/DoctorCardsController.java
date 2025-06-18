package com.example.healzone.controller.doctor;

import com.example.healzone.model.DoctorCardDataModel;
import com.example.healzone.controller.patient.PatientAppointmentBookingController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DoctorCardsController {
    @FXML
    private Button bookAppointmentButton;
    @FXML
    private Button viewProfileButton;
    @FXML
    private Label doctorName;
    @FXML
    private Label specialty;
    @FXML
    private Label degrees;
    @FXML
    private Label experience;
    @FXML
    private Label hospitalName;
    @FXML
    private Label hospitalAddress;
    @FXML
    private Label fee;
    @FXML
    private Label startTime;
    @FXML
    private Label endTime;
    @FXML
    private Label reviews;
    @FXML
    private Label rating;
    @FXML
    private Label satisfaction;
    @FXML
    private Label mon;
    @FXML
    private Label tue;
    @FXML
    private Label wed;
    @FXML
    private Label thu;
    @FXML
    private Label fri;
    @FXML
    private Label sat;
    @FXML
    private Label sun;
    @FXML
    private ImageView profileImage; // Added for clip application

    private DoctorCardDataModel doctorData;

    public void initialize() {
        // Apply circular clip to profileImage
        Circle clip = new Circle();
        clip.setRadius(50); // Half of fitWidth/fitHeight (100/2)
        clip.centerXProperty().bind(profileImage.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(profileImage.fitHeightProperty().divide(2));
        profileImage.setClip(clip);
    }

    public void setDoctorData(DoctorCardDataModel data) {
        this.doctorData = data;

        // Populate basic fields
        doctorName.setText(data.getFullName());
        specialty.setText(data.getSpecialization());
        degrees.setText(data.getDegrees());
        experience.setText(data.getExperience());
        hospitalName.setText(data.getHospitalName());
        hospitalAddress.setText(data.getHospitalAddress());
        fee.setText(data.getConsultationFee());
        startTime.setText(data.getStartTime());
        endTime.setText(data.getEndTime());
        reviews.setText(String.valueOf((data.getReviews() != null ? data.getReviews().size() : 0)));
        rating.setText(String.format("%.1f", data.getAverageRating()) + "/5");
        satisfaction.setText(String.format("%.0f", data.getSatisfactionScore()) + "%");

        // Handle available days visibility
        setAvailableDays(data.getAvailableDays());
    }

    private void setAvailableDays(String[] availableDays) {
        // Initialize all day labels to invisible
        Map<String, Label> dayLabelMap = new HashMap<>();
        dayLabelMap.put("mon", mon);
        dayLabelMap.put("tue", tue);
        dayLabelMap.put("wed", wed);
        dayLabelMap.put("thu", thu);
        dayLabelMap.put("fri", fri);
        dayLabelMap.put("sat", sat);
        dayLabelMap.put("sun", sun);

        // Set all labels to invisible by default
        dayLabelMap.values().forEach(label -> label.setVisible(false));

        // If no available days, return early
        if (availableDays == null || availableDays.length == 0) {
            System.out.println("No available days for doctor: " + (doctorData != null ? doctorData.getFullName() : "Unknown"));
            return;
        }

        // Normalize and set visible labels for matching days
        for (String day : availableDays) {
            if (day != null) {
                String normalizedDay = day.trim().toLowerCase();
                // Map database day names to FXML label IDs
                String labelKey = switch (normalizedDay) {
                    case "monday", "mon" -> "mon";
                    case "tuesday", "tue" -> "tue";
                    case "wednesday", "wed" -> "wed";
                    case "thursday", "thu" -> "thu";
                    case "friday", "fri" -> "fri";
                    case "saturday", "sat" -> "sat";
                    case "sunday", "sun" -> "sun";
                    default -> "";
                };

                if (!labelKey.isEmpty() && dayLabelMap.containsKey(labelKey)) {
                    dayLabelMap.get(labelKey).setVisible(true);
                    System.out.println("Set " + labelKey + " visible for doctor: " + (doctorData != null ? doctorData.getFullName() : "Unknown"));
                }
            }
        }
    }

    @FXML
    public void onClickBookAppointmentButton() {
        System.out.println("Book Appointment clicked for: " + (doctorData != null ? doctorData.getFullName() : "No data"));
        if (doctorData == null) {
            System.err.println("No doctor data available for this card!");
            return;
        }

        try {
            // Load the ConfirmAppointment.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/ConfirmApppointment.fxml"));
            ScrollPane confirmationScrollPane = loader.load();

            // Get the controller for ConfirmAppointment and pass the doctor data
            PatientAppointmentBookingController controller = loader.getController();
            controller.setDataForConfirmAppointment(doctorData);

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setScene(new Scene(confirmationScrollPane));
            popupStage.setAlwaysOnTop(true);
            popupStage.setResizable(false);

            // Center the popup on the screen
            popupStage.centerOnScreen();

            // Add global mouse event handler to close popup on outside click
            Scene mainScene = bookAppointmentButton.getScene();
            mainScene.getRoot().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!confirmationScrollPane.getContent().contains(event.getSceneX(), event.getSceneY())) {
                    popupStage.close();
                }
            });

            // Show the popup
            popupStage.show();

            // Ensure the popup closes when the main window is closed
            popupStage.setOnHidden(e -> mainScene.getRoot().removeEventFilter(MouseEvent.MOUSE_PRESSED, event -> {}));

        } catch (IOException e) {
            System.err.println("Error loading ConfirmAppointment.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}