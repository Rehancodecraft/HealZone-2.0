//package com.example.healzone.Doctor;
//
//import com.example.healzone.Patient.ConfirmAppointmentController;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//
//import java.io.IOException;
//
//public class DoctorCardsController {
//    @FXML
//    private Button bookAppointmentButton;
//    @FXML
//    private Label doctorName;
//    @FXML
//    private Label speciality;
//    @FXML
//    private Label degrees;
//    @FXML
//    private Label experience;
//    @FXML
//    private Label hospitalName;
//    @FXML
//    private Label hospitalAddress;
//    @FXML
//    private Label fee;
//    @FXML
//    private Label startTime;
//    @FXML
//    private Label endTime;
//    @FXML
//    private Label reviews;
//    @FXML
//    private Label rating;
//    @FXML
//    private Label satisfaction;
//
//
//    @FXML
//    public void onClickBookAppointmentButton() {
//        try {
//            // Load the ConfirmAppointment.fxml
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/ConfirmAppointment.fxml"));
//            Pane confirmationPane = loader.load();
//
//            // Get the controller for ConfirmAppointment
//            ConfirmAppointmentController controller = loader.getController();
//            controller.setDataForConfirmAppointment(doctorData); // Pass doctor data to the popup
//
//            // Create a new stage for the popup
//            Stage popupStage = new Stage();
//            popupStage.initStyle(StageStyle.TRANSPARENT); // No window decorations
//            popupStage.setScene(new Scene(confirmationPane));
//            popupStage.setAlwaysOnTop(true);
//            popupStage.setResizable(false);
//
//            // Center the popup on the screen
//            popupStage.centerOnScreen();
//
//            // Add global mouse event handler to close popup on outside click
//            Scene mainScene = bookAppointmentButton.getScene();
//            mainScene.getRoot().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
//                if (!confirmationPane.contains(event.getSceneX(), event.getSceneY())) {
//                    popupStage.close();
//                }
//            });
//
//            // Show the popup
//            popupStage.show();
//
//            // Ensure the popup closes when the main window is closed
//            popupStage.setOnHidden(e -> mainScene.getRoot().removeEventFilter(MouseEvent.MOUSE_PRESSED, event -> {}));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    public void setDoctorData(DoctorCardData data) {
//        doctorName.setText(data.getFullName());
//        speciality.setText(data.getSpecialization());
//        degrees.setText(data.getDegrees());
//        experience.setText(data.getExperience());
//        hospitalName.setText(data.getHospitalName());
//        hospitalAddress.setText(data.getHospitalAddress());
//        fee.setText(data.getConsultationFee());
//        startTime.setText(data.getStartTime());
//        endTime.setText(data.getEndTime());
//        reviews.setText("Reviews: " + (data.getReviews() != null ? data.getReviews().size() : 0));
//        rating.setText(String.format("%.1f", data.getAverageRating()) + "/5");
//        satisfaction.setText(String.format("%.0f", data.getSatisfactionScore()) + "%");
//    }
//}
package com.example.healzone.Doctor;

import com.example.healzone.Patient.ConfirmAppointmentController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DoctorCardsController {
    @FXML
    private Button bookAppointmentButton;
    @FXML
    private Label doctorName;
    @FXML
    private Label speciality;
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

    private DoctorCardData doctorData; // Store the specific doctor data for this card

    public void setDoctorData(DoctorCardData data) {
        this.doctorData = data; // Set the doctor data for this instance
        doctorName.setText(data.getFullName());
        speciality.setText(data.getSpecialization());
        degrees.setText(data.getDegrees());
        experience.setText("Experience: " + data.getExperience() + " Yrs");
        hospitalName.setText("Hospital: " + data.getHospitalName());
        hospitalAddress.setText("Address: " + data.getHospitalAddress());
        fee.setText("Fee: Rs. " + data.getConsultationFee());
        startTime.setText("Available: " + data.getStartTime());
        endTime.setText(data.getEndTime());
        reviews.setText("Reviews: " + (data.getReviews() != null ? data.getReviews().size() : 0));
        rating.setText("Rating: " + String.format("%.1f", data.getAverageRating()) + "/5");
        satisfaction.setText("Satisfaction: " + String.format("%.0f", data.getSatisfactionScore()) + "%");
    }

    @FXML
    public void onClickBookAppointmentButton() {
        if (doctorData == null) {
            System.err.println("No doctor data available for this card!");
            return;
        }

        try {
            // Load the ConfirmAppointment.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/ConfirmApppointment.fxml")); // Corrected path
            ScrollPane confirmationScrollPane = loader.load(); // Cast to ScrollPane

            // Get the controller for ConfirmAppointment and pass the doctor data
            ConfirmAppointmentController controller = loader.getController();
            controller.setDataForConfirmAppointment(doctorData); // Pass the specific doctor data

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.TRANSPARENT); // No window decorations
            popupStage.setScene(new Scene(confirmationScrollPane));
            popupStage.setAlwaysOnTop(true);
            popupStage.setResizable(false);

            // Center the popup on the screen
            popupStage.centerOnScreen();

            // Add global mouse event handler to close popup on outside click
            Scene mainScene = bookAppointmentButton.getScene();
            mainScene.getRoot().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                // Check if the click is outside the ScrollPane content
                if (!confirmationScrollPane.getContent().contains(event.getSceneX(), event.getSceneY())) {
                    popupStage.close();
                }
            });

            // Show the popup
            popupStage.show();

            // Ensure the popup closes when the main window is closed
            popupStage.setOnHidden(e -> mainScene.getRoot().removeEventFilter(MouseEvent.MOUSE_PRESSED, event -> {}));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}