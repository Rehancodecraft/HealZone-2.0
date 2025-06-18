package com.example.healzone.model;

public class PatientModel {
         static private String name;
         static private String fatherName;
         static private String age;
         static private String gender;
         static private String password;
         static private String confirmPassword;
         static private String phone;
         static private String email;

    public  static String getName() {
        return name;
    }

    public static String getFatherName() {
        System.out.println("Fathername in patient" + fatherName);
        return fatherName;
    }

    public static String getAge() {
        return age;
    }

    public static String getGender() {
        return gender;
    }

    public static String getPassword() {
        return password;
    }

    public static String getConfirmPassword() {
        return confirmPassword;
    }

    public static String getPhone() {
        return phone;
    }
    public static String getEmail() {
        return email;
    }

    public static void  setName(String newName) {
        name = newName;
    }

    public static void setFatherName(String newFatherName) {
        fatherName = newFatherName;
    }

    public static void setAge(String newAge) {
        age = newAge;
    }

    public static void setGender(String newGender) {
        gender = newGender;
    }

    public static void setPassword(String newPassword) {
        password = newPassword;
    }

    public static void setConfirmPassword(String newConfirmPassword) {
        confirmPassword = newConfirmPassword;
    }

    public static void setPhone(String newPhone) {
        phone = newPhone;
    }
    public static void setEmail(String newEmail) {
        email = newEmail;
    }
}
