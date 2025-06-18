package com.example.healzone.util;

import com.example.healzone.model.DoctorModel;

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
        DoctorModel.setGovtID(null);
        DoctorModel.setFirstName(null);
        DoctorModel.setLastName(null);
        DoctorModel.setEmail(null);
        DoctorModel.setPhone(null);
        DoctorModel.setSpecialization(null);
        DoctorModel.setDegrees(null);
        DoctorModel.setMedicalLicenseNumber(null);
        DoctorModel.setBio(null);
        DoctorModel.setHospitalName(null);
        DoctorModel.setHospitalAddress(null);
        DoctorModel.setConsultationFee(null);

    }
}