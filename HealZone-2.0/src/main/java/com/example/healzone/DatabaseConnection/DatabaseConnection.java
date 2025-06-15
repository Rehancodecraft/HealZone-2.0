package com.example.healzone.DatabaseConnection;

import java.io.File;
import java.sql.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.healzone.DatabaseConnection.Appointments.createTableForAppointments;
import static com.example.healzone.DatabaseConnection.Doctors.*;
import static com.example.healzone.DatabaseConnection.Patients.*;
import static com.example.healzone.DatabaseConnection.Prescription.createPrescriptionTable;

public class DatabaseConnection {
    public static Connection connection;
    public static boolean isSQLite = false;

    public static void connectToDatabase() {
        try {
            String pgUrl = "jdbc:postgresql://localhost:5432/HealZone2";
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
        createPrescriptionTable();
    }
    public static boolean isSQLite() {
        return isSQLite;
    }
}
