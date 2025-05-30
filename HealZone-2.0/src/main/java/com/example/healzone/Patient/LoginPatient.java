package com.example.healzone.Patient;

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

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;

public class LoginPatient {


    public static boolean getCurrentPatient(String email, String password) {
        try {
            ResultSet rs = getCurrentPatientDetails(email, password);
            if(rs.next()) {
                Patient.setName(rs.getString("name"));
                Patient.setFatherName(rs.getString("father_name"));
                Patient.setGender(rs.getString("gender"));
                Patient.setAge(rs.getString("age"));
                Patient.setPhone(rs.getString("phone_number"));
                return true;
            }else{
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
