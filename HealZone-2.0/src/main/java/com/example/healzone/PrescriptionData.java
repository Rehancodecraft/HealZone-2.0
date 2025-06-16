package com.example.healzone;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionData {
    private String appointmentNumber;
    private int id;
    private String patientName;
    private int patientAge;
    private String patientGender;
    private String patientId;
    private String doctorName;
    private String doctorSpecialization;
    private String licenseNumber;
    private String hospitalName;
    private String hospitalAddress;
    private String doctorPhone;
    private String doctorEmail;
    private String diagnosis;
    private List<MedicationData> medications; // Already contains the parsed medicines
    private String precautions;
    private String followup;
    private String notes;
    private String doctorId;
    private String date; // Added date field

    // Constructor to initialize medications list
    public PrescriptionData() {
        this.medications = new ArrayList<>(); // Initialize to avoid null
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public int getPatientAge() { return patientAge; }
    public void setPatientAge(int patientAge) { this.patientAge = patientAge; }
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getHospitalAddress() { return hospitalAddress; }
    public void setHospitalAddress(String hospitalAddress) { this.hospitalAddress = hospitalAddress; }
    public String getDoctorPhone() { return doctorPhone; }
    public void setDoctorPhone(String doctorPhone) { this.doctorPhone = doctorPhone; }
    public String getDoctorEmail() { return doctorEmail; }
    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public List<MedicationData> getMedications() { return medications; }
    public void setMedications(List<MedicationData> medications) { this.medications = medications != null ? medications : new ArrayList<>(); }
    public String getPrecautions() { return precautions; }
    public void setPrecautions(String precautions) { this.precautions = precautions; }
    public String getFollowup() { return followup; }
    public void setFollowup(String followup) { this.followup = followup; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDate() { return date; } // Added getter
    public void setDate(String date) { this.date = date; } // Added setter

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }
    public String getAppointmentNumber(){
        return appointmentNumber;
    }
}