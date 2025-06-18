package com.example.healzone.Checks;

import com.example.healzone.model.PatientModel;

public class ChecksForPatientFields {
    public static boolean isEmpty(){
        return PatientModel.getName().isEmpty() || PatientModel.getFatherName().isEmpty() || PatientModel.getAge().isEmpty()
                || PatientModel.getEmail().isEmpty() || PatientModel.getGender().isEmpty() || PatientModel.getPhone().isEmpty();
    }
    public static boolean isAgeValid(){
        System.out.println(PatientModel.getAge());
        int age = Integer.parseInt(PatientModel.getAge().trim());
        return age > 0 && age < 120;
    }
    public static boolean isNameValid(){
        return PatientModel.getName().trim().matches("^(?=.{1,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$");
    }
    public static boolean isFatherNameValid(){

        return PatientModel.getFatherName().trim().matches("^(?=.{1,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$");
    }
    public static boolean isPhoneNumberValid() {
        return PatientModel.getPhone().matches("^03\\d{9}$");
    }
    public static boolean isEmailValid(){
        return PatientModel.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    public static boolean isPasswordMatch(){
        return PatientModel.getPassword().equals(PatientModel.getConfirmPassword());
    }
    public static boolean isPasswordValid(){
        return PatientModel.getPassword().length() >= 8;
    }
}


