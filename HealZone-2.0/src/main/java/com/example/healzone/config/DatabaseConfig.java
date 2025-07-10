package com.example.healzone.config;

import java.sql.*;

import static com.example.healzone.repository.AppointmentRepository.createTableForAppointments;
import static com.example.healzone.repository.DoctorRepository.*;
import static com.example.healzone.repository.PatientRepository.*;
import static com.example.healzone.repository.PrescriptionRepository.createPrescriptionTable;

public class DatabaseConfig {
    public static Connection connection;

    public static void connectToDatabase() {
        try {
            String pgUrl = "jdbc:postgresql://localhost:5432/HealZone";
            String pgUser = "postgres";
            String pgPassword = "iamrehan";
            connection = DriverManager.getConnection(pgUrl, pgUser, pgPassword);
            connection.setAutoCommit(true);
            System.out.println("Connected to HealZone database");
            System.out.println("Database initializing.........");
            initializeDatabase();

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
        createIndexes();
    }
}
