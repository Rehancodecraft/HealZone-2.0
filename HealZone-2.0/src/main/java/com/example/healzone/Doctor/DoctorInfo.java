package com.example.healzone.Doctor;

public class DoctorInfo {
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
