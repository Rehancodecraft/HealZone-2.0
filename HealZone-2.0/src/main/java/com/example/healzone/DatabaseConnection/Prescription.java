package com.example.healzone.DatabaseConnection;

import java.sql.*;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Prescription {
    private String doctorId;
    private String patientId;
    private String patientName;
    private String date;
    private String diagnosis;
    private String precautions;
    private String followup;
    private String notes;
    private String medicines;

    // Getters and Setters
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getPrecautions() { return precautions; }
    public void setPrecautions(String precautions) { this.precautions = precautions; }
    public String getFollowup() { return followup; }
    public void setFollowup(String followup) { this.followup = followup; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getMedicines() { return medicines; }
    public void setMedicines(String medicines) { this.medicines = medicines; }

    // SQL Table Creation
    public static void createPrescriptionTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS prescriptions (
                id SERIAL PRIMARY KEY,
                doctor_id VARCHAR(50) NOT NULL,
                patient_id VARCHAR(50) NOT NULL,
                patient_name VARCHAR(100) NOT NULL,
                date DATE NOT NULL,
                diagnosis TEXT,
                precautions TEXT,
                followup VARCHAR(50),
                notes TEXT,
                medicines TEXT NOT NULL,
                FOREIGN KEY (doctor_id) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                FOREIGN KEY (patient_id) REFERENCES patients(phone_number) ON DELETE SET NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Prescriptions table created");
        } catch (SQLException e) {
            System.err.println("Error creating prescriptions table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save Prescription
    public void save() throws SQLException {
        String sql = "INSERT INTO prescriptions (doctor_id, patient_id, patient_name, date, diagnosis, precautions, followup, notes, medicines) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setString(2, patientId);
            pstmt.setString(3, patientName);
            pstmt.setString(4, date);
            pstmt.setString(5, diagnosis);
            pstmt.setString(6, precautions);
            pstmt.setString(7, followup);
            pstmt.setString(8, notes);
            pstmt.setString(9, medicines);
            pstmt.executeUpdate();
        }
    }

    // Static block to create table on class load
    static {
        createPrescriptionTable();
    }
}