package com.example.healzone.Doctor;

public class DoctorInfo {
    private final String fullName;
    private final String specialization;
    private final String licenseNumber;
    private final String registrationNumber;
    private final String hospitalName;
    private final String hospitalAddress;
    private final String doctorPhone;
    private final String doctorEmail;

    public DoctorInfo(String fullName, String specialization, String licenseNumber, String registrationNumber, String hospitalName, String hospitalAddress, String doctorPhone, String doctorEmail) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.registrationNumber = registrationNumber;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.doctorPhone = doctorPhone;
        this.doctorEmail = doctorEmail;
    }

    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getHospitalName() { return hospitalName; }
    public String getHospitalAddress() { return hospitalAddress; }
    public String getDoctorPhone() { return doctorPhone; }
    public String getDoctorEmail() { return doctorEmail; }
}
