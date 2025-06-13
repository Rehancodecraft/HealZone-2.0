package com.example.healzone;

import java.time.LocalDate;

public class Appointment {
        private String id;
        private String doctorId;
        private String patientPhone;
        private int appointmentNumber;
        private LocalDate date;
        private boolean attended;
        // Getters and setters
        public String getId() { return id; }
        public String getDoctorId() { return doctorId; }
        public String getPatientPhone() { return patientPhone; }
        public int getAppointmentNumber() { return appointmentNumber; }
        public LocalDate getDate() { return date; }
        public boolean isAttended() { return attended; }
}
