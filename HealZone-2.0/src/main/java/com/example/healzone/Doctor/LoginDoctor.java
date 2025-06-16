package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Doctors;
import com.example.healzone.Main;
import com.example.healzone.Patient.Patient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
import static com.example.healzone.DatabaseConnection.Doctors.getCurrentDoctorDetails;
import static com.example.healzone.DatabaseConnection.Patients.getCurrentPatientDetails;

public class LoginDoctor {
    public static boolean getCurrentDoctor(String email, String password) {
        Map<String, Object> doctorData = Doctors.getCurrentDoctorDetails(email, password);
        if (!doctorData.isEmpty()) {
            Doctor.setGovtID((String) doctorData.get("govt_id"));
            Doctor.setFirstName((String) doctorData.get("first_name"));
            Doctor.setLastName((String) doctorData.get("last_name"));
            Doctor.setEmail((String) doctorData.get("email"));
            Doctor.setPhone((String) doctorData.get("phone_number"));
            Doctor.setSpecialization((String) doctorData.get("specialization"));
            Doctor.setDegrees((String) doctorData.get("degrees"));
            Doctor.setMedicalLicenseNumber((String) doctorData.get("medical_license_number"));
            Doctor.setBio((String) doctorData.get("bio"));
            Doctor.setHospitalName((String) doctorData.get("hospital_name"));
            Doctor.setHospitalAddress((String) doctorData.get("hospital_address"));
            Doctor.setConsultationFee((String) doctorData.get("consultation_fee"));
            Doctor.setExperience((String) doctorData.get("experience")); // Add experience

            // Clear existing availability
            Doctor.getAvailability().clear();
            // Add new availability
            Map<String, TimeSlot> availability = (Map<String, TimeSlot>) doctorData.get("availability");
            availability.forEach((day, slot) -> Doctor.addAvailability(day, slot));

            return true;
        }
        return false;
    }
    public static void getDoctorAvailability() {
        try {
            PreparedStatement stmt2 = connection.prepareStatement(
                    "SELECT * FROM doctor_availability WHERE govt_id = ?"
            );
            stmt2.setString(1, Doctor.getGovtID());
            ResultSet rs2 = stmt2.executeQuery();

// Optional: clear previous availability before loading new
//            Doctor.getAvailability().clear();
//
//            Doctor.getAvailability().clear();
//            // Add new availability
//            Map<String, TimeSlot> availability = (Map<String, TimeSlot>) doctors.get("availability");
//            availability.forEach((day, slot) -> Doctor.addAvailability(day, slot));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}