package com.example.healzone.Checks;

import com.example.healzone.Doctor.Doctor;
import com.example.healzone.Patient.Patient;

import javax.print.Doc;

public class ChecksForDoctor {
    public static boolean isPersonalDetailsEmpty(){
        return Doctor.getFirstName().isEmpty() || Doctor.getLastName().isEmpty() || Doctor.getGovtID().isEmpty()
                || Doctor.getEmail().isEmpty() || Doctor.getPhone().isEmpty();
    }

    public static boolean isPracticeInformationEmpty(){
        return Doctor.getHospitalAddress().isEmpty() || Doctor.getHospitalName().isEmpty() || Doctor.getConsultationFee().isEmpty();
    }
    public static boolean isProfessionalDetailsEmpty(){
        return Doctor.getSpecialization().isEmpty() || Doctor.getDegrees().isEmpty() || Doctor.getMedicalLicenseNumber().isEmpty();
    }
    public static boolean isTimeTableEmpty(){
        return Doctor.getBio().isEmpty();
    }
    public static boolean isSecurityEmpty(){
        return Doctor.getPassword().isEmpty() || Doctor.getConfirmPassword().isEmpty();
    }

    public static boolean isFirstNameValid(){
        return Doctor.getFirstName().matches("^[A-Za-z\\s'-]+$");
    }
    public static boolean isLastNameValid(){
        return Doctor.getLastName().matches("^[A-Za-z\\s'-]+$");
    }
    public static boolean isPhoneNumberValid(){
        return Doctor.getPhone().matches("\\d+") || Doctor.getPhone().length() == 11 || Doctor.getPhone().startsWith("03");
    }
    public static boolean isEmailValid(String email){
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    public static boolean isPasswordMatch(){
        return Doctor.getPassword().equals(Doctor.getConfirmPassword());
    }
    public static boolean isPasswordValid(){
        return Doctor.getPassword().length() >= 8;
    }
    public static boolean isGovtIDValid(){
        return Doctor.getGovtID().matches("^\\d{5}\\d{7}\\d$");
    }
    public static boolean isHospitalNameValid(){
        return Doctor.getHospitalName().matches("^[A-Za-z0-9&.,\\-\\s]{3,100}$");
    }
    public static boolean isHospitalAddressValid(){
        return Doctor.getHospitalAddress().matches( "^[A-Za-z0-9\\s,./#()\\-:]{5,200}$");

    }
    public static boolean isMedicalLicenseValid(){
        return Doctor.getMedicalLicenseNumber().matches("^PMDC-\\d{5,6}$");
    }
    public static boolean isDegreesValid(){
        return Doctor.getDegrees().matches("^([A-Z]{2,10}(\\s*\\([A-Za-z\\s]+\\))?)(,\\s*[A-Z]{2,10}(\\s*\\([A-Za-z\\s]+\\))?)*$");
    }
    public static boolean isConsultationFeeValid() {
        String feeStr = Doctor.getConsultationFee();

        // Check if it only contains digits
        if (!feeStr.matches("\\d+")) {
            return false;
        }

        // Parse the number and check the range
        int fee = Integer.parseInt(feeStr);
        return fee > 100 && fee < 10000;
    }
}
