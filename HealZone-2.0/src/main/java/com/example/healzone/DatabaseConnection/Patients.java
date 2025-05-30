package com.example.healzone.DatabaseConnection;

import com.example.healzone.Patient.Patient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Patients {
    public static void createTableForPatients(){
        try{
            String patients = """
                            CREATE TABLE IF NOT EXISTS patients (
                                name VARCHAR(100) ,
                                father_name VARCHAR(100),
                                phone_number VARCHAR(15) PRIMARY KEY,
                                email VARCHAR(100) UNIQUE NOT NULL,
                                age VARCHAR(3),
                                gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
                                password TEXT
                            );
                        """;
            PreparedStatement preparedStatement = connection.prepareStatement(patients);
            preparedStatement.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void registerPatient(String name, String fatherName, String phoneNumber,String email, String age, String gender, String password){
        try {
            String register = "INSERT INTO patients (name, father_name, phone_number,email, age, gender, password)\n" +
                    "VALUES (?, ?, ?, ?,?, ?,?);\n";
            PreparedStatement preparedStatement = connection.prepareStatement(register);
            preparedStatement.setString(1,name);
            preparedStatement.setString(2,fatherName);
            preparedStatement.setString(3,phoneNumber);
            preparedStatement.setString(4,email);
            preparedStatement.setString(5,age);
            preparedStatement.setString(6,gender);
            preparedStatement.setString(7,password);
            preparedStatement.execute();
//            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static boolean checkPhoneNumber(String phoneNumber){
        try{
            String sql = "select phone_number from patients where phone_number = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,phoneNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkEmail(String email,String phone){
        try{
            String sql = "select email from patients where email = ? AND phone_number = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
            stmt.setString(2,phone);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkEmail(String email){
        try{
            String sql = "select email from patients where email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static ResultSet getCurrentPatientDetails(String email, String password){
        try{
            String sql = "SELECT * FROM patients WHERE email = ? AND password = ?;";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
            stmt.setString(2,password);
            return stmt.executeQuery();
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
    public static void resetPassword(String password,String email){
        try{
            String sql= "UPDATE patients SET password = ? WHERE email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,password);
            stmt.setString(2,email);
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void getNameOfPatient(String email){
        try{
            String sql = "SELECT name FROM patients WHERE email = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
            ResultSet result = stmt.executeQuery();
            if(result.next()){
                Patient.setName(result.getString("name"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public static ResultSet getUpcomingAppointmentsForPatient(String phoneNumber) {
        String query = """
        SELECT * FROM appointments
        WHERE patient_phone = ? AND status = 'Upcoming'
        ORDER BY appointment_date ASC;
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            return ps.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static ResultSet getCompletedAppointmentsForPatient(String phoneNumber) {
        String query = """
        SELECT * FROM appointments
        WHERE patient_phone = ? AND status = 'Completed'
        ORDER BY appointment_date DESC;
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


}
