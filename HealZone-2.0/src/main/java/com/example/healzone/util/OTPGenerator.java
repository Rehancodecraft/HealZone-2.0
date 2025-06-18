package com.example.healzone.util;

import java.util.Timer;
import java.util.TimerTask;

public class OTPGenerator {
    private static String otp;
    private static long generatedTime;
    private static boolean isOTPExpired = true;
    public static boolean isCooldown = false;
    public  static String generateOTP() {
        if (isCooldown) {
            System.out.println("Please wait, you can request OTP again after 1 minute.");
            return null;
        }else {
            otp = String.valueOf(100000 + new java.util.Random().nextInt(900000));
            generatedTime = System.currentTimeMillis();
            isCooldown = true;
            isOTPExpired = false;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isOTPExpired = true;
                    isCooldown = false;
                    System.out.println("OTP expired. You can request a new one.");
                }
            }, 60_000);
            return otp;
        }
        // 60 seconds
    }
    public static boolean validateOTP(String userInput) {
        System.out.println(otp);
        if (isOTPExpired) {
            System.out.println("OTP is expired or not generated.");
            return false;
        }
        if (otp.equals(userInput)) {
            System.out.println("OTP verified successfully!");
            isOTPExpired = true; // Optional: mark as used
            return true;
        } else {
            System.out.println("Incorrect OTP.");
            return false;
        }
    }
}
