
package com.example.healzone.Doctor;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class Doctor {
    private static String firstName;
    private static String lastName;
    private static String govtID;
    private static String specialization;
    private static String degrees;
    private static String password;
    private static String confirmPassword;
    private static String phone;
    private static String email;
    private static String hospitalName;
    private static String hospitalAddress;
    private static String medicalLicenseNumber;
    private static String consultationFee;
    private static String bio;
    private static String experience;

    // ✅ Temporary storage for availability (day -> TimeSlot)
    private static Map<String, TimeSlot> availability = new HashMap<>();

    public static void addAvailability(String day, TimeSlot slot) {
        availability.put(day, slot);
    }

    public static Map<String, TimeSlot> getAvailability() {
        return availability;
    }

    // (Existing getters and setters below...)

    public static String getFirstName() { return firstName; }
    public static void setFirstName(String firstName) { Doctor.firstName = firstName; }
    public static String getLastName() { return lastName; }
    public static void setLastName(String lastName) { Doctor.lastName = lastName; }
    public static String getGovtID() { return govtID; }
    public static void setGovtID(String govtID) { Doctor.govtID = govtID; }
    public static String getSpecialization() { return specialization; }
    public static void setSpecialization(String specialization) { Doctor.specialization = specialization; }
    public static String getDegrees() { return degrees; }
    public static void setDegrees(String degrees) { Doctor.degrees = degrees; }
    public static String getPassword() { return password; }
    public static void setPassword(String password) { Doctor.password = password; }
    public static String getConfirmPassword() { return confirmPassword; }
    public static void setConfirmPassword(String confirmPassword) { Doctor.confirmPassword = confirmPassword; }
    public static String getPhone() { return phone; }
    public static void setPhone(String phone) { Doctor.phone = phone; }
    public static String getEmail() { return email; }
    public static void setEmail(String email) { Doctor.email = email; }
    public static String getHospitalName() { return hospitalName; }
    public static void setHospitalName(String hospitalName) { Doctor.hospitalName = hospitalName; }
    public static String getHospitalAddress() { return hospitalAddress; }
    public static void setHospitalAddress(String hospitalAddress) { Doctor.hospitalAddress = hospitalAddress; }
    public static String getMedicalLicenseNumber() { return medicalLicenseNumber; }
    public static void setMedicalLicenseNumber(String medicalLicenseNumber) { Doctor.medicalLicenseNumber = medicalLicenseNumber; }
    public static String getConsultationFee() { return consultationFee; }
    public static void setConsultationFee(String consultationFee) { Doctor.consultationFee = consultationFee; }
    public static String getBio() { return bio; }
    public static void setBio(String bio) { Doctor.bio = bio; }

    public static String getExperience() {
        return experience;
    }

    public static void setExperience(String experience) {
        Doctor.experience = experience;
    }
}
