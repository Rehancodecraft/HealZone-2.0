
package com.example.healzone.Doctor;

import com.example.healzone.Appointment;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    private Appointment currentAppointment;
    private String doctorId;
    private LocalTime availabilityStartTime;

    @FXML
    protected void initialize() {
        doctorId = Doctor.getGovtID();
        availabilityStartTime = Doctor.; // Assumed method
        loadDashboardData();

        // Setup search functionality
        searchButton.setOnAction(event -> searchPatient());
        patientSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadNextAppointment();
            }
        });
    }

    private void loadDashboardData() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();
                List<Appointment> appointments = Appointments.getAppointmentsForDoctorToday(doctorId, today);

                int totalBooked = appointments.size();
                int totalAttended = 0;
                int totalRemaining = totalBooked;

                boolean isBeforeAvailability = now.isBefore(availabilityStartTime);

                if (!isBeforeAvailability) {
                    totalAttended = (int) appointments.stream().filter(Appointment::isAttended).count();
                    totalRemaining = totalBooked - totalAttended;
                }

                int finalTotalBooked = totalBooked;
                int finalTotalAttended = totalAttended;
                int finalTotalRemaining = totalRemaining;

                Platform.runLater(() -> {
                    totalBookedLabel.setText("Total Booked: " + finalTotalBooked);
                    totalAttendedLabel.setText("Total Attended: " + finalTotalAttended);
                    totalRemainingLabel.setText("Total Remaining: " + finalTotalRemaining);
                    loadNextAppointment();
                });

                return null;
            }
        };

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Error loading dashboard data: " + loadTask.getException().getMessage());
            });
        });

        new Thread(loadTask).start();
    }

    private void loadNextAppointment() {
        Task<Optional<Appointment>> loadTask = new Task<>() {
            @Override
            protected Optional<Appointment> call() {
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();
                if (now.isBefore(availabilityStartTime)) {
                    return Optional.empty();
                }
                List<Appointment> appointments = Appointments.getAppointmentsForDoctorToday(doctorId, today);
                return appointments.stream()
                        .filter(a -> !a.isAttended())
                        .sorted((a1, a2) -> Integer.compare(a1.getAppointmentNumber(), a2.getAppointmentNumber()))
                        .findFirst();
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Optional<Appointment> appointmentOpt = loadTask.getValue();
                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    currentAppointment = appointment;
                    patientNameLabel.setText("Patient: " + Patients.getPatientByPhone(appointment.getPatientPhone()).getName());
                    appointmentNumberLabel.setText("Appointment #: " + appointment.getAppointmentNumber());
                    patientPhoneLabel.setText("Phone: " + appointment.getPatientPhone());
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
                System.err.println("Error loading next appointment: " + loadTask.getException().getMessage());
                nextAppointmentPane.setVisible(false);
                noAppointmentPane.setVisible(true);
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

        Task<Optional<Appointment>> searchTask = new Task<>() {
            @Override
            protected Optional<Appointment> call() {
                LocalDate today = LocalDate.now();
                List<Appointment> appointments = Appointments.getAppointmentsForDoctorToday(doctorId, today);
                return appointments.stream()
                        .filter(a -> !a.isAttended())
                        .filter(a -> {
                            Patient patient = Patients.getPatientByPhone(a.getPatientPhone());
                            return patient.getName().toLowerCase().contains(query.toLowerCase()) ||
                                    a.getPatientPhone().contains(query);
                        })
                        .findFirst();
            }
        };

        searchTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Optional<Appointment> appointmentOpt = searchTask.getValue();
                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    currentAppointment = appointment;
                    patientNameLabel.setText("Patient: " + Patients.getPatientByPhone(appointment.getPatientPhone()).getName());
                    appointmentNumberLabel.setText("Appointment #: " + appointment.getAppointmentNumber());
                    patientPhoneLabel.setText("Phone: " + appointment.getPatientPhone());
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
                System.err.println("Error searching patient: " + searchTask.getException().getMessage());
                nextAppointmentPane.setVisible(false);
                noAppointmentPane.setVisible(true);
            });
        });

        new Thread(searchTask).start();
    }

    @FXML
    protected void onAttendButtonClicked() {
        if (currentAppointment == null) return;

        Task<Void> attendTask = new Task<>() {
            @Override
            protected Void call() {
                Appointments.markAsAttended(currentAppointment.getId());
                return null;
            }
        };

        attendTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                loadDashboardData();
                patientSearchBar.clear();
                currentAppointment = null;
            });
        });

        attendTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Error marking appointment as attended: " + attendTask.getException().getMessage());
            });
        });

        new Thread(attendTask).start();
    }

    @FXML
    protected void onViewHistoryButtonClicked() {
        if (currentAppointment == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/PatientHistory.fxml"));
            Parent historyView = loader.load();
            PatientHistoryController controller = loader.getController();
            controller.setPatientPhone(currentAppointment.getPatientPhone());

            Scene scene = dashboardPane.getScene();
            scene.setRoot(historyView);

            FadeTransition fade = new FadeTransition(Duration.millis(300), historyView);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        } catch (IOException e) {
            System.err.println("Error loading patient history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animateNode(VBox node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
}
