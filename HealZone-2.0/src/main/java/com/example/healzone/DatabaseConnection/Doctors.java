package com.example.healzone.DatabaseConnection;
//
//import com.example.healzone.Doctor.Doctor;
//
//import javax.print.Doc;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Time;
//import java.time.LocalTime;
//
//import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
//
//public class Doctors {
//    public static void createTableForPersonalDetailsOfDoctors(){
//        try {
//            String doctors = """
//
//                    CREATE TABLE IF NOT EXISTS doctors (
//                        govt_id VARCHAR(50) PRIMARY KEY,
//                        first_name VARCHAR(50) NOT NULL,
//                        last_name VARCHAR(50) NOT NULL,
//                        email VARCHAR(100) UNIQUE NOT NULL,
//                        phone_number VARCHAR(20) UNIQUE NOT NULL
//                    );
//
//
//                        """;
//            PreparedStatement preparedStatement = connection.prepareStatement(doctors);
//            preparedStatement.execute();
////            connection.commit();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void createTableForProfessionalDetailsOfDoctor(){
//        try {
//            String professionalDetails = """
//                    CREATE TABLE IF NOT EXISTS professional_details (
//                        professional_id SERIAL PRIMARY KEY,
//                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
//                        specialization VARCHAR(100) NOT NULL,
//                        degrees TEXT NOT NULL,
//                        medical_license_number VARCHAR(100) UNIQUE NOT NULL,
//                        bio TEXT
//                    );
//
//
//                    """;
//            PreparedStatement preparedStatement = connection.prepareStatement(professionalDetails);
//            preparedStatement.execute();
////            connection.commit();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void createTableForPracticeInformationOfDoctor(){
//        try{
//            String practiceInformation = """
//                    CREATE TABLE IF NOT EXISTS practice_information (
//                        practice_id SERIAL PRIMARY KEY,
//                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
//                        hospital_name VARCHAR(150) NOT NULL,
//                        hospital_address TEXT NOT NULL,
//                        consultation_fee VARCHAR(4) NOT NULL
//                    );
//
//                    """;
//            PreparedStatement preparedStatement = connection.prepareStatement(practiceInformation);
//            preparedStatement.execute();
////            connection.commit();
//        } catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void createTableForAvailabilityOfDoctor(){
//        try {
//            String doctorAvailability = """
//
//                    CREATE TABLE IF NOT EXISTS doctor_availability (
//                        id SERIAL PRIMARY KEY,
//                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
//                        day_of_week VARCHAR(10) CHECK (
//                            day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
//                        ),
//                        start_time TIME NOT NULL,
//                        end_time TIME NOT NULL,
//                        UNIQUE(govt_id, day_of_week)
//                    );
//
//
//                        """;
//        PreparedStatement preparedStatement = connection.prepareStatement(
//            doctorAvailability);
//            preparedStatement.execute();
////        connection.commit();
//        } catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void createTableForSecurityOfDoctor(){
//        try {
//            String security = """
//                    CREATE TABLE IF NOT EXISTS doctor_security (
//                        security_id SERIAL PRIMARY KEY,
//                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
//                        password TEXT NOT NULL,
//                        user_agreement BOOLEAN DEFAULT FALSE
//                    );
//
//                    """;
//            PreparedStatement preparedStatement = connection.prepareStatement(security);
//            preparedStatement.execute();
////            connection.commit();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void insertDoctorPersonalDetails(String govtID, String firstName, String lastName, String email, String phone) {
//        String insert = """
//        INSERT INTO doctors (govt_id, first_name, last_name, email, phone_number)
//        VALUES (?, ?, ?, ?, ?);
//    """;
//
//        try {
//            PreparedStatement ps = connection.prepareStatement(insert);
//            ps.setString(1, govtID);
//            ps.setString(2, firstName);
//            ps.setString(3, lastName);
//            ps.setString(4, email);
//            ps.setString(5, phone);
//
//            ps.executeQuery();
//            System.out.println("Personal Details inserted");
////            connection.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//    public static void insertProfessionalDetails(String govtId, String specialization, String degrees, String licenseNumber, String bio) {
//        String insert = """
//        INSERT INTO professional_details (govt_id, specialization, degrees, medical_license_number, bio)
//        VALUES (?, ?, ?, ?, ?);
//    """;
//        try {
//            PreparedStatement ps = connection.prepareStatement(insert);
//            ps.setString(1, govtId);
//            ps.setString(2, specialization);
//            ps.setString(3, degrees);
//            ps.setString(4, licenseNumber);
//            ps.setString(5, bio);
//            ps.executeUpdate();
//            System.out.println("Professional Details inserted");
////            connection.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void insertPracticeInfo(String govtId, String hospitalName, String hospitalAddress, String fee) {
//        String insert = """
//        INSERT INTO practice_information (govt_id, hospital_name, hospital_address, consultation_fee)
//        VALUES (?, ?, ?, ?);
//    """;
//        try {
//            PreparedStatement ps = connection.prepareStatement(insert);
//            ps.setString(1, govtId);
//            ps.setString(2, hospitalName);
//            ps.setString(3, hospitalAddress);
//            ps.setString(4, fee);
//            ps.executeUpdate();
//            System.out.println("Practice infor inserted");
////            connection.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public static void insertAvailabilityOfDoctor(String govtId, String day, LocalTime start, LocalTime end){
//        try {
//            String sql = "INSERT INTO doctor_availability (govt_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1, govtId);
//            stmt.setString(2, day);
//            stmt.setTime(3, Time.valueOf(start));
//            stmt.setTime(4, Time.valueOf(end));
//            stmt.execute();
//            System.out.println("availability inserted");
////            connection.commit();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static void insertSecurityDetails(String govtId, String password, boolean agreement) {
//        String insert = """
//        INSERT INTO doctor_security (govt_id, password, user_agreement)
//        VALUES (?, ?, ?);
//    """;
//        try (PreparedStatement ps = connection.prepareStatement(insert)) {
//            ps.setString(1, govtId);
//            ps.setString(2, password);
//            ps.setBoolean(3, agreement);
//            ps.executeUpdate();
//            System.out.println("security details inserted");
////            connection.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static boolean checkPhoneNumber(String phoneNumber){
//        try{
//            String sql = "select phone_number from doctors where phone_number = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,phoneNumber);
//            ResultSet rs = stmt.executeQuery();
//            if(rs.next() ){
//                return false;
//            }else {
//                return true;
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static boolean checkEmail(String email){
//        try{
//            String sql = "select email from doctors where email = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,email);
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static boolean checkGovtID(String govtID){
//        try{
//            String sql = "select govt_id from doctors where govt_id = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,govtID);
//            ResultSet rs = stmt.executeQuery();
//            if(rs.next()){
//                return true;
//            }else{
//                return false;
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static boolean checkGovtID(String govtID,String email){
//        try{
//            String sql = "select govt_id,first_name,last_name from doctors where govt_id = ? AND email = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,govtID);
//            stmt.setString(2,email);
//            ResultSet rs = stmt.executeQuery();
//            if(rs.next()){
//                Doctor.setFirstName(rs.getString("first_name"));
//                Doctor.setLastName(rs.getString("last_name"));
//                return true;
//            }else {
//                return false;
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//            return false;
//        }
//    }
//    public static ResultSet getCurrentDoctorDetails(String email, String password){
//        try {
//            String sql = """
//            SELECT d.*, pd.*, pi.*, da.*
//            FROM doctors d
//            JOIN professional_details pd ON d.govt_id = pd.govt_id
//            JOIN practice_information pi ON d.govt_id = pi.govt_id
//            LEFT JOIN doctor_availability da ON d.govt_id = da.govt_id
//            JOIN doctor_security sd ON d.govt_id = sd.govt_id
//            WHERE d.email = ? AND sd.password = ?;
//        """;
//
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1, email);
//            stmt.setString(2, password);
//
//            return stmt.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    public static void resetPassword(String password,String email){
//        try{
//            String sql= "UPDATE patients SET password = ? WHERE email = ?;";
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1,password);
//            stmt.setString(2,email);
//            stmt.execute();
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//    }
//    public static ResultSet getCompletedAppointmentsForDoctor(String doctorId) {
//        String query = """
//        SELECT * FROM appointments
//        WHERE doctor_id = ? AND status = 'Completed'
//        ORDER BY appointment_date DESC;
//    """;
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//            ps.setString(1, doctorId);
//            return ps.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    public static ResultSet getUpcomingAppointmentsForDoctor(String doctorId) {
//        String query = """
//        SELECT * FROM appointments
//        WHERE doctor_id = ? AND status = 'Upcoming'
//        ORDER BY appointment_date DESC;
//    """;
//
//        try (PreparedStatement ps = connection.prepareStatement(query)) {
//            ps.setString(1, doctorId);
//            return ps.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//
//}


import com.example.healzone.Doctor.Doctor;
import com.example.healzone.Doctor.TimeSlot;

import java.sql.*;
import java.time.LocalTime;
import java.util.*;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Doctors {
    public static void createTableForPersonalDetailsOfDoctors() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("govt_id", "VARCHAR(50)", true, true, null, null),
                new DatabaseUtils.Column("first_name", "VARCHAR(50)", true, false, null, null),
                new DatabaseUtils.Column("last_name", "VARCHAR(50)", true, false, null, null),
                new DatabaseUtils.Column("email", "VARCHAR(100)", true, false, null, null),
                new DatabaseUtils.Column("phone_number", "VARCHAR(20)", true, false, null, null)
        );
        DatabaseUtils.createTable("doctors", columns, null, null, null);
    }

    public static void createTableForProfessionalDetailsOfDoctor() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("professional_id", "SERIAL", true, true, null, null),
                new DatabaseUtils.Column("govt_id", "VARCHAR(50)", true, false, "doctors(govt_id) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("specialization", "VARCHAR(100)", true, false, null, null),
                new DatabaseUtils.Column("degrees", "TEXT", true, false, null, null),
                new DatabaseUtils.Column("medical_license_number", "VARCHAR(100)", true, false, null, null),
                new DatabaseUtils.Column("bio", "TEXT", false, false, null, null)
        );
        DatabaseUtils.createTable("professional_details", columns, null, null, null);
    }

    public static void createTableForPracticeInformationOfDoctor() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("practice_id", "SERIAL", true, true, null, null),
                new DatabaseUtils.Column("govt_id", "VARCHAR(50)", true, false, "doctors(govt_id) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("hospital_name", "VARCHAR(150)", true, false, null, null),
                new DatabaseUtils.Column("hospital_address", "TEXT", true, false, null, null),
                new DatabaseUtils.Column("consultation_fee", "VARCHAR(4)", true, false, null, null)
        );
        DatabaseUtils.createTable("practice_information", columns, null, null, null);
    }

    public static void createTableForAvailabilityOfDoctor() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("id", "SERIAL", true, true, null, null),
                new DatabaseUtils.Column("govt_id", "VARCHAR(50)", true, false, "doctors(govt_id) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("day_of_week", "VARCHAR(10)", true, false, null, DatabaseConnection.isSQLite() ? null : "CHECK (day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'))"),
                new DatabaseUtils.Column("start_time", "TIME", true, false, null, null),
                new DatabaseUtils.Column("end_time", "TIME", true, false, null, null)
        );
        DatabaseUtils.createTable("doctor_availability", columns, null, null, null);
        if (DatabaseConnection.isSQLite()) {
            try {
                DatabaseUtils.validateCheckConstraint("doctor_availability", "day_of_week",
                        Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
            } catch (SQLException e) {
                System.err.println("Error validating day_of_week constraint: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void createTableForSecurityOfDoctor() {
        List<DatabaseUtils.Column> columns = List.of(
                new DatabaseUtils.Column("security_id", "SERIAL", true, true, null, null),
                new DatabaseUtils.Column("govt_id", "VARCHAR(50)", true, false, "doctors(govt_id) ON DELETE CASCADE", null),
                new DatabaseUtils.Column("password", "TEXT", true, false, null, null),
                new DatabaseUtils.Column("user_agreement", DatabaseConnection.isSQLite() ? "INTEGER" : "BOOLEAN", false, false, null, null)
        );
        DatabaseUtils.createTable("doctor_security", columns, null, DatabaseConnection.isSQLite() ? "0" : "FALSE", null);
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

    public static void insertProfessionalDetails(String govtId, String specialization, String degrees, String licenseNumber, String bio) {
        String insert = """
            INSERT INTO professional_details (govt_id, specialization, degrees, medical_license_number, bio)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtId);
            ps.setString(2, specialization);
            ps.setString(3, degrees);
            ps.setString(4, licenseNumber);
            ps.setString(5, bio);
            ps.executeUpdate();
            System.out.println("Professional Details inserted");
        } catch (SQLException e) {
            System.err.println("Error inserting professional details: " + e.getMessage());
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
        if (DatabaseConnection.isSQLite() && !Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").contains(day)) {
            throw new IllegalArgumentException("Invalid day_of_week: " + day);
        }
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
            if (DatabaseConnection.isSQLite()) {
                ps.setInt(3, agreement ? 1 : 0); // SQLite uses INTEGER
            } else {
                ps.setBoolean(3, agreement); // PostgreSQL uses BOOLEAN
            }
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
                Doctor.setFirstName(rs.getString("first_name"));
                Doctor.setLastName(rs.getString("last_name"));
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
                   pd.specialization, pd.degrees, pd.medical_license_number, pd.bio,
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
        Map<String, TimeSlot> availability = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
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
                        availability.put(day, new TimeSlot(startTime, endTime));
                    }
                }
            }
            if (hasData) {
                doctorData.put("availability", availability);
            }
            return doctorData;
        } catch (SQLException e) {
            System.err.println("Error getting doctor details: " + e.getMessage());
            e.printStackTrace();
            return doctorData; // Return empty map on error
        }
    }

    public static void resetPassword(String password, String email) {
        String sql = "UPDATE doctor_security SET password = ? WHERE govt_id IN (SELECT govt_id FROM doctors WHERE email = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> getCompletedAppointmentsForDoctor(String doctorId) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, status, created_at
            FROM appointments
            WHERE doctor_id = ? AND status = 'Completed'
            ORDER BY appointment_date DESC
            """;
        List<Map<String, Object>> appointments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("doctor_id", rs.getString("doctor_id"));
                appointment.put("appointment_number", rs.getInt("appointment_number"));
                appointment.put("patient_phone", rs.getString("patient_phone"));
                appointment.put("appointment_date", rs.getDate("appointment_date"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getDate("created_at"));
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }

    public static List<Map<String, Object>> getUpcomingAppointmentsForDoctor(String doctorId) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, status, created_at
            FROM appointments
            WHERE doctor_id = ? AND status = 'Upcoming'
            ORDER BY appointment_date DESC
            """;
        List<Map<String, Object>> appointments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("doctor_id", rs.getString("doctor_id"));
                appointment.put("appointment_number", rs.getInt("appointment_number"));
                appointment.put("patient_phone", rs.getString("patient_phone"));
                appointment.put("appointment_date", rs.getDate("appointment_date"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getDate("created_at"));
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting upcoming appointments: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }

    public static Map<String, TimeSlot> getDoctorAvailability(String govtId) {
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
        Map<String, TimeSlot> availability = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, govtId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String day = rs.getString("day_of_week");
                Time sqlStartTime = rs.getTime("start_time");
                Time sqlEndTime = rs.getTime("end_time");
                LocalTime startTime = sqlStartTime != null ? sqlStartTime.toLocalTime() : null;
                LocalTime endTime = sqlEndTime != null ? sqlEndTime.toLocalTime() : null;
                if (startTime != null && endTime != null) {
                    availability.put(day, new TimeSlot(startTime, endTime));
                }
            }
            return availability;
        } catch (SQLException e) {
            System.err.println("Error getting doctor availability: " + e.getMessage());
            e.printStackTrace();
            return availability;
        }
    }
}