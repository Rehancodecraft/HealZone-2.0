package com.example.healzone.model;

import com.example.healzone.repository.DoctorRepository;

import java.util.Map;

public class LoggedInDoctorModel {
    public static boolean getCurrentDoctor(String email, String password) {
        Map<String, Object> doctorData = DoctorRepository.getCurrentDoctorDetails(email, password);
        if (!doctorData.isEmpty()) {
            DoctorModel.setGovtID((String) doctorData.get("govt_id"));
            DoctorModel.setFirstName((String) doctorData.get("first_name"));
            DoctorModel.setLastName((String) doctorData.get("last_name"));
            DoctorModel.setEmail((String) doctorData.get("email"));
            DoctorModel.setPhone((String) doctorData.get("phone_number"));
            DoctorModel.setSpecialization((String) doctorData.get("specialization"));
            DoctorModel.setDegrees((String) doctorData.get("degrees"));
            DoctorModel.setMedicalLicenseNumber((String) doctorData.get("medical_license_number"));
            DoctorModel.setBio((String) doctorData.get("bio"));
            DoctorModel.setHospitalName((String) doctorData.get("hospital_name"));
            DoctorModel.setHospitalAddress((String) doctorData.get("hospital_address"));
            DoctorModel.setConsultationFee((String) doctorData.get("consultation_fee"));
            DoctorModel.setExperience((String) doctorData.get("experience")); // Add experience

            // Clear existing availability
            DoctorModel.getAvailability().clear();
            // Add new availability
            Map<String, TimeSlotModel> availability = (Map<String, TimeSlotModel>) doctorData.get("availability");
            availability.forEach((day, slot) -> DoctorModel.addAvailability(day, slot));

            return true;
        }
        return false;
    }
}