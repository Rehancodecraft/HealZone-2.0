package com.example.healzone;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.Doctor.DoctorDashboardController;
import com.example.healzone.Doctor.DoctorPrescriptionController;
import com.example.healzone.Doctor.ExtendedPrescriptionData;
import com.example.healzone.Doctor.Medicine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.transform.Scale;
import javafx.concurrent.Task;
import javafx.application.Platform;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.healzone.DatabaseConnection.Prescription;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class PrescriptionViewController implements Initializable {

    // Header Labels
    @FXML private Label doctorNameLabel;
    @FXML private Label doctorSpecializationLabel;
    @FXML private Label licenseLabel;
    @FXML private Label hospitalName;
    @FXML private Label hospitalAddress;
    @FXML private Label doctorPhoneEmail;

    // Patient Information Labels
    @FXML private Label patientNameLabel;
    @FXML private Label patientAgeLabel;
    @FXML private Label patientGenderLabel;
    @FXML private Label prescriptionDateLabel;
    @FXML private Label patientIdLabel;

    // Content Areas
    @FXML private VBox diagnosisSection;
    @FXML private TextArea diagnosisText;
    @FXML private VBox medicationsContainer;
    @FXML private VBox precautionsSection;
    @FXML private TextArea precautionsText;
    @FXML private VBox followupSection;
    @FXML private Label followupText;
    @FXML private VBox notesSection;
    @FXML private TextArea notesText;
    @FXML private Label prescriptionIdLabel;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    // Data Storage
    private PrescriptionData prescriptionData;
    private int prescriptionId; // Track the prescription ID for database-loaded prescriptions
    private Map<String, Object> currentAppointment; // Add this line
    private Stage parentStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set current date
        setCurrentDate();

        // Generate or set prescription ID (to be overridden when loading from database)
        generatePrescriptionId();

        // Initialize text areas to be non-focusable for better print appearance
        setupTextAreas();
    }

    private void setupTextAreas() {
        diagnosisText.setFocusTraversable(false);
        precautionsText.setFocusTraversable(false);
        notesText.setFocusTraversable(false);
    }

    private void setCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        prescriptionDateLabel.setText(currentDate.format(formatter)); // Will show 16/06/2025
    }

    private void generatePrescriptionId() {
        // This will be overridden when loading from database
        String prescriptionId = "RX" + System.currentTimeMillis();
        prescriptionIdLabel.setText("Prescription ID: " + prescriptionId);
    }

    public void loadPrescription(PrescriptionData prescriptionData) {
        this.prescriptionData = prescriptionData;
        if (prescriptionData != null) {
            populateView();
        } else {
            System.out.println("Prescription data is null");
        }
    }

    public void populateView() {
        if (prescriptionData == null) return;

        // Set patient information
        patientNameLabel.setText(prescriptionData.getPatientName() != null ? prescriptionData.getPatientName() : "Unknown");
        patientAgeLabel.setText(prescriptionData.getPatientAge() > 0 ? prescriptionData.getPatientAge() + " years" : "N/A");
        patientGenderLabel.setText(prescriptionData.getPatientGender() != null ? prescriptionData.getPatientGender() : "N/A");
        patientIdLabel.setText(prescriptionData.getPatientId() != null ? prescriptionData.getPatientId() : "N/A");

        // Set doctor information using setDoctorInfo
        setDoctorInfo(
                prescriptionData.getDoctorName(),
                prescriptionData.getDoctorSpecialization(),
                prescriptionData.getLicenseNumber(),
                prescriptionData.getHospitalName(),
                prescriptionData.getHospitalAddress(),
                formatDoctorContact(prescriptionData.getDoctorPhone(), prescriptionData.getDoctorEmail()),
                prescriptionData.getDoctorId()
        );

        // Set diagnosis
        if (prescriptionData.getDiagnosis() != null && !prescriptionData.getDiagnosis().trim().isEmpty()) {
            diagnosisText.setText(prescriptionData.getDiagnosis());
            diagnosisSection.setVisible(true);
            diagnosisSection.setManaged(true);
        } else {
            diagnosisSection.setVisible(false);
            diagnosisSection.setManaged(false);
        }

        // Populate medications
        populateMedications();

        // Set precautions
        if (prescriptionData.getPrecautions() != null && !prescriptionData.getPrecautions().trim().isEmpty()) {
            precautionsText.setText(prescriptionData.getPrecautions());
            precautionsSection.setVisible(true);
            precautionsSection.setManaged(true);
        } else {
            precautionsSection.setVisible(false);
            precautionsSection.setManaged(false);
        }

        // Set follow-up
        if (prescriptionData.getFollowup() != null && !prescriptionData.getFollowup().trim().isEmpty()) {
            followupText.setText("Next visit in " + prescriptionData.getFollowup().trim());
            followupSection.setVisible(true);
            followupSection.setManaged(true);
        } else {
            followupSection.setVisible(false);
            followupSection.setManaged(false);
        }

        // Set notes
        if (prescriptionData.getNotes() != null && !prescriptionData.getNotes().trim().isEmpty()) {
            notesText.setText(prescriptionData.getNotes());
            notesSection.setVisible(true);
            notesSection.setManaged(true);
        } else {
            notesSection.setVisible(false);
            notesSection.setManaged(false);
        }

        // Update prescription ID
        if (prescriptionId > 0) {
            prescriptionIdLabel.setText("Prescription ID: RX" + prescriptionId);
        } else {
            prescriptionIdLabel.setText("Prescription ID: Preview");
        }
    }

    private void populateMedications() {
        medicationsContainer.getChildren().clear();
        System.out.println("Populating medications for prescription ID: " + prescriptionId);

        List<MedicationData> medications = prescriptionData.getMedications();
        if (medications == null || medications.isEmpty()) {
            System.out.println("No medications found in prescription data.");
            return;
        }

        System.out.println("Found " + medications.size() + " medications to display.");
        int counter = 1;
        for (MedicationData medication : medications) {
            System.out.println("Adding medication: " + medication.getMedicineName());
            VBox medicationItem = createMedicationItem(counter, medication);
            medicationsContainer.getChildren().add(medicationItem);
            counter++;
        }
    }

    private VBox createMedicationItem(int number, MedicationData medication) {
        VBox medicationItem = new VBox();
        medicationItem.getStyleClass().add("medication-item");

        // Medicine number and name
        HBox nameContainer = new HBox(5);
        Label numberLabel = new Label(number + ".");
        numberLabel.getStyleClass().add("medication-number");

        Label nameLabel = new Label(medication.getMedicineName());
        nameLabel.getStyleClass().add("medication-name");

        Label typeLabel = new Label("(" + medication.getType() + ")");
        typeLabel.getStyleClass().add("medication-type");

        nameContainer.getChildren().addAll(numberLabel, nameLabel, typeLabel);

        // Medicine details
        VBox detailsContainer = new VBox(2);

        Label dosageLabel = new Label("┌─ Dosage: " + medication.getDosage());
        dosageLabel.getStyleClass().add("medication-detail");

        Label frequencyLabel = new Label("├─ Frequency: " + medication.getFrequency());
        frequencyLabel.getStyleClass().add("medication-detail");

        Label timingLabel = new Label("├─ Timing: " + medication.getTiming());
        timingLabel.getStyleClass().add("medication-detail");

        Label durationLabel = new Label("└─ Duration: " + medication.getDuration());
        durationLabel.getStyleClass().add("medication-detail");

        detailsContainer.getChildren().addAll(dosageLabel, frequencyLabel, timingLabel, durationLabel);

        medicationItem.getChildren().addAll(nameContainer, detailsContainer);

        // Add instructions if available
        if (medication.getInstructions() != null && !medication.getInstructions().trim().isEmpty()) {
            Label instructionsLabel = new Label("📋 Instructions: " + medication.getInstructions());
            instructionsLabel.getStyleClass().add("medication-instructions");
            medicationItem.getChildren().add(instructionsLabel);
        }

        return medicationItem;
    }

    @FXML
    private void savePrescription(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Prescription");
        String baseFileName = "Prescription_" + (prescriptionData.getPatientName() != null ? prescriptionData.getPatientName().replaceAll("\\s+", "_") : "Unknown") + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG Images", "*.png");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files", "*.pdf");

        fileChooser.getExtensionFilters().addAll(pdfFilter, pngFilter);
        fileChooser.setSelectedExtensionFilter(pdfFilter);
        fileChooser.setInitialFileName(baseFileName + ".pdf");

        File defaultDir = new File("/home/rehan-shafiq/HealZone-2.0/PrescriptionReports");
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        fileChooser.setInitialDirectory(defaultDir);

        File selectedFile = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                hideButtons(true);

                String fileName = selectedFile.getName().toLowerCase();
                File finalFile;

                FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
                if (selectedFilter == pdfFilter && !fileName.endsWith(".pdf")) {
                    finalFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pdf");
                } else if (selectedFilter == pngFilter && !fileName.endsWith(".png")) {
                    finalFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
                } else {
                    finalFile = selectedFile;
                }

                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                System.out.println("Final file: " + finalFile.getAbsolutePath());
                System.out.println("Selected filter: " + (selectedFilter == pdfFilter ? "PDF" : "PNG"));

                // Take screenshot and save as PDF/PNG
                WritableImage image = takeScreenshot();
                if (image == null) {
                    throw new IOException("Failed to capture screenshot");
                }

                String finalFileName = finalFile.getName().toLowerCase();
                if (finalFileName.endsWith(".pdf")) {
                    saveAsPDF(finalFile, image);
                } else if (finalFileName.endsWith(".png")) {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    if (bufferedImage == null) {
                        throw new IOException("Failed to convert image");
                    }
                    ImageIO.write(bufferedImage, "PNG", finalFile);
                } else {
                    throw new IOException("Unsupported file format.");
                }

                // Check if there is a current appointment and prompt confirmation
                if (currentAppointment != null) {
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Confirm Attendance");
                    dialog.setHeaderText("Mark appointment as attended?");
                    dialog.setContentText("Patient: " + currentAppointment.get("patient_name") +
                            "\nAppointment #: " + currentAppointment.get("appointment_number"));

                    ButtonType okayButton = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(okayButton, ButtonType.CANCEL);

                    Optional<ButtonType> result = dialog.showAndWait();
                    if (result.isPresent() && result.get() == okayButton) {
                        // Mark appointment as attended
                        Task<Void> attendTask = new Task<>() {
                            @Override
                            protected Void call() {
                                Appointments.markAsAttended(prescriptionData.getDoctorId(), (String) currentAppointment.get("appointment_number"));
                                return null;
                            }
                        };

                        attendTask.setOnSucceeded(event1 -> {
                            Platform.runLater(() -> {
//                                showSuccessMessage("Appointment marked as attended.");
                                // Save prescription using the appointment number
                                savePrescriptionToDatabase();
                                if (finalFile.exists() && finalFile.length() > 0) {
                                    System.out.println("Save Success: Prescription saved successfully as " + finalFile.getName() +
                                            "\nLocation: " + finalFile.getAbsolutePath() +
                                            "\nFile size: " + finalFile.length() + " bytes" +
                                            "\nSaved to database.");
                                    Stage stage = (Stage) saveButton.getScene().getWindow();
                                    stage.close();
                                    if (parentStage != null) {
                                        parentStage.close();
                                        DoctorDashboardController dashboardController = (DoctorDashboardController) parentStage.getUserData();
                                        if (dashboardController != null) {
                                            dashboardController.refreshDashboard();
                                        }
                                    }
                                } else {
                                    try {
                                        throw new IOException("File not created or empty.");
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        });

                        attendTask.setOnFailed(event1 -> {
                            Platform.runLater(() -> {
//                                showErrorMessage("Failed to mark appointment as attended: " + attendTask.getException().getMessage());
                            });
                        });

                        new Thread(attendTask).start();
                        return; // Exit early to wait for task completion
                    } else {
                        // If cancelled, do not proceed with saving
                        return;
                    }
                }

                // If no appointment or cancelled, save prescription directly
                savePrescriptionToDatabase();
                if (finalFile.exists() && finalFile.length() > 0) {
                    System.out.println("Save Success: Prescription saved successfully as " + finalFile.getName() +
                            "\nLocation: " + finalFile.getAbsolutePath() +
                            "\nFile size: " + finalFile.length() + " bytes" +
                            "\nSaved to database.");
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                    if (parentStage != null) {
                        parentStage.close();
                        DoctorDashboardController dashboardController = (DoctorDashboardController) parentStage.getUserData();
                        if (dashboardController != null) {
                            dashboardController.refreshDashboard();
                        }
                    }
                } else {
                    throw new IOException("File not created or empty.");
                }
            } catch (Exception e) {
                System.out.println("Save error: " + e.getMessage());
                e.printStackTrace();
                System.out.println("Save Error: Failed to save prescription: " + e.getMessage());
            } finally {
                hideButtons(false);
            }
        }
    }

    private void savePrescriptionToDatabase() {
        Prescription prescription = new Prescription();
        prescription.setDoctorId(prescriptionData.getDoctorId());
        prescription.setPatientId(prescriptionData.getPatientId());
        prescription.setPatientName(prescriptionData.getPatientName());
        // Use appointment number from parent controller if currentAppointment is null
        String appointmentNumber = currentAppointment != null ? (String) currentAppointment.get("appointment_number") :
                (parentStage != null ? (String) ((DoctorPrescriptionController) parentStage.getUserData()).getCurrentAppointment().get("appointment_number") : null);
        if (appointmentNumber == null) {
            System.out.println("Warning: No valid appointment number found. Using empty string.");
            appointmentNumber = ""; // Fallback to empty string if no appointment number
        }
        prescription.setAppointmentNumber(appointmentNumber);
        prescription.setDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        prescription.setDiagnosis(prescriptionData.getDiagnosis());
        prescription.setPrecautions(prescriptionData.getPrecautions());
        prescription.setFollowup(prescriptionData.getFollowup());
        prescription.setNotes(prescriptionData.getNotes());

        StringBuilder medicinesString = new StringBuilder();
        boolean first = true;
        for (MedicationData medication : prescriptionData.getMedications()) {
            if (!first) {
                medicinesString.append(";");
            }
            medicinesString.append(String.format("%s|%s|%s|%s|%s|%s|%s",
                    medication.getMedicineName(),
                    medication.getType(),
                    medication.getDosage(),
                    medication.getFrequency(),
                    medication.getTiming(),
                    medication.getDuration(),
                    medication.getInstructions() != null ? medication.getInstructions() : ""
            ));
            first = false;
        }
        prescription.setMedicines(medicinesString.toString());
        try {
            prescription.save();
            System.out.println("Database Success: Prescription saved to database.");
        } catch (Exception e) {
            System.out.println("Database Error: Error saving prescription to database: " + e.getMessage());
        }
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        if (parentStage != null) {
            parentStage.close();
            DoctorDashboardController dashboardController = (DoctorDashboardController) parentStage.getUserData();
            if (dashboardController != null) {
                dashboardController.refreshDashboard();
            }
        }
    }

    private void saveAsPDF(File file, WritableImage image) throws IOException {
        PDDocument document = null;
        try {
            System.out.println("Creating PDF document for: " + file.getAbsolutePath());
            document = new PDDocument();

            // Create A4 page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Convert JavaFX image to BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            if (bufferedImage == null) {
                throw new IOException("Failed to convert JavaFX image to BufferedImage");
            }

            System.out.println("BufferedImage dimensions: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());

            // Create PDF image object
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

            // Calculate scaling to fit A4 page while maintaining aspect ratio
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float imageWidth = bufferedImage.getWidth();
            float imageHeight = bufferedImage.getHeight();

            // Calculate scale to fit page
            float scaleX = pageWidth / imageWidth;
            float scaleY = pageHeight / imageHeight;
            float scale = Math.min(scaleX, scaleY);

            float scaledWidth = imageWidth * scale;
            float scaledHeight = imageHeight * scale;

            // Center image on page
            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            System.out.println("Image scaling - Original: " + imageWidth + "x" + imageHeight +
                    ", Scaled: " + scaledWidth + "x" + scaledHeight +
                    ", Position: " + x + "," + y);

            // Draw image on PDF page
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
            }

            System.out.println("Saving PDF to: " + file.getAbsolutePath());
            document.save(file);
            System.out.println("PDF saved successfully. File size: " + file.length() + " bytes");

        } catch (Exception e) {
            System.out.println("PDF save error: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error saving PDF: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    System.err.println("Error closing PDF document: " + e.getMessage());
                }
            }
        }
    }

    private WritableImage takeScreenshot() {
        hideButtons(true);

        try {
            // Get the root node (the entire prescription content)
            Node rootNode = saveButton.getScene().getRoot();

            // If the root is in a ScrollPane, we need to handle it differently
            Node contentNode = rootNode;

            // Find the main content container (usually the root or a child of ScrollPane)
            if (rootNode instanceof ScrollPane) {
                contentNode = ((ScrollPane) rootNode).getContent();
            } else {
                // Look for ScrollPane in the scene graph
                contentNode = findScrollableContent(rootNode);
            }

            // Create snapshot parameters for high resolution
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(2.0, 2.0)); // High-resolution snapshot

            // Take snapshot of the content node (this captures the full content regardless of scroll position)
            WritableImage image = contentNode.snapshot(params, null);

            return image;

        } catch (Exception e) {
            System.err.println("Error taking screenshot: " + e.getMessage());
            e.printStackTrace();

            // Fallback to original method if the new approach fails
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(2.0, 2.0));
            return saveButton.getScene().getRoot().snapshot(params, null);

        } finally {
            hideButtons(false);
        }
    }

    private Node findScrollableContent(Node node) {
        if (node instanceof ScrollPane) {
            return ((ScrollPane) node).getContent();
        }

        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                Node result = findScrollableContent(child);
                if (result != child) { // If we found a ScrollPane's content
                    return result;
                }
            }
        }

        return node; // Return the original node if no ScrollPane found
    }

    private void hideButtons(boolean hide) {
        saveButton.setVisible(!hide);
        closeButton.setVisible(!hide);
        saveButton.setManaged(!hide);
        closeButton.setManaged(!hide);
    }

    @FXML
    private void closePrescription(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        if (parentStage != null) {
            parentStage.close();
            DoctorDashboardController dashboardController = (DoctorDashboardController) parentStage.getUserData();
            if (dashboardController != null) {
                dashboardController.refreshDashboard();
            }
        }
    }

    public void setDoctorInfo(String name, String specialization, String license, String hospitalName, String hospitalAddress, String doctorPhoneEmail, String doctorId) {
        if (name != null && !name.trim().isEmpty()) {
            doctorNameLabel.setText(name);
        }
        if (specialization != null && !specialization.trim().isEmpty()) {
            doctorSpecializationLabel.setText(specialization);
        }
        if (license != null && !license.trim().isEmpty()) {
            licenseLabel.setText("License No: " + license);
        }
        if (hospitalName != null && !hospitalName.trim().isEmpty()) {
            this.hospitalName.setText(hospitalName);
        }
        if (hospitalAddress != null && !hospitalAddress.trim().isEmpty()) {
            this.hospitalAddress.setText(hospitalAddress);
        }
        if (doctorPhoneEmail != null && !doctorPhoneEmail.trim().isEmpty()) {
            this.doctorPhoneEmail.setText(doctorPhoneEmail);
        }
        this.prescriptionData.setDoctorId(doctorId); // Assuming setDoctorId exists in PrescriptionData
    }

    public void refreshView() {
        if (prescriptionData != null) {
            populateView();
        }
    }

    public boolean validatePrescriptionData() {
        if (prescriptionData == null) {
            System.out.println("Validation Error: No prescription data available.");
            return false;
        }

        if (prescriptionData.getPatientName() == null || prescriptionData.getPatientName().trim().isEmpty()) {
            System.out.println("Validation Error: Patient name is required.");
            return false;
        }

        if (prescriptionData.getMedications() == null || prescriptionData.getMedications().isEmpty()) {
            System.out.println("Validation Error: At least one medication is required.");
            return false;
        }

        return true;
    }

    private String formatDoctorContact(String phone, String email) {
        String formattedPhone = (phone != null && !phone.trim().isEmpty()) ? formatPhoneNumber(phone.trim()) : "N/A";
        String formattedEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : "N/A";
        return "Phone: " + formattedPhone + " | Email: " + formattedEmail;
    }

    private String formatPhoneNumber(String phone) {
        if (phone != null && phone.length() == 11 && phone.matches("\\d+")) {
            return "+92" + phone.substring(1);
        }
        return phone != null ? phone : "";
    }

    public Map<String, Object> getCurrentAppointment() {
        return currentAppointment;
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void setCurrentAppointment(Map<String, Object> appointment) {
        this.currentAppointment = appointment;
    }

    public void setDoctorPrescriptionData(String doctorId, String patientId, ObservableList<Medicine> medicinesList) {
        if (prescriptionData == null) {
            prescriptionData = new PrescriptionData();
        }
        prescriptionData.setDoctorId(doctorId);
        prescriptionData.setPatientId(patientId);
        // Convert medicinesList to MedicationData list
        ObservableList<MedicationData> medicationDataList = FXCollections.observableArrayList();
        if (medicinesList != null) {
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
        }
    }
}