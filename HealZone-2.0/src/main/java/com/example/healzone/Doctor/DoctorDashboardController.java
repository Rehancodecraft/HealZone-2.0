package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.DatabaseConnection.Prescription;
import com.example.healzone.Patient.PatientHistoryController;
import com.example.healzone.SessionManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class DoctorDashboardController {
    @FXML
    private VBox dashboardPane;
    @FXML
    private Label totalBookedLabel;
    @FXML
    private Label totalAttendedLabel;
    @FXML
    private Label totalRemainingLabel;
    @FXML
    private TextField patientSearchBar;
    @FXML
    private Button searchButton;
    @FXML
    private VBox nextAppointmentPane;
    @FXML
    private Label patientNameLabel;
    @FXML
    private Label appointmentNumberLabel;
    @FXML
    private Label patientPhoneLabel;
    @FXML
    private Button attendButton;
    @FXML
    private Button viewHistoryButton;
    @FXML
    private VBox noAppointmentPane;

    private Map<String, Object> currentAppointment;
    private String doctorId;
    private static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");

    @FXML
    protected void initialize() {
        doctorId = SessionManager.getCurrentDoctorId();
        if (doctorId == null) {
            showErrorAlert("Session Error", "No doctor logged in.");
            return;
        }

        // Check and initialize nextAppointmentPane
        if (nextAppointmentPane == null) {
            System.out.println("Warning: nextAppointmentPane is not initialized. Creating a default VBox.");
            nextAppointmentPane = new VBox(); // Fallback if FXML injection fails
            nextAppointmentPane.setVisible(false); // Default to hidden
        }
        if (noAppointmentPane == null) {
            System.out.println("Warning: noAppointmentPane is not initialized. Creating a default VBox.");
            noAppointmentPane = new VBox(); // Fallback if FXML injection fails
            noAppointmentPane.setVisible(false); // Default to hidden
        }

        loadNextAppointment();

        // Setup search functionality
        searchButton.setOnAction(event -> searchPatient());
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
        patientSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadNextAppointment();
            }
        });
        // Add Enter key handler for search
        patientSearchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchPatient();
            }
        });
    }

    public  void refreshDashboard() {
        // Reload appointments and update UI
        loadNextAppointment(); // This will refresh the next appointment display
        // Optionally, update total booked, attended, and remaining counts if needed
        // For now, assume loadNextAppointment() handles the UI update
    }

    private LocalTime getAvailabilityStartTime() {
        String dayOfWeek = LocalDate.now(PAKISTAN_ZONE).getDayOfWeek().toString().substring(0, 1).toUpperCase() +
                LocalDate.now(PAKISTAN_ZONE).getDayOfWeek().toString().substring(1).toLowerCase();
        String query = "SELECT start_time FROM doctor_availability WHERE govt_id = ? AND day_of_week = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setString(2, dayOfWeek);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Time sqlStart = rs.getTime("start_time");
                if (sqlStart != null) {
                    return sqlStart.toLocalTime();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching availability: " + e.getMessage());
        }
        // Default to 9:00 AM if no availability
        return LocalTime.of(9, 0);
    }

    // Setters for DoctorHomePageController
    public void setTotalBooked(String value) {
        totalBookedLabel.setText("Total Booked: " + value);
    }

    public void setAttended(String value) {
        totalAttendedLabel.setText("Total Attended: " + value);
    }

    public void setRemaining(String value) {
        totalRemainingLabel.setText("Total Remaining: " + value);
    }

    private void loadNextAppointment() {
        Task<Optional<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected Optional<Map<String, Object>> call() {
                LocalDate today = LocalDate.now(PAKISTAN_ZONE); // Current: 2025-06-16
                LocalTime now = LocalTime.now(PAKISTAN_ZONE); // Current: ~07:12 AM
                LocalTime availabilityStartTime = getAvailabilityStartTime();
                if (now.isBefore(availabilityStartTime)) {
                    return Optional.empty();
                }
                List<Map<String, Object>> appointments = Appointments.getAppointmentsForDoctorToday(doctorId, today);
                return appointments.stream()
                        .filter(a -> "Upcoming".equals(a.get("status")))
                        .min((a1, a2) -> {
                            String num1 = String.valueOf(a1.get("appointment_number"));
                            String num2 = String.valueOf(a2.get("appointment_number"));
                            // Extract the incremental part (after hyphen) and compare
                            int seq1 = Integer.parseInt(num1.split("-")[1]);
                            int seq2 = Integer.parseInt(num2.split("-")[1]);
                            return Integer.compare(seq1, seq2);
                        });
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Optional<Map<String, Object>> appointmentOpt = loadTask.getValue();
                if (nextAppointmentPane == null) {
                    System.out.println("Error: nextAppointmentPane is null. Skipping update.");
                    return;
                }
                if (noAppointmentPane == null) {
                    System.out.println("Error: noAppointmentPane is null. Skipping update.");
                    return;
                }

                if (appointmentOpt.isPresent()) {
                    currentAppointment = appointmentOpt.get();
                    patientNameLabel.setText("Patient: " + currentAppointment.get("patient_name"));
                    appointmentNumberLabel.setText("Appointment #: " + currentAppointment.get("appointment_number"));
                    patientPhoneLabel.setText("Phone: " + currentAppointment.get("patient_phone"));
                    nextAppointmentPane.setVisible(true);
                    noAppointmentPane.setVisible(false);
                    animateNode(nextAppointmentPane);
                } else {
                    currentAppointment = null;
                    nextAppointmentPane.setVisible(false);
                    noAppointmentPane.setVisible(true);
                    animateNode(noAppointmentPane);
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showErrorAlert("Appointment Error", "Failed to load next appointment: " + loadTask.getException().getMessage());
                if (nextAppointmentPane != null) nextAppointmentPane.setVisible(false);
                if (noAppointmentPane != null) noAppointmentPane.setVisible(true);
            });
        });

        new Thread(loadTask).start();
    }

    private void searchPatient() {
        String query = patientSearchBar.getText().trim();
        if (query.isEmpty()) {
            loadNextAppointment();
            return;
        }

        Task<Optional<Map<String, Object>>> searchTask = new Task<>() {
            @Override
            protected Optional<Map<String, Object>> call() {
                LocalDate today = LocalDate.now(PAKISTAN_ZONE);
                List<Map<String, Object>> appointments = Appointments.getAppointmentsForDoctorToday(doctorId, today);
                return appointments.stream()
                        .filter(a -> "Upcoming".equals(a.get("status")))
                        .filter(a -> {
                            String patientName = (String) a.get("patient_name");
                            String patientPhone = (String) a.get("patient_phone");
                            return (patientName != null && patientName.toLowerCase().contains(query.toLowerCase())) ||
                                    (patientPhone != null && patientPhone.contains(query));
                        })
                        .min((a1, a2) -> {
                            String num1 = String.valueOf(a1.get("appointment_number"));
                            String num2 = String.valueOf(a2.get("appointment_number"));
                            // Extract the incremental part (after hyphen) and compare
                            int seq1 = Integer.parseInt(num1.split("-")[1]);
                            int seq2 = Integer.parseInt(num2.split("-")[1]);
                            return Integer.compare(seq1, seq2);
                        });
            }
        };

        searchTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Optional<Map<String, Object>> appointmentOpt = searchTask.getValue();
                if (nextAppointmentPane == null) {
                    System.out.println("Error: nextAppointmentPane is null. Skipping update.");
                    return;
                }
                if (noAppointmentPane == null) {
                    System.out.println("Error: noAppointmentPane is null. Skipping update.");
                    return;
                }

                if (appointmentOpt.isPresent()) {
                    currentAppointment = appointmentOpt.get();
                    patientNameLabel.setText("Patient: " + currentAppointment.get("patient_name"));
                    appointmentNumberLabel.setText("Appointment #: " + currentAppointment.get("appointment_number"));
                    patientPhoneLabel.setText("Phone: " + currentAppointment.get("patient_phone"));
                    nextAppointmentPane.setVisible(true);
                    noAppointmentPane.setVisible(false);
                    animateNode(nextAppointmentPane);
                } else {
                    currentAppointment = null;
                    nextAppointmentPane.setVisible(false);
                    noAppointmentPane.setVisible(true);
                    animateNode(noAppointmentPane);
                }
            });
        });

        searchTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showErrorAlert("Search Error", "Failed to search patient: " + searchTask.getException().getMessage());
                if (nextAppointmentPane != null) nextAppointmentPane.setVisible(false);
                if (noAppointmentPane != null) noAppointmentPane.setVisible(true);
            });
        });

        new Thread(searchTask).start();
    }

    @FXML
    protected void onAttendButtonClicked() {
        if (currentAppointment == null) {
            showErrorAlert("Action Error", "No appointment selected.");
            return;
        }

        // Create a confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Attendance");
        dialog.setHeaderText("Mark appointment as attended?");
        dialog.setContentText("Patient: " + currentAppointment.get("patient_name") + "\nAppointment #: " + currentAppointment.get("appointment_number"));

        // Define buttons
        ButtonType addPrescriptionButton = new ButtonType("Add Prescription", ButtonBar.ButtonData.LEFT);
        ButtonType okayButton = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addPrescriptionButton, okayButton, ButtonType.CANCEL);

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == okayButton) {
                // Mark appointment as attended
                Task<Void> attendTask = new Task<>() {
                    @Override
                    protected Void call() {
                        Appointments.markAsAttended(doctorId, (String) currentAppointment.get("appointment_number"));
                        return null;
                    }
                };

                attendTask.setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        patientSearchBar.clear();
                        currentAppointment = null;
                        loadNextAppointment();
                    });
                });

                attendTask.setOnFailed(event -> {
                    Platform.runLater(() -> {
                        showErrorAlert("Update Error", "Failed to mark appointment as attended: " + attendTask.getException().getMessage());
                    });
                });

                new Thread(attendTask).start();
            } else if (response == addPrescriptionButton) {
                openPrescriptionPopup(currentAppointment);
            }
        });
    }

    // Updated openPrescriptionPopup method
    // Updated openPrescriptionPopup method in DoctorDashboardController
    private void openPrescriptionPopup(Map<String, Object> appointmentData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorPrescription.fxml"));
            Parent root = loader.load();

            DoctorPrescriptionController controller = loader.getController();
            // Use patient_phone instead of patient_id
            String patientPhone = String.valueOf(appointmentData.getOrDefault("patient_phone", ""));
            String patientName = String.valueOf(appointmentData.getOrDefault("patient_name", "Unknown"));
            String patientAge = String.valueOf(appointmentData.getOrDefault("patient_age", ""));
            String patientGender = String.valueOf(appointmentData.getOrDefault("patient_gender", ""));
            System.out.println("Opening prescription for patient_phone: " + patientPhone); // Debug log
            controller.setPatientInfo(patientName, patientAge, patientGender);
            controller.setPatientId(patientPhone); // Set the correct patient_phone
            controller.setDoctorId(doctorId);
            controller.setCurrentAppointment(appointmentData); // Pass the current appointment

            // IMPORTANT: Pass the dashboard controller reference
            controller.setDashboardController(this);

            Stage prescriptionStage = new Stage();
            prescriptionStage.setTitle("Add Prescription - " + patientName);
            prescriptionStage.initModality(Modality.APPLICATION_MODAL);
            prescriptionStage.setScene(new Scene(root));
            prescriptionStage.setResizable(false);
            prescriptionStage.initStyle(StageStyle.DECORATED);

            // Add listener for stage close with debug logging
            prescriptionStage.setOnHidden(e -> {
                System.out.println("Prescription window closed. Controller currentAppointment: " +
                        controller.getCurrentAppointment() + ", Dashboard currentAppointment: " + currentAppointment);
                if (controller.getCurrentAppointment() == null && currentAppointment != null) {
                    currentAppointment = null; // Sync with prescription controller
                    System.out.println("Triggering refreshDashboard...");
                    loadNextAppointment(); // Reload next appointment
                    refreshDashboard(); // Ensure dashboard refreshes
                }
            });

            prescriptionStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Prescription Error", "Failed to open prescription view: " + e.getMessage());
        }
    }

    @FXML
    protected void onViewHistoryButtonClicked() {
        if (currentAppointment == null) {
            showErrorAlert("Action Error", "No appointment selected.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/PatientHistory.fxml"));
            Parent historyView = loader.load();
            PatientHistoryController controller = loader.getController();
            controller.setPatientPhone((String) currentAppointment.get("patient_phone"));

            Scene scene = dashboardPane.getScene();
            scene.setRoot(historyView);

            FadeTransition fade = new FadeTransition(Duration.millis(300), historyView);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load patient history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animateNode(VBox node) {
        if (node != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), node);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}