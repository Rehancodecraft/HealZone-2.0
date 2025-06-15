package com.example.healzone;
import java.util.List;

public class PrescriptionData {
    // Patient information
    private String patientName;
    private int patientAge;
    private String patientGender;
    private String patientId;

    // Doctor information
    private String doctorName;
    private String doctorSpecialization;
    private String licenseNumber;
    private String registrationNumber;

    // Prescription content
    private String diagnosis;
    private List<MedicationData> medications;
    private String precautions;
    private String followup;
    private String notes;

    // Constructors
    public PrescriptionData() {}

    public PrescriptionData(String patientName, int patientAge, String patientGender, String patientId) {
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientId = patientId;
    }

    // Getters and Setters for Patient Information
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public int getPatientAge() { return patientAge; }
    public void setPatientAge(int patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    // Getters and Setters for Doctor Information
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    // Getters and Setters for Prescription Content
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public List<MedicationData> getMedications() { return medications; }
    public void setMedications(List<MedicationData> medications) { this.medications = medications; }

    public String getPrecautions() { return precautions; }
    public void setPrecautions(String precautions) { this.precautions = precautions; }

    public String getFollowup() { return followup; }
    public void setFollowup(String followup) { this.followup = followup; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
class DoctorInfo {
    private final String fullName;
    private final String specialization;
    private final String licenseNumber;
    private final String registrationNumber;

    public DoctorInfo(String fullName, String specialization, String licenseNumber, String registrationNumber) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.registrationNumber = registrationNumber;
    }

    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
}
