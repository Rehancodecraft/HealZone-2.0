package com.example.healzone.DatabaseConnection;

import com.example.healzone.MedicationData;
import com.example.healzone.PrescriptionData;
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(date, formatter);
            java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);
            pstmt.setDate(4, sqlDate);
            pstmt.setString(5, diagnosis);
            pstmt.setString(6, precautions);
            pstmt.setString(7, followup);
            pstmt.setString(8, notes);
            pstmt.setString(9, medicines);
            pstmt.executeUpdate();
        }
    }

    // Fetch Prescription by ID
    public static PrescriptionData fetchPrescriptionById(int prescriptionId) {
        PrescriptionData prescriptionData = null;
        String sql = "SELECT p.*, d.first_name, d.last_name, pd.specialization, pd.medical_license_number, " +
                "pat.age AS patient_age, pat.gender AS patient_gender " +
                "FROM prescriptions p " +
                "LEFT JOIN doctors d ON p.doctor_id = d.govt_id " +
                "LEFT JOIN professional_details pd ON p.doctor_id = pd.govt_id " +
                "LEFT JOIN patients pat ON p.patient_id = pat.phone_number " +
                "WHERE p.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                prescriptionData = new PrescriptionData();
                prescriptionData.setPatientName(rs.getString("patient_name"));
                prescriptionData.setPatientAge(rs.getString("patient_age") != null ? Integer.parseInt(rs.getString("patient_age")) : 0); // Convert age to int
                prescriptionData.setPatientGender(rs.getString("patient_gender"));
                prescriptionData.setPatientId(rs.getString("patient_id"));
                // Combine first_name and last_name for doctor_name
                String doctorFirstName = rs.getString("first_name");
                String doctorLastName = rs.getString("last_name");
                prescriptionData.setDoctorName((doctorFirstName != null ? doctorFirstName : "") + " " + (doctorLastName != null ? doctorLastName : ""));
                prescriptionData.setDoctorSpecialization(rs.getString("specialization"));
                prescriptionData.setLicenseNumber(rs.getString("medical_license_number"));
                prescriptionData.setRegistrationNumber("N/A"); // No registration_number in schema, use placeholder
                prescriptionData.setDiagnosis(rs.getString("diagnosis"));
                prescriptionData.setPrecautions(rs.getString("precautions"));
                prescriptionData.setFollowup(rs.getString("followup"));
                prescriptionData.setNotes(rs.getString("notes"));

                // Parse medicines string into MedicationData list
                String medicinesStr = rs.getString("medicines");
                if (medicinesStr != null && !medicinesStr.trim().isEmpty()) {
                    List<MedicationData> medications = new ArrayList<>();
                    String[] medicineLines = medicinesStr.split("\n");
                    for (String line : medicineLines) {
                        if (!line.trim().isEmpty()) {
                            String[] parts = line.split(" - ", 2);
                            if (parts.length == 2) {
                                String[] nameType = parts[0].replace("(", "").replace(")", "").trim().split(" ", 2);
                                if (nameType.length == 2) {
                                    String medicineName = nameType[0];
                                    String type = nameType[1];
                                    String[] details = parts[1].split(", ");
                                    if (details.length >= 4) {
                                        String dosage = details[0].trim();
                                        String frequency = details[1].trim();
                                        String timing = details[2].trim();
                                        String duration = details[3].trim();
                                        String instructions = details.length > 4 ? details[4].trim() : "";
                                        medications.add(new MedicationData(medicineName, type, dosage, frequency, timing, duration, instructions));
                                    }
                                }
                            }
                        }
                    }
                    prescriptionData.setMedications(medications);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching prescription: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing patient age: " + e.getMessage());
            prescriptionData.setPatientAge(0); // Fallback if age parsing fails
        }
        return prescriptionData;
    }

    // Static block to create table on class load
    static {
        createPrescriptionTable();
    }
}