package com.example.healzone.DatabaseConnection;

import java.sql.*;
import java.time.LocalTime;

import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.DatabaseConnection.Patients.*;

public class DatabaseConnection {
    public static Connection connection;
    public static void connectToDatabase() {
        try {

            String url = "jdbc:postgresql://localhost:5432/HealZone"; // replace with your DB name
            String user = "postgres";
            String password = "Rehan*17112006";

            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(true);
            initializeDatabase();
            System.out.println("Database connected and initialized");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error in database Connection");
            e.printStackTrace();
        }
    }
    public static void initializeDatabase(){
            createTableForPatients();
            createTableForPersonalDetailsOfDoctors();
            createTableForProfessionalDetailsOfDoctor();
            createTableForPracticeInformationOfDoctor();
            createTableForAvailabilityOfDoctor();
            createTableForSecurityOfDoctor();
            createTableForAppointments();
    }
    public static void createTableForAppointments() {
        try {
            String sql = """
                    CREATE TABLE IF NOT EXISTS appointments (
                        doctor_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                        appointment_number INTEGER NOT NULL,
                        patient_phone VARCHAR(15) REFERENCES patients(phone_number) ON DELETE CASCADE,
                        appointment_date DATE NOT NULL,
                        status VARCHAR(20) CHECK (status IN ('Upcoming', 'Completed', 'Cancelled')) DEFAULT 'Upcoming',
                        created_at DATE DEFAULT CURRENT_DATE,
                        PRIMARY KEY (doctor_id, appointment_number)
                    );
                    """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }
    public static int getAppointmentNo(String doctorGovtId){
        try {
            String countQuery = "SELECT COALESCE(MAX(appointment_number), 0) FROM appointments WHERE doctor_id = ?";
            PreparedStatement countStmt = connection.prepareStatement(countQuery);
            countStmt.setString(1, doctorGovtId);
            ResultSet rs = countStmt.executeQuery();
            int nextNumber = 1;
            if (rs.next()) {
                nextNumber = rs.getInt(1) + 1;
                return nextNumber;
            }else{
                return 0;
            }

        }catch (SQLException e){
            e.printStackTrace();
            return 0;
        }
    }
    public static void insertNewAppointment(String doctorGovtId,int appointmentNumber,String patientPhone){
        try{
            String insertQuery = """
            INSERT INTO appointments (doctor_id, appointment_number, patient_phone)
            VALUES (?, ?, ?)
            """;
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setString(1, doctorGovtId);
            insertStmt.setInt(2, appointmentNumber);
            insertStmt.setString(3, patientPhone);
            insertStmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

}
