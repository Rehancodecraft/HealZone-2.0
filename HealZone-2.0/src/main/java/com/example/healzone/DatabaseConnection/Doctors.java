package com.example.healzone.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Doctors {
    public static void createTableForPersonalDetailsOfDoctors(){
        try {
            String doctors = """
                        
                    CREATE TABLE IF NOT EXISTS doctors (
                        govt_id VARCHAR(50) PRIMARY KEY,
                        first_name VARCHAR(50) NOT NULL,
                        last_name VARCHAR(50) NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        phone_number VARCHAR(20) UNIQUE NOT NULL
                    );
                    
                   
                        """;
            PreparedStatement preparedStatement = connection.prepareStatement(doctors);
            preparedStatement.execute();
//            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void createTableForProfessionalDetailsOfDoctor(){
        try {
            String professionalDetails = """
                    CREATE TABLE IF NOT EXISTS professional_details (
                        professional_id SERIAL PRIMARY KEY,
                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                        specialization VARCHAR(100) NOT NULL,
                        degrees TEXT NOT NULL,
                        medical_license_number VARCHAR(100) UNIQUE NOT NULL,
                        bio TEXT
                    );
                    
                   
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(professionalDetails);
            preparedStatement.execute();
//            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void createTableForPracticeInformationOfDoctor(){
        try{
            String practiceInformation = """
                    CREATE TABLE IF NOT EXISTS practice_information (
                        practice_id SERIAL PRIMARY KEY,
                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                        hospital_name VARCHAR(150) NOT NULL,
                        hospital_address TEXT NOT NULL,
                        consultation_fee VARCHAR(4) NOT NULL
                    );
                    
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(practiceInformation);
            preparedStatement.execute();
//            connection.commit();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void createTableForAvailabilityOfDoctor(){
        try {
            String doctorAvailability = """
                        
                    CREATE TABLE IF NOT EXISTS doctor_availability (
                        id SERIAL PRIMARY KEY,
                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                        day_of_week VARCHAR(10) CHECK (
                            day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
                        ),
                        start_time TIME NOT NULL,
                        end_time TIME NOT NULL,
                        UNIQUE(govt_id, day_of_week)
                    );
                    
   
                        """;
        PreparedStatement preparedStatement = connection.prepareStatement(
            doctorAvailability);
            preparedStatement.execute();
//        connection.commit();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void createTableForSecurityOfDoctor(){
        try {
            String security = """
                    CREATE TABLE IF NOT EXISTS doctor_security (
                        security_id SERIAL PRIMARY KEY,
                        govt_id VARCHAR(50) REFERENCES doctors(govt_id) ON DELETE CASCADE,
                        password TEXT NOT NULL,
                        user_agreement BOOLEAN DEFAULT FALSE
                    );
                    
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(security);
            preparedStatement.execute();
//            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void insertDoctorPersonalDetails(String govtID, String firstName, String lastName, String email, String phone) {
        String insert = """
        INSERT INTO doctors (govt_id, first_name, last_name, email, phone_number)
        VALUES (?, ?, ?, ?, ?);
    """;

        try {
            PreparedStatement ps = connection.prepareStatement(insert);
            ps.setString(1, govtID);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, email);
            ps.setString(5, phone);

            ps.executeQuery();
            System.out.println("Personal Details inserted");
//            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void insertProfessionalDetails(String govtId, String specialization, String degrees, String licenseNumber, String bio) {
        String insert = """
        INSERT INTO professional_details (govt_id, specialization, degrees, medical_license_number, bio)
        VALUES (?, ?, ?, ?, ?);
    """;
        try {
            PreparedStatement ps = connection.prepareStatement(insert);
            ps.setString(1, govtId);
            ps.setString(2, specialization);
            ps.setString(3, degrees);
            ps.setString(4, licenseNumber);
            ps.setString(5, bio);
            ps.executeUpdate();
            System.out.println("Professional Details inserted");
//            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertPracticeInfo(String govtId, String hospitalName, String hospitalAddress, String fee) {
        String insert = """
        INSERT INTO practice_information (govt_id, hospital_name, hospital_address, consultation_fee)
        VALUES (?, ?, ?, ?);
    """;
        try {
            PreparedStatement ps = connection.prepareStatement(insert);
            ps.setString(1, govtId);
            ps.setString(2, hospitalName);
            ps.setString(3, hospitalAddress);
            ps.setString(4, fee);
            ps.executeUpdate();
            System.out.println("Practice infor inserted");
//            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void insertAvailabilityOfDoctor(String govtId, String day, LocalTime start, LocalTime end){
        try {
            String sql = "INSERT INTO doctor_availability (govt_id, day_of_week, start_time, end_time) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, govtId);
            stmt.setString(2, day);
            stmt.setTime(3, Time.valueOf(start));
            stmt.setTime(4, Time.valueOf(end));
            stmt.execute();
            System.out.println("availability inserted");
//            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void insertSecurityDetails(String govtId, String password, boolean agreement) {
        String insert = """
        INSERT INTO doctor_security (govt_id, password, user_agreement)
        VALUES (?, ?, ?);
    """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, govtId);
            ps.setString(2, password);
            ps.setBoolean(3, agreement);
            ps.executeUpdate();
            System.out.println("security details inserted");
//            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkPhoneNumber(String phoneNumber){
        try{
            String sql = "select phone_number from doctors where phone_number = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if(rs.next() ){
                return false;
            }else {
                return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkEmail(String email){
        try{
            String sql = "select email from doctors where email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkGovtID(String govtID){
        try{
            String sql = "select govt_id from doctors where govt_id = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,govtID);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return true;
            }else{
                return false;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static boolean checkGovtID(String govtID,String email){
        try{
            String sql = "select govt_id from doctors where govt_id = ? AND email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,govtID);
            stmt.setString(2,email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    public static ResultSet getCurrentDoctorDetails(String email, String password){
        try {
            String sql = """
            SELECT d.*, pd.*, pi.*, da.*
            FROM doctors d
            JOIN professional_details pd ON d.govt_id = pd.govt_id
            JOIN practice_information pi ON d.govt_id = pi.govt_id
            LEFT JOIN doctor_availability da ON d.govt_id = da.govt_id
            JOIN doctor_security sd ON d.govt_id = sd.govt_id
            WHERE d.email = ? AND sd.password = ?;
        """;

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void resetPassword(String password,String email){
        try{
            String sql= "UPDATE patients SET password = ? WHERE email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,password);
            stmt.setString(2,email);
            stmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static ResultSet getCompletedAppointmentsForDoctor(String doctorId) {
        String query = """
        SELECT * FROM appointments
        WHERE doctor_id = ? AND status = 'Completed'
        ORDER BY appointment_date DESC;
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static ResultSet getUpcomingAppointmentsForDoctor(String doctorId) {
        String query = """
        SELECT * FROM appointments
        WHERE doctor_id = ? AND status = 'Upcoming'
        ORDER BY appointment_date DESC;
    """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }



}
