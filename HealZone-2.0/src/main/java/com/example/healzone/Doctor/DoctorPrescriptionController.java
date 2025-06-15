package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Prescription;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class DoctorPrescriptionController implements Initializable {

    // Header Components
    @FXML private Label patientNameLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label prescriptionDateLabel;

    // Medicine Form Components
    @FXML private TextField medicineNameField;
    @FXML private ComboBox<String> medicineTypeCombo;
    @FXML private TextField dosageField;
    @FXML private ComboBox<String> frequencyCombo;
    @FXML private ComboBox<String> timingCombo;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> durationUnitCombo;
    @FXML private TextArea instructionsArea;

    // Medicine Table Components
    @FXML private TableView<Medicine> medicinesTable;
    @FXML private TableColumn<Medicine, String> medicineNameColumn;
    @FXML private TableColumn<Medicine, String> typeColumn;
    @FXML private TableColumn<Medicine, String> dosageColumn;
    @FXML private TableColumn<Medicine, String> frequencyColumn;
    @FXML private TableColumn<Medicine, String> timingColumn;
    @FXML private TableColumn<Medicine, String> durationColumn;
    @FXML private TableColumn<Medicine, String> instructionsColumn;
    @FXML private TableColumn<Medicine, Void> actionColumn;

    // Additional Information Components
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea precautionsArea;
    @FXML private TextField followupField;
    @FXML private ComboBox<String> followupUnitCombo;
    @FXML private TextArea notesArea;

    // Button Components
    @FXML private Button saveButton;
    @FXML private Button printButton;
    @FXML private Button addMedicineButton;
    @FXML private Button clearAllButton;
    @FXML private Button cancelButton;
    @FXML private Button previewButton;
    @FXML private Button saveDraftButton;

    // Data Storage
    private ObservableList<Medicine> medicinesList;
    private String currentPatientId;
    private String currentDoctorId;

    // Patient Information
    private String patientName = "John Doe";
    private String patientAge = "35";
    private String patientGender = "Male";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        initializeTable();
        initializeData();
        setupValidation();
        setCurrentDate();
    }

    private void initializeComboBoxes() {
        // Medicine Type ComboBox
        medicineTypeCombo.setItems(FXCollections.observableArrayList(
                "Tablet", "Capsule", "Syrup", "Injection", "Drops", "Cream", "Ointment", "Inhaler", "Powder"
        ));

        // Frequency ComboBox
        frequencyCombo.setItems(FXCollections.observableArrayList(
                "Once daily", "Twice daily", "Three times daily", "Four times daily",
                "Every 6 hours", "Every 8 hours", "Every 12 hours", "As needed", "Weekly", "Monthly"
        ));

        // Timing ComboBox
        timingCombo.setItems(FXCollections.observableArrayList(
                "Before meals", "After meals", "With meals", "On empty stomach",
                "At bedtime", "Morning", "Evening", "Anytime"
        ));

        // Duration Unit ComboBox
        durationUnitCombo.setItems(FXCollections.observableArrayList(
                "Days", "Weeks", "Months", "Years"
        ));

        // Follow-up Unit ComboBox
        followupUnitCombo.setItems(FXCollections.observableArrayList(
                "Days", "Weeks", "Months"
        ));

        // Set default values
        medicineTypeCombo.setValue("Tablet");
        frequencyCombo.setValue("Twice daily");
        timingCombo.setValue("After meals");
        durationUnitCombo.setValue("Days");
        followupUnitCombo.setValue("Weeks");
    }

    private void initializeTable() {
        medicinesList = FXCollections.observableArrayList();
        medicinesTable.setItems(medicinesList);

        // Set up table columns
        medicineNameColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        timingColumn.setCellValueFactory(new PropertyValueFactory<>("timing"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        instructionsColumn.setCellValueFactory(new PropertyValueFactory<>("instructions"));

        // Set up action column with remove buttons
        actionColumn.setCellFactory(param -> new TableCell<Medicine, Void>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.getStyleClass().add("remove-medicine-button");
                FontIcon icon = new FontIcon("fas-trash");
                icon.setIconSize(10);
                icon.setIconColor(javafx.scene.paint.Color.WHITE);
                removeButton.setGraphic(icon);

                removeButton.setOnAction(event -> {
                    Medicine medicine = getTableView().getItems().get(getIndex());
                    removeMedicine(medicine);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });

        // Make table responsive
        medicinesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initializeData() {
        // Set patient information
        patientNameLabel.setText(patientName);
        patientAgeLabel.setText(patientAge);
        patientGenderLabel.setText(patientGender);
    }

    private void setupValidation() {
        // Add listeners for form validation
        medicineNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dosageField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        durationField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        frequencyCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void setCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        prescriptionDateLabel.setText(currentDate.format(formatter));
    }

    private void validateForm() {
        boolean isValid = !medicineNameField.getText().trim().isEmpty() &&
                !dosageField.getText().trim().isEmpty() &&
                !durationField.getText().trim().isEmpty() &&
                frequencyCombo.getValue() != null;

        addMedicineButton.setDisable(!isValid);
    }

    @FXML
    private void addMedicine(ActionEvent event) {
        if (validateMedicineForm()) {
            Medicine medicine = new Medicine(
                    medicineNameField.getText().trim(),
                    medicineTypeCombo.getValue(),
                    dosageField.getText().trim(),
                    frequencyCombo.getValue(),
                    timingCombo.getValue(),
                    durationField.getText().trim() + " " + durationUnitCombo.getValue(),
                    instructionsArea.getText().trim()
            );

            medicinesList.add(medicine);
            clearMedicineForm();
            showSuccessMessage("Medicine added successfully!");
        }
    }

    private boolean validateMedicineForm() {
        StringBuilder errors = new StringBuilder();

        if (medicineNameField.getText().trim().isEmpty()) {
            errors.append("- Medicine name is required\n");
        }
        if (dosageField.getText().trim().isEmpty()) {
            errors.append("- Dosage is required\n");
        }
        if (frequencyCombo.getValue() == null) {
            errors.append("- Frequency is required\n");
        }
        if (durationField.getText().trim().isEmpty()) {
            errors.append("- Duration is required\n");
        }

        if (errors.length() > 0) {
            showErrorMessage("Please fix the following errors:\n" + errors.toString());
            return false;
        }

        try {
            Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            showErrorMessage("Duration must be a valid number");
            return false;
        }

        return true;
    }

    private void clearMedicineForm() {
        medicineNameField.clear();
        medicineTypeCombo.setValue("Tablet");
        dosageField.clear();
        frequencyCombo.setValue("Twice daily");
        timingCombo.setValue("After meals");
        durationField.clear();
        durationUnitCombo.setValue("Days");
        instructionsArea.clear();
    }

    private void removeMedicine(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Medicine");
        alert.setHeaderText("Are you sure you want to remove this medicine?");
        alert.setContentText("Medicine: " + medicine.getMedicineName());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            medicinesList.remove(medicine);
            showSuccessMessage("Medicine removed successfully!");
        }
    }

    @FXML
    private void clearAllMedicines(ActionEvent event) {
        if (medicinesList.isEmpty()) {
            showInfoMessage("No medicines to clear!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear All Medicines");
        alert.setHeaderText("Are you sure you want to clear all medicines?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            medicinesList.clear();
            showSuccessMessage("All medicines cleared!");
        }
    }

    @FXML
    private void savePrescription(ActionEvent event) {
        if (validatePrescription()) {
            try {
                savePrescriptionToDatabase();
                closeWindow(event);
            } catch (Exception e) {
                showErrorMessage("Error saving prescription: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveDraft(ActionEvent event) {
        try {
            savePrescriptionAsDraft();
            showSuccessMessage("Prescription saved as draft!");
        } catch (Exception e) {
            showErrorMessage("Error saving draft: " + e.getMessage());
        }
    }

    @FXML
    private void printPrescription(ActionEvent event) {
        if (validatePrescription()) {
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null && printerJob.showPrintDialog(printButton.getScene().getWindow())) {
                try {
                    Node printNode = createPrintableContent();
                    boolean success = printerJob.printPage(printNode);
                    if (success) {
                        printerJob.endJob();
                        showSuccessMessage("Prescription sent to printer!");
                    } else {
                        showErrorMessage("Failed to print prescription");
                    }
                } catch (Exception e) {
                    showErrorMessage("Error printing prescription: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void previewPrescription(ActionEvent event) {
        if (validatePrescription()) {
            showPrescriptionPreview();
        }
    }

    @FXML
    private void cancelPrescription(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Prescription");
        alert.setHeaderText("Are you sure you want to cancel?");
        alert.setContentText("All unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            closeWindow(event);
        }
    }

    private boolean validatePrescription() {
        StringBuilder errors = new StringBuilder();

        if (medicinesList.isEmpty()) {
            errors.append("- At least one medicine must be prescribed\n");
        }
        if (diagnosisArea.getText().trim().isEmpty()) {
            errors.append("- Diagnosis is required\n");
        }

        if (errors.length() > 0) {
            showErrorMessage("Please fix the following errors:\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void savePrescriptionToDatabase() {
        Prescription prescription = new Prescription();
        prescription.setDoctorId(currentDoctorId);
        prescription.setPatientId(currentPatientId);
        prescription.setPatientName(patientName);
        prescription.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        prescription.setDiagnosis(diagnosisArea.getText().trim());
        prescription.setPrecautions(precautionsArea.getText().trim());
        prescription.setFollowup(followupField.getText().trim() + " " + (followupUnitCombo.getValue() != null ? followupUnitCombo.getValue() : ""));
        prescription.setNotes(notesArea.getText().trim());

        // Convert medicines to a string for storage
        StringBuilder medicinesString = new StringBuilder();
        for (Medicine medicine : medicinesList) {
            medicinesString.append(String.format("%s (%s) - %s, %s, %s, %s, %s%n",
                    medicine.getMedicineName(), medicine.getType(), medicine.getDosage(),
                    medicine.getFrequency(), medicine.getTiming(), medicine.getDuration(),
                    medicine.getInstructions()));
        }
        prescription.setMedicines(medicinesString.toString());

        try {
            prescription.save();
            showSuccessMessage("Prescription saved successfully!");
        } catch (Exception e) {
            showErrorMessage("Error saving prescription: " + e.getMessage());
        }
    }

    private void savePrescriptionAsDraft() {
        Prescription prescription = new Prescription();
        prescription.setDoctorId(currentDoctorId);
        prescription.setPatientId(currentPatientId);
        prescription.setPatientName(patientName);
        prescription.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        prescription.setDiagnosis(diagnosisArea.getText().trim());
        prescription.setPrecautions(precautionsArea.getText().trim());
        prescription.setFollowup(followupField.getText().trim() + " " + (followupUnitCombo.getValue() != null ? followupUnitCombo.getValue() : ""));
        prescription.setNotes(notesArea.getText().trim());

        // Convert medicines to a string for storage
        StringBuilder medicinesString = new StringBuilder();
        for (Medicine medicine : medicinesList) {
            medicinesString.append(String.format("%s (%s) - %s, %s, %s, %s, %s%n",
                    medicine.getMedicineName(), medicine.getType(), medicine.getDosage(),
                    medicine.getFrequency(), medicine.getTiming(), medicine.getDuration(),
                    medicine.getInstructions()));
        }
        prescription.setMedicines(medicinesString.toString());

        try {
            prescription.save(); // Mark as draft if needed, adjust database logic
            showSuccessMessage("Prescription saved as draft!");
        } catch (Exception e) {
            showErrorMessage("Error saving draft: " + e.getMessage());
        }
    }

    private Node createPrintableContent() {
        return medicinesTable.getParent();
    }

    private void showPrescriptionPreview() {
        StringBuilder preview = new StringBuilder();
        preview.append("PRESCRIPTION PREVIEW\n");
        preview.append("===================\n\n");
        preview.append("Patient: ").append(patientName).append("\n");
        preview.append("Age: ").append(patientAge).append("\n");
        preview.append("Gender: ").append(patientGender).append("\n");
        preview.append("Date: ").append(prescriptionDateLabel.getText()).append("\n\n");

        preview.append("MEDICINES:\n");
        for (Medicine medicine : medicinesList) {
            preview.append("- ").append(medicine.getMedicineName())
                    .append(" (").append(medicine.getType()).append(") - ")
                    .append(medicine.getDosage()).append(", ")
                    .append(medicine.getFrequency()).append(", ")
                    .append(medicine.getTiming()).append(", ")
                    .append(medicine.getDuration()).append("\n");
            if (!medicine.getInstructions().isEmpty()) {
                preview.append("  Instructions: ").append(medicine.getInstructions()).append("\n");
            }
        }

        preview.append("\nDIAGNOSIS:\n").append(diagnosisArea.getText()).append("\n");
        preview.append("\nPRECAUTIONS:\n").append(precautionsArea.getText()).append("\n");

        if (!followupField.getText().trim().isEmpty()) {
            preview.append("\nFOLLOW-UP: ").append(followupField.getText())
                    .append(" ").append(followupUnitCombo.getValue()).append("\n");
        }

        if (!notesArea.getText().trim().isEmpty()) {
            preview.append("\nNOTES:\n").append(notesArea.getText()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prescription Preview");
        alert.setHeaderText("Prescription Details");

        TextArea textArea = new TextArea(preview.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(50);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPatientInfo(String name, String age, String gender) {
        this.patientName = name;
        this.patientAge = age;
        this.patientGender = gender;

        if (patientNameLabel != null) {
            patientNameLabel.setText(name);
            patientAgeLabel.setText(age);
            patientGenderLabel.setText(gender);
        }
    }

    public void setPatientId(String patientId) {
        this.currentPatientId = patientId;
    }

    public void setDoctorId(String doctorId) {
        this.currentDoctorId = doctorId;
    }

    public static class Medicine {
        private final SimpleStringProperty medicineName;
        private final SimpleStringProperty type;
        private final SimpleStringProperty dosage;
        private final SimpleStringProperty frequency;
        private final SimpleStringProperty timing;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty instructions;

        public Medicine(String medicineName, String type, String dosage,
                        String frequency, String timing, String duration, String instructions) {
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

        public SimpleStringProperty medicineNameProperty() { return medicineName; }
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty dosageProperty() { return dosage; }
        public SimpleStringProperty frequencyProperty() { return frequency; }
        public SimpleStringProperty timingProperty() { return timing; }
        public SimpleStringProperty durationProperty() { return duration; }
        public SimpleStringProperty instructionsProperty() { return instructions; }
    }
}