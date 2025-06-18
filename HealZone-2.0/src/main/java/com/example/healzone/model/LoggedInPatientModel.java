package com.example.healzone.model;

import com.example.healzone.repository.PatientRepository;

import java.util.Map;

public class LoggedInPatientModel {


    public static boolean getCurrentPatient(String email, String password) {
        try {
            Map<String, String> patientData = PatientRepository.getCurrentPatientDetails(email, password);
            if (!patientData.isEmpty()) {
                PatientModel.setName(patientData.get("name"));
                PatientModel.setFatherName(patientData.get("father_name"));
                PatientModel.setGender(patientData.get("gender"));
                PatientModel.setAge(patientData.get("age"));
                PatientModel.setPhone(patientData.get("phone_number"));
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
