package com.example.healzone;

import com.example.healzone.Doctor.Doctor;

import javax.print.Doc;

public class SessionManager {
    private static boolean loggedIn = false;
    private static String currentUser; // Stores doctor govtID or patient phone
    private static boolean isDoctor = false;

    public static String isLoggedIn() {
        if (loggedIn) {
            return getCurrentUser();
        } else {
            return null;
        }
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentDoctorId() {
        if (loggedIn && isDoctor) {
            return currentUser; // Assumes currentUser is govtID for doctors
        }
        return null;
    }

    public static boolean isDoctor() {
        return loggedIn && isDoctor;
    }

    public static void logIn(String user, boolean doctor) {
        loggedIn = true;
        currentUser = user;
        isDoctor = doctor;
    }

    public static void logOut() {
        loggedIn = false;
        currentUser = null;
        isDoctor = false;
        Doctor.setGovtID(null);
        Doctor.setFirstName(null);
        Doctor.setLastName(null);
        Doctor.setEmail(null);
        Doctor.setPhone(null);
        Doctor.setSpecialization(null);
        Doctor.setDegrees(null);
        Doctor.setMedicalLicenseNumber(null);
        Doctor.setBio(null);
        Doctor.setHospitalName(null);
        Doctor.setHospitalAddress(null);
        Doctor.setConsultationFee(null);

    }
}