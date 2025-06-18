
package com.example.healzone.repository;

import com.example.healzone.model.DoctorModel;
import com.example.healzone.model.DoctorCardDataModel;
import com.example.healzone.model.TimeSlotModel;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.healzone.config.DatabaseConfig.connection;

public class DoctorRepository {
    public static void createTableForPersonalDetailsOfDoctors() {
        String sql = """
            CREATE TABLE IF NOT EXISTS doctors (
                govt_id VARCHAR(50) PRIMARY KEY NOT NULL,
                first_name VARCHAR(50) NOT NULL,
                last_name VARCHAR(50) NOT NULL,
                email VARCHAR(100) NOT NULL,
                phone_number VARCHAR(20) NOT NULL
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Doctors table created");
        } catch (SQLException e) {
            System.err.println("Error creating doctors table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableForProfessionalDetailsOfDoctor() {
        String sql = """
            CREATE TABLE IF NOT EXISTS professional_details (
                professional_id SERIAL PRIMARY KEY NOT NULL,
                govt_id VARCHAR(50) NOT NULL,
                specialization VARCHAR(100) NOT NULL,
                degrees TEXT NOT NULL,
                medical_license_number VARCHAR(100) NOT NULL,
                bio TEXT,
                experience VARCHAR(3) NOT NULL,
                FOREIGN KEY (govt_id) REFERENCES doctors(govt_id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Professional details table created");
        } catch (SQLException e) {
            System.err.println("Error creating professional_details table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableForPracticeInformationOfDoctor() {
        String sql = """
            CREATE TABLE IF NOT EXISTS practice_information (
                practice_id SERIAL PRIMARY KEY NOT NULL,
                govt_id VARCHAR(50) NOT NULL,
                hospital_name VARCHAR(150) NOT NULL,
                hospital_address TEXT NOT NULL,
                consultation_fee VARCHAR(4) NOT NULL,
                FOREIGN KEY (govt_id) REFERENCES doctors(govt_id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Practice information table created");
        } catch (SQLException e) {
            System.err.println("Error creating practice_information table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableForAvailabilityOfDoctor() {
        String sql = """
            CREATE TABLE IF NOT EXISTS doctor_availability (
                id SERIAL PRIMARY KEY NOT NULL,
                govt_id VARCHAR(50) NOT NULL,
                day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')),
                start_time TIME NOT NULL,
                end_time TIME NOT NULL,
                FOREIGN KEY (govt_id) REFERENCES doctors(govt_id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Doctor availability table created");
        } catch (SQLException e) {
            System.err.println("Error creating doctor_availability table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableForSecurityOfDoctor() {
        String sql = """
            CREATE TABLE IF NOT EXISTS doctor_security (
                security_id SERIAL PRIMARY KEY NOT NULL,
                govt_id VARCHAR(50) NOT NULL,
                password TEXT NOT NULL,
                user_agreement BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (govt_id) REFERENCES doctors(govt_id) ON DELETE CASCADE
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Doctor security table created");
        } catch (SQLException e) {
            System.err.println("Error creating doctor_security table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createTableForDoctorReviews() {
        String sql = """
            CREATE TABLE IF NOT EXISTS doctor_reviews (
                review_id SERIAL PRIMARY KEY NOT NULL,
                govt_id VARCHAR(50) NOT NULL,
                phone_number VARCHAR(15) NOT NULL,
                rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                review_text TEXT,
                satisfaction_score INTEGER CHECK (satisfaction_score BETWEEN 0 AND 100),
                review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (govt_id) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                FOREIGN KEY (phone_number) REFERENCES patients(phone_number) ON DELETE SET NULL
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Doctor reviews table created");
        } catch (SQLException e) {
            System.err.println("Error creating doctor_reviews table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertDoctorPersonalDetails(String govtID, String firstName, String lastName, String email, String phone) {
        String insert = """
            INSERT INTO doctors (govt_id, first_name, last_name, email, phone_number)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtID);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.executeUpdate();
            System.out.println("Personal Details inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting doctor personal details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertDoctorProfessionalDetails(String govtID, String specialization, String degrees, String licenseNumber, String bio, String experience) {
        String insert = """
            INSERT INTO professional_details (govt_id, specialization, degrees, medical_license_number, bio, experience)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtID);
            ps.setString(2, specialization);
            ps.setString(3, degrees);
            ps.setString(4, licenseNumber);
            ps.setString(5, bio);
            ps.setString(6, experience);
            ps.executeUpdate();
            System.out.println("Professional Details inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting doctor professional details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertPracticeInfo(String govtId, String hospitalName, String hospitalAddress, String fee) {
        String insert = """
            INSERT INTO practice_information (govt_id, hospital_name, hospital_address, consultation_fee)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtId);
            ps.setString(2, hospitalName);
            ps.setString(3, hospitalAddress);
            ps.setString(4, fee);
            ps.executeUpdate();
            System.out.println("Practice info inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting practice info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertAvailabilityOfDoctor(String govtId, String day, LocalTime start, LocalTime end) {
        String insert = """
            INSERT INTO doctor_availability (govt_id, day_of_week, start_time, end_time)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setString(1, govtId);
            stmt.setString(2, day);
            stmt.setTime(3, start != null ? Time.valueOf(start) : null);
            stmt.setTime(4, end != null ? Time.valueOf(end) : null);
            stmt.executeUpdate();
            System.out.println("Availability inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting availability: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void insertSecurityDetails(String govtId, String password, boolean agreement) {
        String insert = """
            INSERT INTO doctor_security (govt_id, password, user_agreement)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtId);
            ps.setString(2, password);
            ps.setBoolean(3, agreement);
            ps.executeUpdate();
            System.out.println("Security details inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting security details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean checkPhoneNumber(String phoneNumber) {
        String sql = "SELECT phone_number FROM doctors WHERE phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            return !rs.next(); // Return true if phone number doesn't exist
        } catch (SQLException e) {
            System.err.println("Error checking phone number: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkEmail(String email) {
        String sql = "SELECT email FROM doctors WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Return true if email exists
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkGovtID(String govtID) {
        String sql = "SELECT govt_id FROM doctors WHERE govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, govtID);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Return true if govt_id exists
        } catch (SQLException e) {
            System.err.println("Error checking govt_id: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkGovtID(String govtID, String email) {
        String sql = "SELECT govt_id, first_name, last_name FROM doctors WHERE govt_id = ? AND email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, govtID);
            stmt.setString(2, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                DoctorModel.setFirstName(rs.getString("first_name"));
                DoctorModel.setLastName(rs.getString("last_name"));
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking govt_id and email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, Object> getCurrentDoctorDetails(String email, String password) {
        String sql = """
            SELECT d.govt_id, d.first_name, d.last_name, d.email, d.phone_number,
                   pd.specialization, pd.degrees, pd.medical_license_number, pd.bio, pd.experience,
                   pi.hospital_name, pi.hospital_address, pi.consultation_fee,
                   da.day_of_week, da.start_time, da.end_time
            FROM doctors d
            JOIN professional_details pd ON d.govt_id = pd.govt_id
            JOIN practice_information pi ON d.govt_id = pi.govt_id
            LEFT JOIN doctor_availability da ON d.govt_id = da.govt_id
            JOIN doctor_security sd ON d.govt_id = sd.govt_id
            WHERE d.email = ? AND sd.password = ?
            """;
        Map<String, Object> doctorData = new HashMap<>();
        Map<String, TimeSlotModel> availability = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // Match database time format
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                // Scalar fields (set once from first row)
                if (doctorData.isEmpty()) {
                    doctorData.put("govt_id", rs.getString("govt_id"));
                    doctorData.put("first_name", rs.getString("first_name"));
                    doctorData.put("last_name", rs.getString("last_name"));
                    doctorData.put("email", rs.getString("email"));
                    doctorData.put("phone_number", rs.getString("phone_number"));
                    doctorData.put("specialization", rs.getString("specialization"));
                    doctorData.put("degrees", rs.getString("degrees"));
                    doctorData.put("medical_license_number", rs.getString("medical_license_number"));
                    doctorData.put("bio", rs.getString("bio"));
                    doctorData.put("experience", rs.getString("experience"));
                    doctorData.put("hospital_name", rs.getString("hospital_name"));
                    doctorData.put("hospital_address", rs.getString("hospital_address"));
                    doctorData.put("consultation_fee", rs.getString("consultation_fee"));
                }
                // Availability records
                String day = rs.getString("day_of_week");
                if (day != null) {
                    Time sqlStartTime = rs.getTime("start_time");
                    Time sqlEndTime = rs.getTime("end_time");
                    LocalTime startTime = sqlStartTime != null ? sqlStartTime.toLocalTime() : null;
                    LocalTime endTime = sqlEndTime != null ? sqlEndTime.toLocalTime() : null;
                    if (startTime != null && endTime != null) {
                        availability.put(day, new TimeSlotModel(startTime, endTime));
                        System.out.println("Loaded availability for " + day + ": " + startTime + " - " + endTime); // Debug
                    }
                }
            }
            if (hasData) {
                doctorData.put("availability", availability);
                // Set Doctor static fields
                DoctorModel.setGovtID((String) doctorData.get("govt_id"));
                DoctorModel.setFirstName((String) doctorData.get("first_name"));
                DoctorModel.setLastName((String) doctorData.get("last_name"));
                DoctorModel.setEmail((String) doctorData.get("email"));
                DoctorModel.setPhone((String) doctorData.get("phone_number"));
                DoctorModel.setSpecialization((String) doctorData.get("specialization"));
                DoctorModel.setDegrees((String) doctorData.get("degrees"));
                DoctorModel.setMedicalLicenseNumber((String) doctorData.get("medical_license_number"));
                DoctorModel.setBio((String) doctorData.get("bio"));
                DoctorModel.setExperience((String) doctorData.get("experience"));
                DoctorModel.setHospitalName((String) doctorData.get("hospital_name"));
                DoctorModel.setHospitalAddress((String) doctorData.get("hospital_address"));
                DoctorModel.setConsultationFee((String) doctorData.get("consultation_fee"));
                DoctorModel.clearAvailability();
                availability.forEach(DoctorModel::addAvailability);
            }
            return doctorData;
        } catch (SQLException e) {
            System.err.println("Error getting doctor details: " + e.getMessage());
            e.printStackTrace();
            return doctorData;
        }
    }

    public static void resetPassword(String password, String email) {
        String sql = "UPDATE doctor_security SET password = ? WHERE govt_id IN (SELECT govt_id FROM doctors WHERE email = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("Password reset successfully");
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static Map<String, TimeSlotModel> getDoctorAvailability(String govtId) {
        String query = """
            SELECT day_of_week, start_time, end_time
            FROM doctor_availability
            WHERE govt_id = ?
            ORDER BY CASE day_of_week
                WHEN 'Monday' THEN 1
                WHEN 'Tuesday' THEN 2
                WHEN 'Wednesday' THEN 3
                WHEN 'Thursday' THEN 4
                WHEN 'Friday' THEN 5
                WHEN 'Saturday' THEN 6
                WHEN 'Sunday' THEN 7
            END
            """;
        Map<String, TimeSlotModel> availability = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, govtId);
            ResultSet rs = ps.executeQuery();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
            while (rs.next()) {
                String day = rs.getString("day_of_week");
                Time sqlStartTime = rs.getTime("start_time");
                Time sqlEndTime = rs.getTime("end_time");
                LocalTime startTime = sqlStartTime != null ? sqlStartTime.toLocalTime() : null;
                LocalTime endTime = sqlEndTime != null ? sqlEndTime.toLocalTime() : null;
                if (startTime != null && endTime != null) {
                    availability.put(day, new TimeSlotModel(startTime, endTime));
                }
            }
            return availability;
        } catch (SQLException e) {
            System.err.println("Error getting doctor availability: " + e.getMessage());
            e.printStackTrace();
            return availability;
        }
    }

    public static List<String> getReviewsForDoctor(String govtId) {
        List<String> reviews = new ArrayList<>();
        String sql = """
            SELECT p.name, dr.review_text, dr.review_date 
            FROM doctor_reviews dr
            JOIN patients p ON dr.phone_number = p.phone_number
            WHERE dr.govt_id = ?
            ORDER BY dr.review_date DESC
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, govtId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String review = rs.getString("review_text");
                String date = rs.getString("review_date");
                reviews.add(name + " (" + date + "): " + review);
            }
        } catch (SQLException e) {
            System.err.println("Error getting reviews: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }

    public static double getAverageRating(String govtId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM doctor_reviews WHERE govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, govtId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static double getAverageSatisfaction(String govtId) {
        String sql = "SELECT AVG(satisfaction_score) AS avg_satisfaction FROM doctor_reviews WHERE govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, govtId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_satisfaction");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average satisfaction: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public static List<DoctorCardDataModel> loadDoctorCards(String searchTerm, int limit) {
        List<DoctorCardDataModel> doctorList = new ArrayList<>();
        String sql = """
        SELECT d.govt_id, d.first_name, d.last_name,
               pd.specialization, pd.degrees, pd.experience,
               pi.hospital_name, pi.hospital_address, pi.consultation_fee,
               da.start_time, da.end_time,
               (SELECT ARRAY_AGG(da2.day_of_week) 
                FROM doctor_availability da2 
                WHERE da2.govt_id = d.govt_id) AS available_days
        FROM doctors d
        JOIN professional_details pd ON d.govt_id = pd.govt_id
        JOIN practice_information pi ON d.govt_id = pi.govt_id
        JOIN doctor_availability da ON d.govt_id = da.govt_id
        WHERE LOWER(d.first_name) LIKE ? OR
              LOWER(d.last_name) LIKE ? OR
              LOWER(pd.specialization) LIKE ? OR
              LOWER(pi.hospital_name) LIKE ? OR
              LOWER(pi.hospital_address) LIKE ?
        GROUP BY d.govt_id, d.first_name, d.last_name,
                 pd.specialization, pd.degrees, pd.experience,
                 pi.hospital_name, pi.hospital_address, pi.consultation_fee,
                 da.start_time, da.end_time
        ORDER BY RANDOM()
        LIMIT ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }
            stmt.setInt(6, limit); // Set the LIMIT parameter
            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("h:mm a");
            while (rs.next()) {
                DoctorCardDataModel details = new DoctorCardDataModel();
                String govtId = rs.getString("govt_id");
                details.setFullName("Dr."+rs.getString("first_name") + " " + rs.getString("last_name"));
                details.setSpecialization(rs.getString("specialization"));
                details.setDegrees(rs.getString("degrees"));
                details.setExperience(rs.getString("experience"));
                details.setHospitalName(rs.getString("hospital_name"));
                details.setHospitalAddress(rs.getString("hospital_address"));
                details.setConsultationFee(rs.getString("consultation_fee"));
                String startTimeStr = rs.getString("start_time");
                String endTimeStr = rs.getString("end_time");
                try {
                    LocalTime startTime = LocalTime.parse(startTimeStr, inputFormatter);
                    LocalTime endTime = LocalTime.parse(endTimeStr, inputFormatter);
                    details.setStartTime(startTime.format(outputFormatter));
                    details.setEndTime(endTime.format(outputFormatter));
                    System.out.println(details.getStartTime() + " " + details.getEndTime());
                } catch (Exception e) {
                    System.err.println("Error parsing time: " + e.getMessage());
                    details.setStartTime("Invalid Time");
                    details.setEndTime("Invalid Time");
                }
                details.setGovtId(govtId);
                Array availableDaysArray = rs.getArray("available_days");
                if (availableDaysArray != null) {
                    String[] availableDays = (String[]) availableDaysArray.getArray();
                    details.setAvailableDays(availableDays);
                } else {
                    details.setAvailableDays(new String[0]);
                }
                details.setReviews(getReviewsForDoctor(govtId));
                details.setAverageRating(getAverageRating(govtId));
                details.setSatisfactionScore(getAverageSatisfaction(govtId));
                doctorList.add(details);
            }
        } catch (SQLException e) {
            System.err.println("Error loading doctor cards: " + e.getMessage());
            e.printStackTrace();
        }
        return doctorList;
    }
    public static void updatePersonalDetails(String govtId, String firstName, String lastName, String email, String phone) {
        String update = """
            UPDATE doctors
            SET first_name = ?, last_name = ?, email = ?, phone_number = ?
            WHERE govt_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, govtId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Personal details updated for govtId: " + govtId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating personal details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update professional details
    public static void updateProfessionalDetails(String govtId, String specialization, String degrees, String licenseNumber, String bio, String experience) {
        String update = """
            UPDATE professional_details
            SET specialization = ?, degrees = ?, medical_license_number = ?, bio = ?, experience = ?
            WHERE govt_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, specialization);
            ps.setString(2, degrees);
            ps.setString(3, licenseNumber);
            ps.setString(4, bio);
            ps.setString(5, experience);
            ps.setString(6, govtId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Professional details updated for govtId: " + govtId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating professional details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update practice information
    public static void updatePracticeInfo(String govtId, String hospitalName, String hospitalAddress, String consultationFee) {
        String update = """
            UPDATE practice_information
            SET hospital_name = ?, hospital_address = ?, consultation_fee = ?
            WHERE govt_id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, hospitalName);
            ps.setString(2, hospitalAddress);
            ps.setString(3, consultationFee);
            ps.setString(4, govtId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Practice info updated for govtId: " + govtId);
            }
        } catch (SQLException e) {
            System.err.println("Error updating practice info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Clear existing availability
    public static void clearDoctorAvailability(String govtId) {
        String deleteSql = "DELETE FROM doctor_availability WHERE govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, govtId);
            stmt.executeUpdate();
            System.out.println("Old availability cleared for govtId: " + govtId);
        } catch (SQLException e) {
            System.err.println("Error clearing old availability: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
