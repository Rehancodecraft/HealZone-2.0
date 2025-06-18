package com.example.healzone.Checks;

import com.example.healzone.model.DoctorModel;

public class ChecksForDoctorFields {
    public static boolean isPersonalDetailsEmpty(){
        return DoctorModel.getFirstName().isEmpty() || DoctorModel.getLastName().isEmpty() || DoctorModel.getGovtID().isEmpty()
                || DoctorModel.getEmail().isEmpty() || DoctorModel.getPhone().isEmpty();
    }

    public static boolean isPracticeInformationEmpty(){
        return DoctorModel.getHospitalAddress().isEmpty() || DoctorModel.getHospitalName().isEmpty() || DoctorModel.getConsultationFee().isEmpty();
    }
    public static boolean isProfessionalDetailsEmpty(){
        return DoctorModel.getSpecialization().isEmpty() || DoctorModel.getDegrees().isEmpty() || DoctorModel.getMedicalLicenseNumber().isEmpty();
    }
//    public static boolean isTimeTableEmpty(){
//        return DoctorModel.getBio().isEmpty();
//    }
    public static boolean isSecurityEmpty(){
        return DoctorModel.getPassword().isEmpty() || DoctorModel.getConfirmPassword().isEmpty();
    }
    public static boolean isExperienceEmpty(){
        return DoctorModel.getExperience().isEmpty();
    }

    public static boolean isFirstNameValid(){
        return DoctorModel.getFirstName().trim().matches("^(?=.{1,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$");
    }
    public static boolean isLastNameValid(){
        return DoctorModel.getLastName().trim().matches("^(?=.{1,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$");
    }
    public static boolean isPhoneNumberValid(){
        return DoctorModel.getPhone().matches("^03\\d{9}$");
    }
    public static boolean isEmailValid(String email){
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    public static boolean isPasswordMatch(){
        return DoctorModel.getPassword().equals(DoctorModel.getConfirmPassword());
    }
    public static boolean isPasswordValid(){
        return DoctorModel.getPassword().length() >= 8;
    }
    public static boolean isGovtIDValid(){
        return DoctorModel.getGovtID().matches("^\\d{5}\\d{7}\\d$");
    }
    public static boolean isHospitalNameValid(){
        return DoctorModel.getHospitalName().matches("^[A-Za-z0-9&.,\\-\\s]{3,100}$");
    }
    public static boolean isHospitalAddressValid(){
        return DoctorModel.getHospitalAddress().matches( "^[A-Za-z0-9\\s,./#()\\-:]{5,200}$");

    }
    public static boolean isMedicalLicenseValid(){
        return DoctorModel.getMedicalLicenseNumber().matches("^PMDC-\\d{5,6}$");
    }
    public static boolean isDegreesValid(){
        return DoctorModel.getDegrees().matches("^([A-Z]{2,10}(\\s*\\([A-Za-z\\s]+\\))?)(,\\s*[A-Z]{2,10}(\\s*\\([A-Za-z\\s]+\\))?)*$");
    }
    public static boolean isConsultationFeeValid() {
        String feeStr = DoctorModel.getConsultationFee();

        // Check if it only contains digits
        if (!feeStr.matches("\\d+")) {
            return false;
        }

        // Parse the number and check the range
        int fee = Integer.parseInt(feeStr);
        return fee > 100 && fee < 10000;
    }
}
