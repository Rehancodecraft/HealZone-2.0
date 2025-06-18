package com.example.healzone.repository;

import com.example.healzone.model.PatientModel;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static com.example.healzone.config.DatabaseConfig.connection;

public class PatientRepository {
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

                // Update Patient static fields
                PatientModel.setName(rs.getString("name"));
                PatientModel.setFatherName(rs.getString("father_name"));
                PatientModel.setPhone(rs.getString("phone_number"));
                PatientModel.setEmail(rs.getString("email"));
                PatientModel.setAge(rs.getString("age"));
                PatientModel.setGender(rs.getString("gender"));
                PatientModel.setPassword(rs.getString("password")); // Store for verification
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
            PatientModel.setPassword(password); // Update static field
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean verifyPassword(String phoneNumber, String password) {
        String sql = "SELECT password FROM patients WHERE phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword != null && storedPassword.equals(password);
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePatientDetails(String phoneNumber, String name, String fatherName, String email, String age, String gender) {
        String update = """
            UPDATE patients
            SET name = ?, father_name = ?, email = ?, age = ?, gender = ?
            WHERE phone_number = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setString(2, fatherName);
            ps.setString(3, email);
            ps.setString(4, age);
            ps.setString(5, gender);
            ps.setString(6, phoneNumber);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Patient details updated for phoneNumber: " + phoneNumber);
                // Update static fields in Patient class
                PatientModel.setName(name);
                PatientModel.setFatherName(fatherName);
                PatientModel.setEmail(email);
                PatientModel.setAge(age);
                PatientModel.setGender(gender);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating patient details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void getNameOfPatient(String email) {
        String sql = "SELECT name FROM patients WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                PatientModel.setName(result.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient name: " + e.getMessage());
            e.printStackTrace();
        }
    }
}