package com.example.healzone;

import java.sql.*;

public class DatabaseConnection {
    public static Connection connection;
    public static void connectToDatabase() {
        try {

            String url = "jdbc:postgresql://localhost:5432/HealZone"; // replace with your DB name
            String user = "postgres";
            String password = "Rehan*17112006";

            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            initializeDatabase();
            System.out.println("Database connected and initialized");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error in database Connection");
            e.printStackTrace();
        }
    }
    public static void initializeDatabase(){
        try {
            String patients = """
            CREATE TABLE IF NOT EXISTS patients (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) ,
                father_name VARCHAR(100),
                phone_number VARCHAR(15) UNIQUE,
                email VARCHAR(100) UNIQUE NOT NULL,
                age VARCHAR(3),
                gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
                password TEXT
            );
        """;
            PreparedStatement preparedStatement = connection.prepareStatement(patients);
            preparedStatement.execute();
            System.out.println("created table patients");
            String doctors = """
        CREATE TABLE IF NOT EXISTS doctors (
    id SERIAL PRIMARY KEY,
    doctor_name VARCHAR(100),
    email VARCHAR(100) UNIQUE NOT NULL,
    hospital_name VARCHAR(150) NOT NULL,
    address TEXT,
    city VARCHAR(50),
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    fee NUMERIC(8, 2) CHECK (fee >= 0),
    specialization VARCHAR(100),
    degrees TEXT,
    experience_years INT CHECK (experience_years >= 0),
    availability_days VARCHAR(100),
    rating NUMERIC(2, 1) CHECK (rating >= 0 AND rating <= 5)
);""";
        preparedStatement = connection.prepareStatement(doctors);
        preparedStatement.execute();
        connection.commit();

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void registerPatient(String name, String fatherName, String phoneNumber,String email, String age, String gender, String password){
        try {
            String register = "INSERT INTO patients (name, father_name, phone_number,email, age, gender, password)\n" +
                    "VALUES (?, ?, ?, ?,?, ?,?);\n";
            PreparedStatement preparedStatement = connection.prepareStatement(register);
            preparedStatement.setString(1,name);
            preparedStatement.setString(2,fatherName);
            preparedStatement.setString(3,phoneNumber);
            preparedStatement.setString(4,email);
            preparedStatement.setString(5,age);
            preparedStatement.setString(6,gender);
            preparedStatement.setString(7,password);
            preparedStatement.execute();
            connection.commit();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static boolean checkPhoneNumber(String phoneNumber){
        try{
            String sql = "select phone_number from patients where phone_number = ?;";
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
            String sql = "select email from patients where email = ?;";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,email);
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
    public static ResultSet getCurrentPatientDetails(String email, String password){
     try{
         String sql = "SELECT * FROM patients WHERE email = ? AND password = ?;";

         PreparedStatement stmt = connection.prepareStatement(sql);
         stmt.setString(1,email);
         stmt.setString(2,password);
         return stmt.executeQuery();
     }catch (SQLException e){
         e.printStackTrace();
         return null;
     }
    }
}
