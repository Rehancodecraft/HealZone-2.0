package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.DatabaseConnection.Prescription;
import com.example.healzone.MedicationData;
import com.example.healzone.PrescriptionData; // Import the top-level PrescriptionData
import com.example.healzone.PrescriptionViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class DoctorPrescriptionController implements Initializable {

    // Header Components
    @FXML private Label patientNameLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label prescriptionDateLabel;
    @FXML private Button exitButton;

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

    // Data Storage
    private ObservableList<Medicine> medicinesList;
    private String currentPatientId; // Represents patient_phone
    private String currentDoctorId;

    // Patient Information
    private String patientName = "";
    private String patientAge = "";
    private String patientGender = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        initializeTable();
        setupValidation();
        setCurrentDate();
        // Ensure labels are updated after initialization if patientId is already set
        if (currentPatientId != null) {
            setPatientInfo(null, null, null);
        }
    }

    private void initializeComboBoxes() {
        medicineTypeCombo.setItems(FXCollections.observableArrayList(
                "Tablet", "Capsule", "Syrup", "Injection", "Drops", "Cream", "Ointment", "Inhaler", "Powder"
        ));
        frequencyCombo.setItems(FXCollections.observableArrayList(
                "Once daily", "Twice daily", "Three times daily", "Four times daily",
                "Every 6 hours", "Every 8 hours", "Every 12 hours", "As needed", "Weekly", "Monthly"
        ));
        timingCombo.setItems(FXCollections.observableArrayList(
                "Before meals", "After meals", "With meals", "On empty stomach",
                "At bedtime", "Morning", "Evening", "Anytime"
        ));
        durationUnitCombo.setItems(FXCollections.observableArrayList(
                "Days", "Weeks", "Months", "Years"
        ));
        followupUnitCombo.setItems(FXCollections.observableArrayList(
                "Days", "Weeks", "Months"
        ));
        medicineTypeCombo.setValue("Tablet");
        frequencyCombo.setValue("Twice daily");
        timingCombo.setValue("After meals");
        durationUnitCombo.setValue("Days");
        followupUnitCombo.setValue("Weeks");
    }

    private void initializeTable() {
        medicinesList = FXCollections.observableArrayList();
        medicinesTable.setItems(medicinesList);
        medicineNameColumn.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dosageColumn.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        timingColumn.setCellValueFactory(new PropertyValueFactory<>("timing"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        instructionsColumn.setCellValueFactory(new PropertyValueFactory<>("instructions"));

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
        medicinesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupValidation() {
        medicineNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        dosageField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        durationField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        frequencyCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void setCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        prescriptionDateLabel.setText(currentDate.format(formatter)); // Will show 15/06/2025
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
        }
    }

    private boolean validateMedicineForm() {
        StringBuilder errors = new StringBuilder();
        if (medicineNameField.getText().trim().isEmpty()) errors.append("- Medicine name is required\n");
        if (dosageField.getText().trim().isEmpty()) errors.append("- Dosage is required\n");
        if (frequencyCombo.getValue() == null) errors.append("- Frequency is required\n");
        if (durationField.getText().trim().isEmpty()) errors.append("- Duration is required\n");
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
            try {
                // Load the FXML for PrescriptionViewController
                URL fxmlUrl = getClass().getResource("/com/example/healzone/PrescriptionView.fxml");
                if (fxmlUrl == null) {
                    showErrorMessage("Error loading preview: FXML file not found at /com/example/healzone/PrescriptionView.fxml");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();

                // Get the controller
                PrescriptionViewController previewController = loader.getController();

                // Fetch doctor information
                DoctorInfo doctorInfo = fetchDoctorInfo(currentDoctorId);
                if (doctorInfo == null) {
                    showErrorMessage("Failed to load doctor information.");
                    return;
                }

                // Prepare prescription data from the current form
                PrescriptionData prescriptionData = new PrescriptionData();
                prescriptionData.setPatientName(patientName);
                prescriptionData.setPatientAge(patientAge != null && !patientAge.isEmpty() ? Integer.parseInt(patientAge) : 0);
                prescriptionData.setPatientGender(patientGender);
                prescriptionData.setPatientId(currentPatientId);
                prescriptionData.setDoctorName(doctorInfo.getFullName());
                prescriptionData.setDoctorSpecialization(doctorInfo.getSpecialization());
                prescriptionData.setLicenseNumber(doctorInfo.getLicenseNumber());
                prescriptionData.setRegistrationNumber(doctorInfo.getRegistrationNumber());
                prescriptionData.setDiagnosis(diagnosisArea.getText().trim());
                prescriptionData.setPrecautions(precautionsArea.getText().trim());
                prescriptionData.setFollowup(followupField.getText().trim() + " " + (followupUnitCombo.getValue() != null ? followupUnitCombo.getValue() : ""));
                prescriptionData.setNotes(notesArea.getText().trim());

                // Convert medicines to MedicationData list
                ObservableList<MedicationData> medicationDataList = FXCollections.observableArrayList();
                for (Medicine medicine : medicinesList) {
                    MedicationData medData = new MedicationData(
                            medicine.getMedicineName(),
                            medicine.getType(),
                            medicine.getDosage(),
                            medicine.getFrequency(),
                            medicine.getTiming(),
                            medicine.getDuration(),
                            medicine.getInstructions()
                    );
                    medicationDataList.add(medData);
                }
                prescriptionData.setMedications(medicationDataList);

                // Load data into the preview controller
                previewController.loadPrescription(prescriptionData);
                previewController.setDoctorInfo(
                        prescriptionData.getDoctorName(),
                        prescriptionData.getDoctorSpecialization(),
                        prescriptionData.getLicenseNumber(),
                        prescriptionData.getRegistrationNumber()
                );
                previewController.populateView();

                // Create a new stage for preview
                Stage previewStage = new Stage();
                previewStage.setTitle("Prescription Preview");
                Scene scene = new Scene(root);
                previewStage.setScene(scene);
                previewStage.initModality(Modality.APPLICATION_MODAL);
                previewStage.initOwner(((Node) event.getSource()).getScene().getWindow());

                // Set minimum height to ensure all content is accessible
                previewStage.setMinHeight(600.0);
                previewStage.sizeToScene();

                previewStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace(); // Debug full stack trace
                showErrorMessage("Error loading preview: " + e.getMessage());
            } catch (NumberFormatException e) {
                showErrorMessage("Invalid patient age format: " + e.getMessage());
            }
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

    @FXML
    private void exitPrescription(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Prescription");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("All unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            closeWindow(event);
        }
    }

    private boolean validatePrescription() {
        StringBuilder errors = new StringBuilder();
        if (medicinesList.isEmpty()) errors.append("- At least one medicine must be prescribed\n");
        if (diagnosisArea.getText().trim().isEmpty()) errors.append("- Diagnosis is required\n");
        if (currentPatientId == null) errors.append("- Patient ID is not set\n");
        if (currentDoctorId == null) errors.append("- Doctor ID is not set\n");
        if (errors.length() > 0) {
            showErrorMessage("Please fix the following errors:\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void savePrescriptionToDatabase() {
        Prescription prescription = new Prescription();
        prescription.setDoctorId(currentDoctorId);
        prescription.setPatientId(currentPatientId); // patient_phone
        prescription.setPatientName(patientName);
        prescription.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        prescription.setDiagnosis(diagnosisArea.getText().trim());
        prescription.setPrecautions(precautionsArea.getText().trim());
        prescription.setFollowup(followupField.getText().trim() + " " + (followupUnitCombo.getValue() != null ? followupUnitCombo.getValue() : ""));
        prescription.setNotes(notesArea.getText().trim());
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

    private Node createPrintableContent() {
        return medicinesTable.getParent();
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
        this.patientName = name != null ? name : fetchPatientNameFromAppointments();
        this.patientAge = age != null ? age : fetchPatientAgeFromAppointments();
        this.patientGender = gender != null ? gender : fetchPatientGenderFromAppointments();
        updateLabels();
        System.out.println("Updated patient info - Name: " + patientName + ", Age: " + patientAge + ", Gender: " + patientGender);
    }

    private String fetchPatientNameFromAppointments() {
        if (currentPatientId == null) {
            System.out.println("currentPatientId is null");
            return "Unknown";
        }
        System.out.println("Fetching patient name for patient_phone: " + currentPatientId);
        String query = "SELECT patient_name FROM appointments WHERE patient_phone = ? ORDER BY appointment_date DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("patient_name");
                System.out.println("Fetched patient_name: " + name);
                return name != null ? name : "Unknown";
            } else {
                System.out.println("No record found for patient_phone: " + currentPatientId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient name: " + e.getMessage());
        }
        return "Unknown";
    }

    private String fetchPatientAgeFromAppointments() {
        if (currentPatientId == null) {
            System.out.println("currentPatientId is null");
            return "N/A";
        }
        System.out.println("Fetching patient age for patient_phone: " + currentPatientId);
        String query = "SELECT patient_age FROM appointments WHERE patient_phone = ? ORDER BY appointment_date DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String age = rs.getString("patient_age");
                System.out.println("Fetched patient_age: " + age);
                return age != null ? age : "N/A";
            } else {
                System.out.println("No record found for patient_phone: " + currentPatientId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient age: " + e.getMessage());
        }
        return "N/A";
    }

    private String fetchPatientGenderFromAppointments() {
        if (currentPatientId == null) {
            System.out.println("currentPatientId is null");
            return "N/A";
        }
        System.out.println("Fetching patient gender for patient_phone: " + currentPatientId);
        String query = "SELECT patient_gender FROM appointments WHERE patient_phone = ? ORDER BY appointment_date DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentPatientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String gender = rs.getString("patient_gender");
                System.out.println("Fetched patient_gender: " + gender);
                return gender != null ? gender : "N/A";
            } else {
                System.out.println("No record found for patient_phone: " + currentPatientId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient gender: " + e.getMessage());
        }
        return "N/A";
    }

    private void updateLabels() {
        if (patientNameLabel != null && patientAgeLabel != null && patientGenderLabel != null) {
            patientNameLabel.setText(this.patientName);
            patientAgeLabel.setText(this.patientAge + (this.patientAge.matches("\\d+") ? " years" : ""));
            patientGenderLabel.setText(this.patientGender);
        }
    }

    public void setPatientId(String patientPhone) {
        this.currentPatientId = patientPhone;
        System.out.println("Setting patientId to: " + patientPhone);
        // Re-fetch and update patient info when patientPhone changes
        setPatientInfo(null, null, null); // Trigger fetch from appointments
    }

    public void setDoctorId(String doctorId) {
        this.currentDoctorId = doctorId;
        System.out.println("Setting doctorId to: " + doctorId);
    }

    private DoctorInfo fetchDoctorInfo(String doctorId) {
        if (doctorId == null) {
            System.out.println("doctorId is null");
            return new DoctorInfo("Unknown", "Unknown", "N/A", "N/A");
        }
        String query = "SELECT d.first_name, d.last_name, pd.specialization, pd.medical_license_number " +
                "FROM doctors d " +
                "LEFT JOIN professional_details pd ON d.govt_id = pd.govt_id " +
                "WHERE d.govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String specialization = rs.getString("specialization");
                String licenseNumber = rs.getString("medical_license_number");
                return new DoctorInfo(
                        (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""),
                        specialization != null ? specialization : "Unknown",
                        licenseNumber != null ? licenseNumber : "N/A",
                        "N/A" // No registration_number in schema
                );
            } else {
                System.out.println("No doctor record found for govt_id: " + doctorId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor info: " + e.getMessage());
        }
        return new DoctorInfo("Unknown", "Unknown", "N/A", "N/A");
    }
}