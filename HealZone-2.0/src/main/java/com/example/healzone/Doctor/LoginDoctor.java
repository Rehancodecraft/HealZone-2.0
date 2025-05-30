package com.example.healzone.Doctor;

import com.example.healzone.Main;
import com.example.healzone.Patient.Patient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
import static com.example.healzone.DatabaseConnection.Doctors.getCurrentDoctorDetails;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;

public class LoginDoctor {
    public static boolean getCurrentDoctor(String email, String password) {
        try {
            ResultSet rs = getCurrentDoctorDetails(email, password);
            if (rs.next()) {
                Doctor.setGovtID(rs.getString("govt_id"));
                Doctor.setFirstName(rs.getString("first_name"));
                Doctor.setLastName(rs.getString("last_name"));
                Doctor.setEmail(rs.getString("email"));
                Doctor.setPhone(rs.getString("phone_number"));

                // From professional_details
                Doctor.setSpecialization(rs.getString("specialization"));
                Doctor.setDegrees(rs.getString("degrees"));
                Doctor.setMedicalLicenseNumber(rs.getString("medical_license_number"));
                Doctor.setBio(rs.getString("bio"));

                // From practice_information
                Doctor.setHospitalName(rs.getString("hospital_name"));
                Doctor.setHospitalAddress(rs.getString("hospital_address"));
                Doctor.setConsultationFee(rs.getString("consultation_fee"));
                getDoctorAvailability();
                return true;
            }else{
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void getDoctorAvailability() {
        try {
            PreparedStatement stmt2 = connection.prepareStatement(
                    "SELECT * FROM doctor_availability WHERE govt_id = ?"
            );
            stmt2.setString(1, Doctor.getGovtID());
            ResultSet rs2 = stmt2.executeQuery();

// Optional: clear previous availability before loading new
            Doctor.getAvailability().clear();

            while (rs2.next()) {
                String day = rs2.getString("day_of_week");
                LocalTime start = rs2.getTime("start_time").toLocalTime();
                LocalTime end = rs2.getTime("end_time").toLocalTime();

                TimeSlot slot = new TimeSlot(start, end);
                Doctor.addAvailability(day, slot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}