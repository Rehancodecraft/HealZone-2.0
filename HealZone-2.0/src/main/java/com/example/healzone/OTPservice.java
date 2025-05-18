package com.example.healzone;

public class OTPservice {
    public static String generateOTP() {
        int otp = 100000 + new java.util.Random().nextInt(900000);
        return String.valueOf(otp);
    }
}
