package com.example.healzone.Patient;

import com.example.healzone.DatabaseConnection.Patients;
import com.example.healzone.Doctor.Doctor;
import com.example.healzone.Doctor.TimeSlot;
import com.example.healzone.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;

public class LoginPatient {


    public static boolean getCurrentPatient(String email, String password) {
        try {
            Map<String, String> patientData = Patients.getCurrentPatientDetails(email, password);
            if (!patientData.isEmpty()) {
                Patient.setName(patientData.get("name"));
                Patient.setFatherName(patientData.get("father_name"));
                Patient.setGender(patientData.get("gender"));
                Patient.setAge(patientData.get("age"));
                Patient.setPhone(patientData.get("phone_number"));
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
