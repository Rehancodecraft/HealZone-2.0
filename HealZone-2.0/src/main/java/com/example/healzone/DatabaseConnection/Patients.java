package com.example.healzone.DatabaseConnection;
import com.example.healzone.Patient.Patient;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Patients {
    public static void createTableForPatients() {
        String sql = """
            CREATE TABLE IF NOT EXISTS patients (
                name VARCHAR(100),
                father_name VARCHAR(100),
                phone_number VARCHAR(15) PRIMARY KEY NOT NULL,
                email VARCHAR(100) NOT NULL,
                age VARCHAR(3),
                gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
                password TEXT
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Patients table created");
        } catch (SQLException e) {
            System.err.println("Error creating patients table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void registerPatient(String name, String fatherName, String phoneNumber, String email, String age, String gender, String password) {
        String insert = """
            INSERT INTO patients (name, father_name, phone_number, email, age, gender, password)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, name);
            ps.setString(2, fatherName);
            ps.setString(3, phoneNumber);
            ps.setString(4, email);
            ps.setString(5, age);
            ps.setString(6, gender);
            ps.setString(7, password);
            ps.executeUpdate();
            System.out.println("Patient registered");
        } catch (SQLException e) {
            System.err.println("Error registering patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean checkPhoneNumber(String phoneNumber) {
        String sql = "SELECT phone_number FROM patients WHERE phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking phone number: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkEmail(String email, String phone) {
        String sql = "SELECT email FROM patients WHERE email = ? AND phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking email and phone: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkEmail(String email) {
        String sql = "SELECT email FROM patients WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, String> getCurrentPatientDetails(String email, String password) {
        String sql = """
            SELECT * FROM patients WHERE email = ? AND password = ?
            """;
        Map<String, String> patientData = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                patientData.put("name", rs.getString("name"));
                patientData.put("father_name", rs.getString("father_name"));
                patientData.put("phone_number", rs.getString("phone_number"));
                patientData.put("email", rs.getString("email"));
                patientData.put("age", rs.getString("age"));
                patientData.put("gender", rs.getString("gender"));
                patientData.put("password", rs.getString("password"));
            }
            return patientData;
        } catch (SQLException e) {
            System.err.println("Error getting patient details: " + e.getMessage());
            e.printStackTrace();
            return patientData; // Return empty map on error
        }
    }

    public static void resetPassword(String password, String email) {
        String sql = "UPDATE patients SET password = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("Password reset successfully");
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void getNameOfPatient(String email) {
        String sql = "SELECT name FROM patients WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                Patient.setName(result.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient name: " + e.getMessage());
            e.printStackTrace();
        }
    }
}