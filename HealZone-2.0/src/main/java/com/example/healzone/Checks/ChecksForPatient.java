package com.example.healzone.Checks;

import com.example.healzone.Patient.Patient;

public class ChecksForPatient {
    public static boolean isEmpty(){
        return Patient.getName().isEmpty() || Patient.getFatherName().isEmpty() || Patient.getAge().isEmpty()
                || Patient.getEmail().isEmpty() || Patient.getGender().isEmpty() || Patient.getPhone().isEmpty();
    }
    public static boolean isAgeValid(){
        System.out.println(Patient.getAge());
        int age = Integer.parseInt(Patient.getAge().trim());
        return age > 0 && age < 120;
    }
    public static boolean isNameValid(){
        return Patient.getName().matches("^[A-Za-z\\s'-]+$");
    }
    public static boolean isFatherNameValid(){
        return Patient.getName().matches("^[A-Za-z\\s'-]+$");
    }
    public static boolean isPhoneNumberValid(){
        return Patient.getPhone().matches("\\d+") || Patient.getPhone().length() == 11 || Patient.getPhone().startsWith("03");
    }
    public static boolean isEmailValid(){
        return Patient.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    public static boolean isPasswordMatch(){
        return Patient.getPassword().equals(Patient.getConfirmPassword());
    }
    public static boolean isPasswordValid(){
        return Patient.getPassword().length() >= 8;
    }

}


