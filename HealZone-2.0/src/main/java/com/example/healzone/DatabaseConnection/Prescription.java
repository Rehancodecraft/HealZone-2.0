package com.example.healzone.DatabaseConnection;

import com.example.healzone.Doctor.ExtendedPrescriptionData;
import com.example.healzone.MedicationData;
import com.example.healzone.PrescriptionData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    private String appointmentNumber; // Added field for appointment_number

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
    public String getAppointmentNumber() { return appointmentNumber; } // Added getter
    public void setAppointmentNumber(String appointmentNumber) { this.appointmentNumber = appointmentNumber; } // Added setter

    // SQL Table Creation
    public static void createPrescriptionTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS prescriptions (
                id SERIAL PRIMARY KEY,
                doctor_id VARCHAR(50) NOT NULL,
                patient_id VARCHAR(50) NOT NULL,
                patient_name VARCHAR(100) NOT NULL,
                appointment_number VARCHAR(8) NOT NULL, -- Added column
                date DATE NOT NULL,
                diagnosis TEXT,
                precautions TEXT,
                followup VARCHAR(50),
                notes TEXT,
                medicines TEXT NOT NULL,
                FOREIGN KEY (doctor_id) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                FOREIGN KEY (patient_id) REFERENCES patients(phone_number) ON DELETE SET NULL,
                FOREIGN KEY (appointment_number) REFERENCES appointments(appointment_number) ON DELETE CASCADE -- Added foreign key
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
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection is null or closed");
        }

        // Validate required fields
        if (doctorId == null || patientId == null || appointmentNumber == null || patientName == null || date == null || medicines == null) {
            throw new SQLException("Missing required fields: doctorId=" + doctorId + ", patientId=" + patientId +
                    ", appointmentNumber=" + appointmentNumber + ", patientName=" + patientName +
                    ", date=" + date + ", medicines=" + medicines);
        }

        String sql = "INSERT INTO prescriptions (doctor_id, patient_id, patient_name, appointment_number, date, diagnosis, precautions, followup, notes, medicines) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set parameters with null checks
            pstmt.setString(1, doctorId);
            pstmt.setString(2, patientId);
            pstmt.setString(3, patientName);
            pstmt.setString(4, appointmentNumber);

            // Parse and set date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
            pstmt.setDate(5, sqlDate);

            pstmt.setString(6, diagnosis != null ? diagnosis : "");
            pstmt.setString(7, precautions != null ? precautions : "");
            pstmt.setString(8, followup != null ? followup : "");
            pstmt.setString(9, notes != null ? notes : "");
            pstmt.setString(10, medicines);

            // Execute update and check result
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No rows affected, prescription not saved");
            }

            // Commit transaction
//            connection.commit();
            System.out.println("Prescription saved successfully for appointmentNumber: " + appointmentNumber);
        } catch (SQLException e) {
            // Rollback on failure
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
            }
            throw e; // Rethrow to be caught by the caller
        }
    }

    // Fetch Prescription by appointment_number
    // In com.example.healzone.DatabaseConnection.Prescription.java
    public static PrescriptionData fetchPrescriptionById(String appointmentNumber) {
        PrescriptionData prescriptionData = new PrescriptionData();
        String query = "SELECT * FROM prescriptions WHERE appointment_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, appointmentNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                prescriptionData.setDoctorId(rs.getString("doctor_id"));
                prescriptionData.setPatientId(rs.getString("patient_id"));
                prescriptionData.setPatientName(rs.getString("patient_name"));
                prescriptionData.setAppointmentNumber(rs.getString("appointment_number"));
                prescriptionData.setDate(rs.getString("date"));
                prescriptionData.setDiagnosis(rs.getString("diagnosis"));
                prescriptionData.setPrecautions(rs.getString("precautions"));
                prescriptionData.setFollowup(rs.getString("followup")); // Ensure this field is set
                prescriptionData.setNotes(rs.getString("notes"));

                String medicinesString = rs.getString("medicines");
                if (medicinesString != null && !medicinesString.isEmpty()) {
                    ObservableList<MedicationData> medicationList = FXCollections.observableArrayList();
                    String[] medicineEntries = medicinesString.split(";");
                    for (String entry : medicineEntries) {
                        String[] fields = entry.split("\\|");
                        if (fields.length >= 6) { // Minimum required fields
                            MedicationData medData = new MedicationData(
                                    fields[0], // medicineName
                                    fields[1], // type
                                    fields[2], // dosage
                                    fields[3], // frequency
                                    fields[4], // timing
                                    fields[5], // duration
                                    fields.length > 6 ? fields[6] : "" // instructions
                            );
                            medicationList.add(medData);
                        }
                    }
                    prescriptionData.setMedications(medicationList);
                }
                return prescriptionData;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching prescription: " + e.getMessage());
        }
        return null;
    }

    // Parse medicines string into List<MedicationData>
    private static List<MedicationData> parseMedications(String medicines) {
        List<MedicationData> medicationList = new ArrayList<>();
        if (medicines == null || medicines.trim().isEmpty()) {
            System.out.println("No medicines data provided.");
            return medicationList;
        }

        System.out.println("Parsing medicines string: " + medicines);
        // Split by semicolon for multiple medicines
        String[] medicineEntries = medicines.split(";");
        System.out.println("Found " + medicineEntries.length + " medicine entries.");

        for (String entry : medicineEntries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) continue;
            System.out.println("Processing entry: " + trimmedEntry);

            MedicationData med = new MedicationData();
            try {
                // Match the pattern: "medicineName (type) - dosage, frequency, timing, duration"
                String[] parts = trimmedEntry.split(" - ");
                if (parts.length >= 2) {
                    // Extract medicineName and type
                    String[] nameTypeParts = parts[0].trim().split("\\(");
                    if (nameTypeParts.length >= 2) {
                        med.setMedicineName(nameTypeParts[0].trim());
                        med.setType(nameTypeParts[1].replace(")", "").trim());
                    } else {
                        med.setMedicineName(parts[0].trim());
                        med.setType("Unknown");
                    }

                    // Extract dosage, frequency, timing, duration
                    String[] details = parts[1].split(",");
                    if (details.length >= 4) {
                        med.setDosage(details[0].trim());
                        med.setFrequency(details[1].trim());
                        med.setTiming(details[2].trim());
                        med.setDuration(details[3].trim());
                    } else {
                        System.out.println("Insufficient details in: " + parts[1]);
                    }
                } else {
                    System.out.println("Invalid entry format: " + trimmedEntry);
                    med.setMedicineName(trimmedEntry); // Fallback to use the whole string as name
                }
                medicationList.add(med);
                System.out.println("Added: " + med.getMedicineName() + " - Dosage: " + med.getDosage() + ", Frequency: " + med.getFrequency());
            } catch (Exception e) {
                System.out.println("Error parsing entry: " + trimmedEntry + " - " + e.getMessage());
            }
        }
        System.out.println("Total medications parsed: " + medicationList.size());
        return medicationList;
    }

    // Static block to create table on class load
    static {
        createPrescriptionTable();
    }
}