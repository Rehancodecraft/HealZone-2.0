package com.example.healzone.DatabaseConnection;
//
//import com.example.healzone.Patient.Patient;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
//
//public class Patients {
//    public static void createTableForPatients(){
//        try{
//            String patients = """
//                            CREATE TABLE IF NOT EXISTS patients (
//                                name VARCHAR(100) ,
//                                father_name VARCHAR(100),
//                                phone_number VARCHAR(15) PRIMARY KEY,
//                                email VARCHAR(100) UNIQUE NOT NULL,
//                                age VARCHAR(3),
//                                gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
//                                password TEXT
//                            );
//                        """;
//            PreparedStatement preparedStatement = connection.prepareStatement(patients);
//            preparedStatement.execute();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void registerPatient(String name, String fatherName, String phoneNumber,String email, String age, String gender, String password){
//        try {
//            System.out.println("registerPatient running");
//            String register = "INSERT INTO patients (name, father_name, phone_number,email, age, gender, password)\n" +
//                    "VALUES (?, ?, ?, ?,?, ?,?);\n";
//            PreparedStatement preparedStatement = connection.prepareStatement(register);
//            preparedStatement.setString(1,name);
//            preparedStatement.setString(2,fatherName);
//            preparedStatement.setString(3,phoneNumber);
//            preparedStatement.setString(4,email);
//            preparedStatement.setString(5,age);
//            preparedStatement.setString(6,gender);
//            preparedStatement.setString(7,password);
//            preparedStatement.execute();
////            connection.commit();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static boolean checkPhoneNumber(String phoneNumber){
//        try{
//            String sql = "select phone_number from patients where phone_number = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,phoneNumber);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static boolean checkEmail(String email,String phone){
//        try{
//            String sql = "select email from patients where email = ? AND phone_number = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,email);
//            stmt.setString(2,phone);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static boolean checkEmail(String email){
//        try{
//            String sql = "select email from patients where email = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,email);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static ResultSet getCurrentPatientDetails(String email, String password){
//        try{
//            String sql = "SELECT * FROM patients WHERE email = ? AND password = ?;";
//
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,email);
//            stmt.setString(2,password);
//            return stmt.executeQuery();
//        }catch (SQLException e){
//            e.printStackTrace();
//            return null;
//        }
//    }
//    public static void resetPassword(String password,String email){
//        try{
//            String sql= "UPDATE patients SET password = ? WHERE email = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,password);
//            stmt.setString(2,email);
//            stmt.execute();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void getNameOfPatient(String email){
//        try{
//            String sql = "SELECT name FROM patients WHERE email = ?";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,email);
//            ResultSet result = stmt.executeQuery();
//            if(result.next()){
//                Patient.setName(result.getString("name"));
//            }
//        }catch(SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static ResultSet getUpcomingAppointmentsForPatient(String phoneNumber) {
//        String query = """
//        SELECT * FROM appointments
//        WHERE patient_phone = ? AND status = 'Upcoming'
//        ORDER BY appointment_date ASC;
//    """;
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//            ps.setString(1, phoneNumber);
//            return ps.executeQuery();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    public static ResultSet getCompletedAppointmentsForPatient(String phoneNumber) {
//        String query = """
//        SELECT * FROM appointments
//        WHERE patient_phone = ? AND status = 'Completed'
//        ORDER BY appointment_date DESC;
//    """;
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//            ps.setString(1, phoneNumber);
//            return ps.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//}


import com.example.healzone.Patient.Patient;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;


public class Patients {
    public static void createTableForPatients() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("name", "VARCHAR(100)", false, false, null, null),
                new DatabaseUtils.Column("father_name", "VARCHAR(100)", false, false, null, null),
                new DatabaseUtils.Column("phone_number", "VARCHAR(15)", true, true, null, null),
                new DatabaseUtils.Column("email", "VARCHAR(100)", true, false, null, null),
                new DatabaseUtils.Column("age", "VARCHAR(3)", false, false, null, null),
                new DatabaseUtils.Column("gender", "VARCHAR(10)", false, false, null, DatabaseConnection.isSQLite() ? null : "CHECK (gender IN ('Male', 'Female', 'Other'))"),
                new DatabaseUtils.Column("password", "TEXT", false, false, null, null)
        );
        DatabaseUtils.createTable("patients", columns, null, null, null);
        if (DatabaseConnection.isSQLite()) {
            try {
                DatabaseUtils.validateCheckConstraint("patients", "gender", Arrays.asList("Male", "Female", "Other"));
            } catch (SQLException e) {
                System.err.println("Error validating gender constraint: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void registerPatient(String name, String fatherName, String phoneNumber, String email, String age, String gender, String password) {
        if (DatabaseConnection.isSQLite() && gender != null && !Arrays.asList("Male", "Female", "Other").contains(gender)) {
            throw new IllegalArgumentException("Invalid gender: " + gender);
        }
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

    public static ResultSet getUpcomingAppointmentsForPatient(String phoneNumber) {
        String query = """
            SELECT * FROM appointments
            WHERE patient_phone = ? AND status = 'Upcoming'
            ORDER BY appointment_date ASC
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting upcoming appointments: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getCompletedAppointmentsForPatient(String phoneNumber) {
        String query = """
            SELECT * FROM appointments
            WHERE patient_phone = ? AND status = 'Completed'
            ORDER BY appointment_date DESC
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}