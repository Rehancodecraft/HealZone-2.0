package com.example.healzone;

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
    @FXML private Label registrationLabel;

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
    @FXML private Button printButton;
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    // Data Storage
    private PrescriptionData prescriptionData;
    private int prescriptionId; // Track the prescription ID for database-loaded prescriptions

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
        prescriptionDateLabel.setText(currentDate.format(formatter)); // Will show 15/06/2025
    }

    private void generatePrescriptionId() {
        // This will be overridden when loading from database
        String prescriptionId = "RX" + System.currentTimeMillis();
        prescriptionIdLabel.setText("Prescription ID: " + prescriptionId);
    }

    // Method to load prescription by ID from database
//    public void loadPrescription(int id) {
//        this.prescriptionId = id;
//        this.prescriptionData = Prescription.fetchPrescriptionById(id);
//        if (prescriptionData != null) {
//            populateView();
//        } else {
//            showErrorAlert("Load Error", "Failed to load prescription with ID: " + id);
//        }
//    }

    // Overloaded method to load prescription data directly for preview
    public void loadPrescription(PrescriptionData data) {
        this.prescriptionId = -1; // Indicate this is a preview (not saved)
        this.prescriptionData = data;
        if (prescriptionData != null) {
            populateView();
        } else {
            showErrorAlert("Load Error", "No prescription data available.");
        }
    }

    public void populateView() {
        if (prescriptionData == null) return;

        // Set patient information
        patientNameLabel.setText(prescriptionData.getPatientName() != null ? prescriptionData.getPatientName() : "Unknown");
        patientAgeLabel.setText(prescriptionData.getPatientAge() > 0 ? prescriptionData.getPatientAge() + " years" : "N/A");
        patientGenderLabel.setText(prescriptionData.getPatientGender() != null ? prescriptionData.getPatientGender() : "N/A");
        patientIdLabel.setText(prescriptionData.getPatientId() != null ? prescriptionData.getPatientId() : "N/A");

        // Set doctor information
        setDoctorInfo(
                prescriptionData.getDoctorName(),
                prescriptionData.getDoctorSpecialization(),
                prescriptionData.getLicenseNumber(),
                prescriptionData.getRegistrationNumber()
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
            followupText.setText("Next visit in " + prescriptionData.getFollowup());
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

        List<MedicationData> medications = prescriptionData.getMedications();
        if (medications == null || medications.isEmpty()) return;

        int counter = 1;
        for (MedicationData medication : medications) {
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
    private void printPrescription(ActionEvent event) {
        try {
            // Check for available printers
            ObservableList<Printer> printers = (ObservableList<Printer>) Printer.getAllPrinters();
            if (printers.isEmpty()) {
                showErrorAlert("Printer Error", "No printer detected. Please ensure a printer is connected and installed.");
                return;
            }

            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null) {
                hideButtons(true);
                if (printerJob.showPrintDialog(printButton.getScene().getWindow())) {
                    Node contentToPrint = printButton.getScene().getRoot();
                    boolean success = printerJob.printPage(contentToPrint);
                    if (success) {
                        printerJob.endJob();
                        showSuccessAlert("Print Success", "Prescription printed successfully!");
                    } else {
                        showErrorAlert("Print Error", "Failed to print prescription. Please try again.");
                    }
                } else {
                    showInfoAlert("Print Cancelled", "Printing was cancelled by user.");
                }
            } else {
                showErrorAlert("Printer Error", "No printer available. Please check your printer connections.");
            }
        } catch (Exception e) {
            showErrorAlert("Print Error", "An error occurred while printing: " + e.getMessage());
        } finally {
            hideButtons(false);
        }
    }

    @FXML
    private void savePrescription(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Prescription");
        String baseFileName = "Prescription_" + (prescriptionData.getPatientName() != null ? prescriptionData.getPatientName().replaceAll("\\s+", "_") : "Unknown") + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Set default extension filters
        FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG Images", "*.png");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files", "*.pdf");

        fileChooser.getExtensionFilters().addAll(pdfFilter, pngFilter); // PDF first as default
        fileChooser.setSelectedExtensionFilter(pdfFilter); // Set PDF as default
        fileChooser.setInitialFileName(baseFileName + ".pdf");

        // Ensure directory exists
        File defaultDir = new File("/home/rehan-shafiq/HealZone-2.0/PrescriptionReports");
        if (!defaultDir.exists()) {
            defaultDir.mkdirs();
        }
        fileChooser.setInitialDirectory(defaultDir);

        File selectedFile = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                hideButtons(true);

                // Force correct extension based on selected filter
                String fileName = selectedFile.getName().toLowerCase();
                File finalFile = selectedFile;

                // Check if file has correct extension, if not add it
                FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
                if (selectedFilter == pdfFilter && !fileName.endsWith(".pdf")) {
                    finalFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".pdf");
                } else if (selectedFilter == pngFilter && !fileName.endsWith(".png")) {
                    finalFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
                }

                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                System.out.println("Final file: " + finalFile.getAbsolutePath());
                System.out.println("Selected filter: " + (selectedFilter == pdfFilter ? "PDF" : "PNG"));

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
                    throw new IOException("Unsupported file format. Please use .pdf or .png extension.");
                }

                if (finalFile.exists() && finalFile.length() > 0) {
                    showSuccessAlert("Save Success", "Prescription saved successfully as " + finalFile.getName() +
                            "\nLocation: " + finalFile.getAbsolutePath() +
                            "\nFile size: " + finalFile.length() + " bytes");
                } else {
                    throw new IOException("File not created or empty: " + finalFile.getAbsolutePath());
                }

            } catch (Exception e) {
                System.out.println("Save error: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Save Error", "Failed to save prescription: " + e.getMessage());
            } finally {
                hideButtons(false);
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
        printButton.setVisible(!hide);
        saveButton.setVisible(!hide);
        closeButton.setVisible(!hide);
        printButton.setManaged(!hide);
        saveButton.setManaged(!hide);
        closeButton.setManaged(!hide);
    }

    @FXML
    private void closePrescription(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Prescription");
        alert.setHeaderText("Are you sure you want to close this prescription?");
        alert.setContentText("Any unsaved changes will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showProgressDialog(String message, Runnable task) {
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Processing");
        progressAlert.setHeaderText(null);
        progressAlert.setContentText(message + " Please wait...");

        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                task.run();
                return null;
            }
        };

        backgroundTask.setOnSucceeded(e -> progressAlert.close());
        backgroundTask.setOnFailed(e -> {
            progressAlert.close();
            showErrorAlert("Error", "Operation failed: " + backgroundTask.getException().getMessage());
        });

        Thread taskThread = new Thread(backgroundTask);
        taskThread.setDaemon(true);
        taskThread.start();

        progressAlert.show();
    }

    public void setDoctorInfo(String name, String specialization, String license, String registration) {
        if (name != null && !name.trim().isEmpty()) {
            doctorNameLabel.setText(name);
        }
        if (specialization != null && !specialization.trim().isEmpty()) {
            doctorSpecializationLabel.setText(specialization);
        }
        if (license != null && !license.trim().isEmpty()) {
            licenseLabel.setText("License No: " + license);
        }
        if (registration != null && !registration.trim().isEmpty()) {
            registrationLabel.setText("Registration: " + registration);
        }
    }

    public void refreshView() {
        if (prescriptionData != null) {
            populateView();
        }
    }

    public boolean validatePrescriptionData() {
        if (prescriptionData == null) {
            showErrorAlert("Validation Error", "No prescription data available.");
            return false;
        }

        if (prescriptionData.getPatientName() == null || prescriptionData.getPatientName().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Patient name is required.");
            return false;
        }

        if (prescriptionData.getMedications() == null || prescriptionData.getMedications().isEmpty()) {
            showErrorAlert("Validation Error", "At least one medication is required.");
            return false;
        }

        return true;
    }
}