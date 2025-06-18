package com.example.healzone.model;

import javafx.beans.property.SimpleStringProperty;

public class MedicationPrintModel {
    private final SimpleStringProperty medicineName;
    private final SimpleStringProperty type;
    private final SimpleStringProperty dosage;
    private final SimpleStringProperty frequency;
    private final SimpleStringProperty timing;
    private final SimpleStringProperty duration;
    private final SimpleStringProperty instructions;

    public MedicationPrintModel(String medicineName, String type, String dosage, String frequency, String timing, String duration, String instructions) {
        this.medicineName = new SimpleStringProperty(medicineName);
        this.type = new SimpleStringProperty(type);
        this.dosage = new SimpleStringProperty(dosage);
        this.frequency = new SimpleStringProperty(frequency);
        this.timing = new SimpleStringProperty(timing);
        this.duration = new SimpleStringProperty(duration);
        this.instructions = new SimpleStringProperty(instructions);
    }

    public String getMedicineName() { return medicineName.get(); }
    public String getType() { return type.get(); }
    public String getDosage() { return dosage.get(); }
    public String getFrequency() { return frequency.get(); }
    public String getTiming() { return timing.get(); }
    public String getDuration() { return duration.get(); }
    public String getInstructions() { return instructions.get(); }

//    public SimpleStringProperty medicineNameProperty() { return medicineName; }
//    public SimpleStringProperty typeProperty() { return type; }
//    public SimpleStringProperty dosageProperty() { return dosage; }
//    public SimpleStringProperty frequencyProperty() { return frequency; }
//    public SimpleStringProperty timingProperty() { return timing; }
//    public SimpleStringProperty durationProperty() { return duration; }
//    public SimpleStringProperty instructionsProperty() { return instructions; }
}