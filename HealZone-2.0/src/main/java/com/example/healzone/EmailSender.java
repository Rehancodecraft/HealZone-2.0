package com.example.healzone;

import java.net.Authenticator;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailSender {

    private final String fromEmail = "healzoneappointmentbooking@gmail.com"; // Replace with your email
    private final String password = "snfx khvv gjmc fyxy  ";    // Use App Password, not Gmail password

    public boolean sendOtp(String toEmail, String otp,String name) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("HealZone OTP Code");
            String content = "Hello " + name + ",\n\n"
                    + "Welcome to HealZone!\n\n"
                    + "You're just one step away from accessing your personalized health journey.\n"
                    + "Please use the following One-Time Password (OTP) to verify your account:\n\n"
                    + "OTP: " + otp + "\n\n"
                    + "This OTP is confidential and will expire shortly. Please do not share it with anyone.\n\n"
                    + "HealZone is your digital health companion — offering smart symptom checks, health tips, "
                    + "and personalized care recommendations.\n\n"
                    + "Thank you for choosing HealZone!\n\n"
                    + "Best regards,\n"
                    + "HealZone Team\n";

            message.setText(content);

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
