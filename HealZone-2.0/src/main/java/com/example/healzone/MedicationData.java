package com.example.healzone;

public class MedicationData {
    private String medicineName;
    private String type;          // e.g., Tablet, Capsule, Syrup, etc.
    private String dosage;        // e.g., 500mg, 10ml, etc.
    private String frequency;     // e.g., Twice daily, Three times daily, etc.
    private String timing;        // e.g., Before meals, After meals, etc.
    private String duration;      // e.g., 7 days, 2 weeks, etc.
    private String instructions;  // Additional instructions

    // Constructors
    public MedicationData() {}

    public MedicationData(String medicineName, String type, String dosage,
                          String frequency, String timing, String duration) {
        this.medicineName = medicineName;
        this.type = type;
        this.dosage = dosage;
        this.frequency = frequency;
        this.timing = timing;
        this.duration = duration;
    }

    public MedicationData(String medicineName, String type, String dosage,
                          String frequency, String timing, String duration, String instructions) {
        this(medicineName, type, dosage, frequency, timing, duration);
        this.instructions = instructions;
    }

    // Getters and Setters
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s, %s, %s, %s",
                medicineName, type, dosage, frequency, timing, duration);
    }
}