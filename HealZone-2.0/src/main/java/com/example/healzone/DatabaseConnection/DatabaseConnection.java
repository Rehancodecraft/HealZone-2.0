package com.example.healzone.DatabaseConnection;

import java.io.File;
import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.DatabaseConnection.Patients.*;

public class DatabaseConnection {
    public static Connection connection;
    public static boolean isSQLite = false;

    public static void connectToDatabase() {
        try {
            String pgUrl = "jdbc:postgresql://localhost:5432/HealZone";
            String pgUser = "postgres";
            String pgPassword = "Rehan*17112006";
            connection = DriverManager.getConnection(pgUrl, pgUser, pgPassword);
            connection.setAutoCommit(true);
            isSQLite = false;
            System.out.println("Connected to PostgreSQL database");
            initializeDatabase();
            System.out.println("Database initialized");
        } catch (SQLException e) {
            System.out.println("PostgreSQL connection failed: " + e.getMessage());
            System.out.println("Falling back to SQLite...");
            try {
                // Use SQLite in ~/.healzone/healzone.db (mapped via Docker volume)
                String dbPath = System.getenv().getOrDefault("SQLITE_DB_PATH",
                        System.getProperty("user.home") + File.separator + ".healzone" + File.separator + "healzone.db");
                new File(dbPath).getParentFile().mkdirs(); // Create .healzone directory if needed
                String sqliteUrl = "jdbc:sqlite:" + dbPath;
                connection = DriverManager.getConnection(sqliteUrl);
                connection.setAutoCommit(true);
                isSQLite = true;
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.close();
                System.out.println("Connected to SQLite database at: " + dbPath);
                initializeDatabase();
                System.out.println("Database initialized");
            } catch (SQLException ex) {
                System.err.println("SQLite connection failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void initializeDatabase() {
        createTableForPatients();
        createTableForPersonalDetailsOfDoctors();
        createTableForProfessionalDetailsOfDoctor();
        createTableForPracticeInformationOfDoctor();
        createTableForAvailabilityOfDoctor();
        createTableForSecurityOfDoctor();
        createTableForAppointments();
        createTableForDoctorReviews();
    }

    public static void createTableForAppointments() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("doctor_id", "VARCHAR(50)", true, false, "doctors(govt_id) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("appointment_number", "INTEGER", true, false, null, null),
                new DatabaseUtils.Column("patient_phone", "VARCHAR(15)", true, false, "patients(phone_number) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("appointment_date", "DATE", true, false, null, null),
                new DatabaseUtils.Column("status", "VARCHAR(20)", false, false, null, null)
        );
        DatabaseUtils.createTable("appointments", columns, "created_at", null, List.of("doctor_id", "appointment_number"));
    }

    public static int getAppointmentNo(String doctorGovtId) {
        String countQuery = "SELECT COALESCE(MAX(appointment_number), 0) FROM appointments WHERE doctor_id = ?";
        try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
            countStmt.setString(1, doctorGovtId);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1;
        } catch (SQLException e) {
            System.err.println("Error getting appointment number: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static void insertNewAppointment(String doctorGovtId, int appointmentNumber, String patientPhone, String status) {
        if (isSQLite && status != null && !Arrays.asList("Upcoming", "Completed", "Cancelled").contains(status)) {
            throw new IllegalArgumentException("Invalid status: must be 'Upcoming', 'Completed', or 'Cancelled'");
        }
        String insertQuery = """
            INSERT INTO appointments (doctor_id, appointment_number, patient_phone, status)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, doctorGovtId);
            insertStmt.setInt(2, appointmentNumber);
            insertStmt.setString(3, patientPhone);
            insertStmt.setString(4, status != null ? status : "Upcoming");
            insertStmt.executeUpdate();
            System.out.println("Appointment inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static boolean isSQLite() {
        return isSQLite;
    }
}
