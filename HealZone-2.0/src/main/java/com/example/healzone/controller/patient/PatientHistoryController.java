package com.example.healzone.controller.patient;

import com.example.healzone.repository.AppointmentRepository;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientHistoryController {
    @FXML
    private VBox historyPane;
    @FXML
    private ListView<String> historyList;

    private String patientPhone;

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
        loadHistory();
    }

    private void loadHistory() {
        try (ResultSet rs = AppointmentRepository.getCompletedAppointmentsForPatient(patientPhone)) {
            if (rs != null) {
                while (rs.next()) {
                    String entry = String.format("Appointment #%d with Dr. %s on %s",
                            rs.getInt("appointment_number"),
                            rs.getString("doctor_name"),
                            rs.getDate("appointment_date"));
                    historyList.getItems().add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading patient history: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
