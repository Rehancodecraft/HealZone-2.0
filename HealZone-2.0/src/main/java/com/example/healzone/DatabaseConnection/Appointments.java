package com.example.healzone.DatabaseConnection;

import com.example.healzone.Patient.UpcomingAppointmentModel;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class Appointments {
    // Updated createTableForAppointments (add patient_age and patient_gender)
    public static void createTableForAppointments() {
        String sql = """
        CREATE TABLE IF NOT EXISTS appointments (
            doctor_id VARCHAR(50) NOT NULL,
            appointment_number INTEGER NOT NULL,
            patient_phone VARCHAR(15) NOT NULL,
            patient_age VARCHAR(10), -- Add this
            patient_gender VARCHAR(10), -- Add this
            appointment_date DATE NOT NULL,
            day_of_week VARCHAR(10) NOT NULL CHECK (day_of_week IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')),
            start_time TIME,
            end_time TIME,
            consultation_fee VARCHAR(4) NOT NULL,
            hospital_name VARCHAR(150) NOT NULL,
            hospital_address TEXT NOT NULL,
            doctor_name VARCHAR(100) NOT NULL,
            patient_name VARCHAR(100),
            speciality VARCHAR(100),
            status VARCHAR(20) CHECK (status IN ('Upcoming', 'Completed', 'Cancelled')),
            created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (doctor_id, appointment_number),
            FOREIGN KEY (doctor_id) REFERENCES doctors(govt_id) ON DELETE CASCADE,
            FOREIGN KEY (patient_phone) REFERENCES patients(phone_number) ON DELETE CASCADE
        )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Appointments table created");
        } catch (SQLException e) {
            System.err.println("Error creating appointments table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Updated getUpcomingAppointmentsForDoctor
    public static List<Map<String, Object>> getUpcomingAppointmentsForDoctor(String doctorId) {
        String query = """
        SELECT a.doctor_id, a.appointment_number, a.patient_phone, a.appointment_date, a.day_of_week,
               a.consultation_fee, a.hospital_name, a.hospital_address, a.doctor_name, a.patient_name,
               a.status, a.created_at, p.age AS patient_age, p.gender AS patient_gender 
        FROM appointments a
        JOIN patients p ON a.patient_phone = p.phone_number
        WHERE a.doctor_id = ? AND a.status = 'Upcoming'
        ORDER BY a.appointment_date DESC
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
                appointment.put("day_of_week", rs.getString("day_of_week"));
                appointment.put("consultation_fee", rs.getString("consultation_fee"));
                appointment.put("hospital_name", rs.getString("hospital_name"));
                appointment.put("hospital_address", rs.getString("hospital_address"));
                appointment.put("doctor_name", rs.getString("doctor_name"));
                appointment.put("patient_name", rs.getString("patient_name"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getTimestamp("created_at"));
                appointment.put("patient_age", rs.getString("patient_age")); // New field
                appointment.put("patient_gender", rs.getString("patient_gender")); // New field
                appointment.put("patient_id", rs.getString("patient_phone")); // New field
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting upcoming appointments for doctor: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }

// Similarly update getAppointmentsForDoctorToday and getAppointmentHistoryForDoctor with the same JOIN

    public static int getAppointmentNo(String doctorGovtId) {
        String countQuery = "SELECT COALESCE(MAX(appointment_number), 0) FROM appointments WHERE doctor_id = ?";
        try (PreparedStatement countStmt = connection.prepareStatement(countQuery)) {
            countStmt.setString(1, doctorGovtId);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1;
        } catch (SQLException e) {
            System.err.println("Error getting appointment number: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static void insertNewAppointment(String doctorGovtId, int appointmentNumber, String patientPhone, String patientName, LocalDate appointmentDate, String status) {
        String dayOfWeek = appointmentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);

        // Fetch practice information
        String practiceQuery = """
            SELECT consultation_fee, hospital_name, hospital_address
            FROM practice_information
            WHERE govt_id = ?
            """;
        String consultationFee = null;
        String hospitalName = null;
        String hospitalAddress = null;
        try (PreparedStatement practiceStmt = connection.prepareStatement(practiceQuery)) {
            practiceStmt.setString(1, doctorGovtId);
            ResultSet rs = practiceStmt.executeQuery();
            if (rs.next()) {
                consultationFee = rs.getString("consultation_fee");
                hospitalName = rs.getString("hospital_name");
                hospitalAddress = rs.getString("hospital_address");
            } else {
                throw new SQLException("No practice information found for doctor ID: " + doctorGovtId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching practice information: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Fetch doctor name
        String doctorQuery = """
            SELECT first_name, last_name
            FROM doctors
            WHERE govt_id = ?
            """;
        String doctorName = null;
        try (PreparedStatement doctorStmt = connection.prepareStatement(doctorQuery)) {
            doctorStmt.setString(1, doctorGovtId);
            ResultSet rs = doctorStmt.executeQuery();
            if (rs.next()) {
                doctorName = rs.getString("first_name") + " " + rs.getString("last_name");
            } else {
                throw new SQLException("No doctor found with ID: " + doctorGovtId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor name: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Fetch doctor availability
        LocalTime startTime = null;
        LocalTime endTime = null;
        try (PreparedStatement timeStmt = connection.prepareStatement("""
            SELECT start_time, end_time FROM doctor_availability
            WHERE govt_id = ? AND day_of_week = ?
            """)) {
            timeStmt.setString(1, doctorGovtId);
            timeStmt.setString(2, dayOfWeek);
            ResultSet rs = timeStmt.executeQuery();
            if (rs.next()) {
                Time sqlStart = rs.getTime("start_time");
                Time sqlEnd = rs.getTime("end_time");
                if (sqlStart != null && sqlEnd != null) {
                    startTime = sqlStart.toLocalTime();
                    endTime = sqlEnd.toLocalTime();
                }
            } else {
                throw new SQLException("No availability found for " + dayOfWeek);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor availability: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Fetch speciality
        String speciality = null;
        String fetchSpecialityQuery = "SELECT specialization FROM professional_details WHERE govt_id = ?";
        try (PreparedStatement fetchStmt = connection.prepareStatement(fetchSpecialityQuery)) {
            fetchStmt.setString(1, doctorGovtId);
            ResultSet rs = fetchStmt.executeQuery();
            if (rs.next()) {
                speciality = rs.getString("specialization");
            } else {
                System.err.println("No specialization found for doctorGovtId: " + doctorGovtId);
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        // Fetch patient age and gender
        String patientAge = null;
        String patientGender = null;
        String patientQuery = """
            SELECT age, gender FROM patients WHERE phone_number = ?
            """;
        try (PreparedStatement patientStmt = connection.prepareStatement(patientQuery)) {
            patientStmt.setString(1, patientPhone);
            ResultSet rs = patientStmt.executeQuery();
            if (rs.next()) {
                patientAge = rs.getString("age");
                patientGender = rs.getString("gender");
            } else {
                throw new SQLException("No patient found with phone number: " + patientPhone);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient details: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Updated insert query to include patient_age and patient_gender
        String insertQuery = """
            INSERT INTO appointments (
                doctor_id, appointment_number, patient_phone, patient_age, patient_gender,
                appointment_date, day_of_week, start_time, end_time, consultation_fee,
                hospital_name, hospital_address, doctor_name, patient_name, speciality, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, doctorGovtId);
            stmt.setInt(2, appointmentNumber);
            stmt.setString(3, patientPhone);
            stmt.setString(4, patientAge); // New field
            stmt.setString(5, patientGender); // New field
            stmt.setDate(6, Date.valueOf(appointmentDate));
            stmt.setString(7, dayOfWeek);
            stmt.setTime(8, Time.valueOf(startTime));
            stmt.setTime(9, Time.valueOf(endTime));
            stmt.setString(10, consultationFee);
            stmt.setString(11, hospitalName);
            stmt.setString(12, hospitalAddress);
            stmt.setString(13, doctorName);
            stmt.setString(14, patientName);
            stmt.setString(15, speciality);
            stmt.setString(16, status);
            stmt.executeUpdate();
            System.out.println("Appointment inserted successfully");
        } catch (SQLException e) {
            System.err.println("Insert Error: " + e.getMessage());
        }
    }

    public static List<UpcomingAppointmentModel> getUpcomingAppointmentsForPatient(String phone) {
        String query = """
            SELECT *
            FROM appointments
            WHERE patient_phone = ? AND status = 'Upcoming'
            ORDER BY appointment_date, start_time
            """;

        List<UpcomingAppointmentModel> appointments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UpcomingAppointmentModel appt = new UpcomingAppointmentModel();
                    appt.setAppointmentNumber(rs.getInt("appointment_number"));
                    appt.setDoctorId(rs.getString("doctor_id"));
                    appt.setDoctorName(rs.getString("doctor_name"));
                    appt.setSpeciality(rs.getString("speciality"));
                    appt.setHospitalName(rs.getString("hospital_name"));
                    appt.setHospitalAddress(rs.getString("hospital_address"));
                    appt.setPatientName(rs.getString("patient_name"));
                    appt.setPatientPhone(rs.getString("patient_phone"));
                    appt.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                    appt.setStartTime(rs.getTime("start_time").toLocalTime());
                    appt.setEndTime(rs.getTime("end_time").toLocalTime());
                    appt.setConsultationFee(rs.getString("consultation_fee"));
                    appointments.add(appt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public static ResultSet getCompletedAppointmentsForPatient(String phoneNumber) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, day_of_week,
                   consultation_fee, hospital_name, hospital_address, doctor_name, patient_name,
                   status, created_at
            FROM appointments
            WHERE patient_phone = ? AND status = 'Completed'
            ORDER BY appointment_date DESC
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments for patient: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<Map<String, Object>> getCompletedAppointmentsForDoctor(String doctorId) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, day_of_week,
                   consultation_fee, hospital_name, hospital_address, doctor_name, patient_name,
                   status, created_at
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
                appointment.put("day_of_week", rs.getString("day_of_week"));
                appointment.put("consultation_fee", rs.getString("consultation_fee"));
                appointment.put("hospital_name", rs.getString("hospital_name"));
                appointment.put("hospital_address", rs.getString("hospital_address"));
                appointment.put("doctor_name", rs.getString("doctor_name"));
                appointment.put("patient_name", rs.getString("patient_name"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getTimestamp("created_at"));
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments for doctor: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }
    public static List<Map<String, Object>> getAppointmentsForDoctorToday(String doctorId, LocalDate date) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, day_of_week,
                   consultation_fee, hospital_name, hospital_address, doctor_name, patient_name,
                   status, created_at
            FROM appointments
            WHERE doctor_id = ? AND appointment_date = ?
            """;
        List<Map<String, Object>> appointments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("doctor_id", rs.getString("doctor_id"));
                appointment.put("appointment_number", rs.getInt("appointment_number"));
                appointment.put("patient_phone", rs.getString("patient_phone"));
                appointment.put("appointment_date", rs.getDate("appointment_date"));
                appointment.put("day_of_week", rs.getString("day_of_week"));
                appointment.put("consultation_fee", rs.getString("consultation_fee"));
                appointment.put("hospital_name", rs.getString("hospital_name"));
                appointment.put("hospital_address", rs.getString("hospital_address"));
                appointment.put("doctor_name", rs.getString("doctor_name"));
                appointment.put("patient_name", rs.getString("patient_name"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getTimestamp("created_at"));
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting today's appointments for doctor: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }

    public static void markAsAttended(String doctorId, int appointmentNumber) {
        String query = """
            UPDATE appointments
            SET status = 'Completed'
            WHERE doctor_id = ? AND appointment_number = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setInt(2, appointmentNumber);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Appointment marked as attended: " + doctorId + ", " + appointmentNumber);
            } else {
                System.err.println("No appointment found to mark as attended: " + doctorId + ", " + appointmentNumber);
            }
        } catch (SQLException e) {
            System.err.println("Error marking appointment as attended: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getAppointmentById(String doctorId, int appointmentNumber) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, day_of_week,
                   consultation_fee, hospital_name, hospital_address, doctor_name, patient_name,
                   status, created_at
            FROM appointments
            WHERE doctor_id = ? AND appointment_number = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setInt(2, appointmentNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("doctor_id", rs.getString("doctor_id"));
                appointment.put("appointment_number", rs.getInt("appointment_number"));
                appointment.put("patient_phone", rs.getString("patient_phone"));
                appointment.put("appointment_date", rs.getDate("appointment_date"));
                appointment.put("day_of_week", rs.getString("day_of_week"));
                appointment.put("consultation_fee", rs.getString("consultation_fee"));
                appointment.put("hospital_name", rs.getString("hospital_name"));
                appointment.put("hospital_address", rs.getString("hospital_address"));
                appointment.put("doctor_name", rs.getString("doctor_name"));
                appointment.put("patient_name", rs.getString("patient_name"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getTimestamp("created_at"));
                return appointment;
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error getting appointment by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static int getTotalBookedAppointments(String doctorId, LocalDate date) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND appointment_date = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting total booked appointments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static int getAttendedAppointments(String doctorId, LocalDate date) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND appointment_date = ? AND status = 'Completed'
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting attended appointments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static int getRemainingAppointments(String doctorId, LocalDate date) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND appointment_date = ? AND status = 'Upcoming'
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting remaining appointments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static List<Map<String, Object>> getAppointmentHistoryForDoctor(String doctorId) {
        String query = """
            SELECT doctor_id, appointment_number, patient_phone, appointment_date, day_of_week,
                   consultation_fee, hospital_name, hospital_address, doctor_name, patient_name,
                   status, created_at
            FROM appointments
            WHERE doctor_id = ?
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
                appointment.put("day_of_week", rs.getString("day_of_week"));
                appointment.put("consultation_fee", rs.getString("consultation_fee"));
                appointment.put("hospital_name", rs.getString("hospital_name"));
                appointment.put("hospital_address", rs.getString("hospital_address"));
                appointment.put("doctor_name", rs.getString("doctor_name"));
                appointment.put("patient_name", rs.getString("patient_name"));
                appointment.put("status", rs.getString("status"));
                appointment.put("created_at", rs.getTimestamp("created_at"));
                appointments.add(appointment);
            }
            return appointments;
        } catch (SQLException e) {
            System.err.println("Error getting appointment history for doctor: " + e.getMessage());
            e.printStackTrace();
            return appointments;
        }
    }
    public static int getTotalCompletedAppointments(String doctorId) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND status = 'Completed'
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting total completed appointments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Get number of completed appointments for a doctor this month
    public static int getCompletedAppointmentsThisMonth(String doctorId) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND status = 'Completed' 
            AND DATE_TRUNC('month', appointment_date) = DATE_TRUNC('month', CURRENT_DATE)
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments this month: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Get number of cancelled appointments for a doctor
    public static int getCancelledAppointments(String doctorId) {
        String query = """
            SELECT COUNT(*) 
            FROM appointments 
            WHERE doctor_id = ? AND status = 'Cancelled'
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting cancelled appointments: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Update appointment status
    public static boolean updateAppointmentStatus(String doctorId, int appointmentNumber, String newStatus) {
        String query = """
            UPDATE appointments
            SET status = ?
            WHERE doctor_id = ? AND appointment_number = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, newStatus);
            ps.setString(2, doctorId);
            ps.setInt(3, appointmentNumber);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}