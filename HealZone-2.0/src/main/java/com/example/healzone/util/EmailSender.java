package com.example.healzone.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.scene.control.Alert;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static com.example.healzone.ShowAlert.ShowAlert.showAlert;


public class EmailSender {

    public static String receiverEmail;
    private final String fromEmail = "healzoon@gmail.com";
    private final String password = "gzvz kvej hcps lonp"; // App Password

    public boolean sendOtp(String toEmail, String otp, String name) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "HealZone"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Verify Your HealZone Account with Your OTP");

            // Create multipart for HTML, plain text, and related content (e.g., images)
            // Create multipart for plain text and HTML
            Multipart multipart = new MimeMultipart("alternative");

// Plain text part
            BodyPart textPart = new MimeBodyPart();
            String textContent = """
        Subject: Verify Your HealZone Account with Your OTP
        
        Dear %s,
        
        Welcome to HealZone!
        
        To complete your account verification, please use the following One-Time Password (OTP):
        
        OTP: %s
        
        This OTP is confidential and will expire in 1 minute. For your security, please do not share it with anyone.
        
        At HealZone, we’re here to simplify your healthcare by connecting you with trusted doctors for seamless appointment booking. Get started today and take control of your health!
        
        If you didn’t request this OTP, please contact our support team at healzoon@gmail.com.
        
        Thank you for choosing HealZone!
        
        Warm regards,
        The HealZone Team
        healzoon@gmail.com | www.healzone.com
        """.formatted(name, otp);
            textPart.setText(textContent);
            multipart.addBodyPart(textPart);

// HTML part
            BodyPart htmlPart = new MimeBodyPart();
            String htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>HealZone OTP Verification</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: Arial, Helvetica, sans-serif; background-color: #f4f4f4;">
            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f4f4f4; padding: 20px 0;">
                <tr>
                    <td align="center">
                        <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1);">
                            <!-- Header -->
                            <tr>
                                <td style="background-color: #3b82f6; padding: 20px; text-align: center;">
                                    <h1 style="font-size: 26px; font-weight: bold; margin: 0; color: #ffffff;">Welcome to HealZone, %s!</h1>
                                </td>
                            </tr>
                            <!-- Body -->
                            <tr>
                                <td style="padding: 40px 30px; color: #333333;">
                                    <p style="font-size: 16px; line-height: 24px; margin: 0 0 20px;">
                                        You're one step away from accessing seamless doctor appointment booking with HealZone. Please use the One-Time Password (OTP) below to verify your account:
                                    </p>
                                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f1f5f9; border: 2px solid #3b82f6; border-radius: 6px; padding: 20px; text-align: center; margin: 20px 0;">
                                        <tr>
                                            <td>
                                                <p style="font-size: 18px; font-weight: bold; margin: 0 0 10px; color: #1f2937;">Your Verification OTP</p>
                                                <p style="font-size: 28px; font-weight: bold; color: #3b82f6; margin: 0; letter-spacing: 3px; background-color: #ffffff; padding: 10px; border-radius: 4px;">%s</p>
                                            </td>
                                        </tr>
                                    </table>
                                    <p style="font-size: 14px; line-height: 20px; color: #6b7280; margin: 0 0 20px;">
                                        This OTP is confidential and will expire in <strong>10 minutes</strong>. For your security, please do not share it with anyone.
                                    </p>
                                    <p style="font-size: 16px; line-height: 24px; margin: 0 0 20px;">
                                        At HealZone, we’re here to simplify your healthcare by connecting you with trusted doctors for seamless appointment booking. Enter your OTP in the HealZone app to get started and take control of your health!
                                    </p>
                                    <p style="font-size: 14px; line-height: 20px; color: #6b7280; margin: 0;">
                                        If you didn’t request this OTP, please contact our support team at <a href="mailto:healzoon@gmail.com" style="color: #3b82f6; text-decoration: none;">healzoon@gmail.com</a>.
                                    </p>
                                </td>
                            </tr>
                            <!-- Footer -->
                            <tr>
                                <td style="background-color: #f1f5f9; padding: 20px 30px; text-align: center; font-size: 14px; color: #6b7280;">
                                    <p style="margin: 0 0 10px;">Thank you for choosing HealZone!</p>
                                    <p style="margin: 0 0 10px;">
                                        <a href="https://rehancodecraft.github.io/" style="color: #3b82f6; text-decoration: none;">www.healzone.com</a> | 
                                        <a href="mailto:healzoon@gmail.com" style="color: #3b82f6; text-decoration: none;">healzoon@gmail.com</a>
                                    </p>
                                    <p style="margin: 0;">© 2025 HealZone. All rights reserved.</p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(name, otp);
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

// Set multipart content
            message.setContent(multipart);

            // Send email
            Transport.send(message);
            System.out.println("OTP email sent to " + toEmail);
            return true;
        } catch (SendFailedException e) {
        showAlert(Alert.AlertType.ERROR, "Invalid Email", "The email address is not valid: " + toEmail);
        return false;

    } catch (MessagingException e) {
        if (e.getCause() != null && e.getCause().toString().contains("UnknownHostException")) {
            showAlert(Alert.AlertType.ERROR, "Network Error", "No internet connection. Please check your network.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Email Error", "Failed to send OTP. Please try again.");
        }
        e.printStackTrace();
        return false;

    } catch (UnsupportedEncodingException e) {
        showAlert(Alert.AlertType.ERROR, "Encoding Error", "Unsupported encoding while sending email.");
        e.printStackTrace();
        return false;
    }
    }
    public static void sendOTPToEmail(String email, String name) {
        String generatedOTP = OTPGenerator.generateOTP();
        EmailSender emailSender = new EmailSender();
        boolean success = emailSender.sendOtp(email, generatedOTP, name);

        if (success) {
            System.out.println("OTP sent successfully.");
        } else {
            System.out.println("Failed to send OTP.");
        }
    }
}