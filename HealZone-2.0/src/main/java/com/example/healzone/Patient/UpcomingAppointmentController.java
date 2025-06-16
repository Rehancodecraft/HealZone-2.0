package com.example.healzone.Patient;

import com.example.healzone.DatabaseConnection.Appointments;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UpcomingAppointmentController {
    @FXML
    private Label appointmentNo;
    @FXML
    private Label doctorName;
    @FXML
    private Label speciality;
    @FXML
    private Label hospitalName;
    @FXML
    private Label hospitalAddress;
    @FXML
    private Label patientName;
    @FXML
    private Label patientPhone;
    @FXML
    private Label patientDescription;
    @FXML
    private Label startTime;
    @FXML
    private Label endTime;
    @FXML
    private Label fee;
    @FXML
    private Label appointmentDate;


    public void initializeReceipt(UpcomingAppointmentModel appt) {
        if (appt == null) {
            appointmentNo.setText("Error Loading Appointment");
            return;
        }
        System.out.println("data has been set for upcoming appointment");
        System.out.println(appt.getDoctorName());
        System.out.println(appt.getAppointmentDate());
        System.out.println(appt.getPatientName());
        System.out.println(appt.getStartTime());

        appointmentNo.setText("Appointment No #" + String.format(appt.getAppointmentNumber()));
        doctorName.setText(appt.getDoctorName());
        speciality.setText(appt.getSpeciality());
        hospitalName.setText(appt.getHospitalName());
        hospitalAddress.setText(appt.getHospitalAddress());
        patientName.setText(appt.getPatientName());
        patientPhone.setText(appt.getPatientPhone());
        patientDescription.setText(appt.getPatientDescription());
        appointmentDate.setText(appt.getAppointmentDate().toString());

        // Format time slot in 12-hour AM/PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        startTime.setText(appt.getStartTime().format(formatter));
        endTime.setText(appt.getEndTime().format(formatter));
        fee.setText("Fee: Rs " + appt.getConsultationFee());
    }


}