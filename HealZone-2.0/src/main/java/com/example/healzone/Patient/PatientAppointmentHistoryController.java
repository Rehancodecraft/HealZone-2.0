package com.example.healzone.Patient;
import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.DatabaseConnection.Prescription;
import com.example.healzone.PrescriptionData;
import com.example.healzone.PrescriptionViewController;
import com.example.healzone.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;

public class PatientAppointmentHistoryController {
    @FXML
    private TableView<Map<String, Object>> historyTable;
    @FXML
    private TableColumn<Map<String, Object>, String> appointmentNumberColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> doctorNameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> hospitalColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> dateColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> statusColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> diagnosisColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> actionColumn;
    @FXML
    private Label noHistoryLabel;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button refreshButton;
    @FXML
    private HBox historyStats;
    @FXML
    private TextField doctorSearchBar;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearSearchButton;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private Button applyFiltersButton;
    @FXML
    private Button resetFiltersButton;

    private static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");

    private List<Map<String, Object>> originalHistoryData;
    private List<Map<String, Object>> filteredHistoryData;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupTableStyling();
        setupScrollPane();
        setupFiltersAndSearch();
        loadHistoryStats();
        loadAppointmentHistory();
    }

    private void setupFiltersAndSearch() {
        statusFilter.getItems().addAll("All", "Completed", "Cancelled", "No-Show", "Pending", "Confirmed");
        statusFilter.setValue("All");

        sortComboBox.getItems().addAll(
                "Date (Newest First)",
                "Date (Oldest First)",
                "Doctor Name (A-Z)",
                "Doctor Name (Z-A)",
                "Hospital Name (A-Z)",
                "Status"
        );
        sortComboBox.setValue("Date (Newest First)");

        searchButton.setOnAction(event -> searchDoctor());
        clearSearchButton.setOnAction(event -> clearSearch());

        doctorSearchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchDoctor();
            }
        });

        doctorSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                clearSearch();
            }
        });

        fromDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE).minusMonths(3));
        toDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE));
    }

    private void setupTableColumns() {
        appointmentNumberColumn.setCellValueFactory(data ->
                new SimpleStringProperty("#" + String.valueOf(data.getValue().get("appointment_number"))));
        appointmentNumberColumn.setCellFactory(createStyledCellFactory("appointment-number-cell"));

        doctorNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("doctor_name"))));
        doctorNameColumn.setCellFactory(createStyledCellFactory("doctor-name-cell"));

        hospitalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("hospital_name"))));
        hospitalColumn.setCellFactory(createStyledCellFactory("hospital-cell"));

        dateColumn.setCellValueFactory(data -> {
            Object dateObj = data.getValue().get("appointment_date");
            if (dateObj != null) {
                try {
                    String dateStr = dateObj.toString();
                    return new SimpleStringProperty(formatDate(dateStr));
                } catch (Exception e) {
                    return new SimpleStringProperty(dateObj.toString());
                }
            }
            return new SimpleStringProperty("N/A");
        });
        dateColumn.setCellFactory(createStyledCellFactory("date-cell"));

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("status"))));
        statusColumn.setCellFactory(createStatusCellFactory());

        diagnosisColumn.setCellValueFactory(data -> {
            Object diagnosis = data.getValue().get("diagnosis");
            String diagnosisText = (diagnosis != null && !diagnosis.toString().isEmpty())
                    ? diagnosis.toString()
                    : "Not specified";
            return new SimpleStringProperty(diagnosisText);
        });
        diagnosisColumn.setCellFactory(createStyledCellFactory("diagnosis-cell"));

        actionColumn.setCellFactory(createActionCellFactory());
    }

    private String formatDate(String dateStr) {
        try {
            return dateStr; // Placeholder - adjust based on actual date format
        } catch (Exception e) {
            return dateStr;
        }
    }

    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
    createStyledCellFactory(String styleClass) {
        return column -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.getStyleClass().add(styleClass);
            return cell;
        };
    }

    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
    createStatusCellFactory() {
        return column -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show", "status-pending", "status-confirmed");
                    } else {
                        setText(item);
                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show", "status-pending", "status-confirmed");
                        switch (item.toLowerCase()) {
                            case "completed":
                                getStyleClass().add("status-completed");
                                break;
                            case "cancelled":
                                getStyleClass().add("status-cancelled");
                                break;
                            case "no-show":
                                getStyleClass().add("status-no-show");
                                break;
                            case "pending":
                                getStyleClass().add("status-pending");
                                break;
                            case "confirmed":
                                getStyleClass().add("status-confirmed");
                                break;
                        }
                    }
                }
            };
            return cell;
        };
    }

    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
    createActionCellFactory() {
        return column -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
                private final Button viewPrescriptionBtn = new Button("View Prescription");
                private final HBox actionBox = new HBox(5);

                {
                    viewPrescriptionBtn.getStyleClass().add("prescription-button");
                    viewPrescriptionBtn.setPrefHeight(25);
                    viewPrescriptionBtn.setPrefWidth(120);

                    FontIcon prescriptionIcon = new FontIcon("fas-prescription-bottle-alt");
                    prescriptionIcon.setIconSize(12);
                    prescriptionIcon.setIconColor(javafx.scene.paint.Color.WHITE);
                    viewPrescriptionBtn.setGraphic(prescriptionIcon);

                    actionBox.getChildren().add(viewPrescriptionBtn);
                    actionBox.setAlignment(javafx.geometry.Pos.CENTER);

                    // Add action handler
                    viewPrescriptionBtn.setOnAction(event -> {
                        Map<String, Object> appointmentData = getTableView().getItems().get(getIndex());
                        viewPrescription(appointmentData);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Map<String, Object> appointmentData = getTableView().getItems().get(getIndex());
                        String status = String.valueOf(appointmentData.get("status"));
                        // Only show prescription button for completed appointments
                        if ("Completed".equalsIgnoreCase(status)) {
                            setGraphic(actionBox);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            };
            return cell;
        };
    }

    private void setupTableStyling() {
        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        historyTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-hover"));
                    row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-hover"));
                }
            });
            return row;
        });

        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupScrollPane() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }

    private void loadHistoryStats() {
        if (historyStats == null) return;

        Task<Map<String, Integer>> statsTask = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                String patientPhone = Patient.getPhone(); // Assuming patient is identified by phone
                Map<String, Integer> stats = new java.util.HashMap<>();
                stats.put("total", getTotalAppointmentsForPatient(patientPhone));
                stats.put("thisMonth", getAppointmentsThisMonthForPatient(patientPhone));
                stats.put("completed", getCompletedAppointmentsForPatient(patientPhone));
                return stats;
            }
        };

        statsTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                Map<String, Integer> stats = statsTask.getValue();
                createStatCards(stats);
            });
        });

        new Thread(statsTask).start();
    }

    private void createStatCards(Map<String, Integer> stats) {
        historyStats.getChildren().clear();

        VBox totalCard = createStatCard("Total Appointments", String.valueOf(stats.get("total")), "fas-calendar-check");
        VBox monthCard = createStatCard("This Month", String.valueOf(stats.get("thisMonth")), "fas-calendar-day");
        VBox completedCard = createStatCard("Completed", String.valueOf(stats.get("completed")), "fas-check-circle");

        historyStats.getChildren().addAll(totalCard, monthCard, completedCard);
    }

    private VBox createStatCard(String label, String value, String iconLiteral) {
        VBox card = new VBox(2);
        card.getStyleClass().add("stat-mini-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.web("#60a5fa"));

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-mini-number");
        valueLabel.setFont(Font.font("SansSerif Bold", 18));

        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-mini-label");
        labelText.setFont(Font.font("SansSerif", 16));

        card.getChildren().addAll(icon, valueLabel, labelText);
        return card;
    }

    private void loadAppointmentHistory() {
        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String patientPhone = Patient.getPhone();
                return getAppointmentHistoryForPatient(patientPhone);
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                originalHistoryData = loadTask.getValue();
                filteredHistoryData = originalHistoryData;
                setHistoryData(filteredHistoryData);
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                noHistoryLabel.setVisible(true);
            });
        });

        new Thread(loadTask).start();
    }

    public void setHistoryData(List<Map<String, Object>> historyData) {
        Platform.runLater(() -> {
            if (historyData == null || historyData.isEmpty()) {
                historyTable.setVisible(false);
                scrollPane.setVisible(false);
                noHistoryLabel.setVisible(true);
            } else {
                ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(historyData);
                historyTable.setItems(data);
                historyTable.setVisible(true);
                scrollPane.setVisible(true);
                noHistoryLabel.setVisible(false);
            }
        });
    }

    @FXML
    private void refreshHistory() {
        if (refreshButton != null) {
            refreshButton.setDisable(true);
            refreshButton.setText("Refreshing...");
        }

        Task<List<Map<String, Object>>> refreshTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String patientPhone = Patient.getPhone();
                return getAppointmentHistoryForPatient(patientPhone);
            }
        };

        refreshTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                originalHistoryData = refreshTask.getValue();
                applyCurrentFilters();
                loadHistoryStats();

                if (refreshButton != null) {
                    refreshButton.setDisable(false);
                    refreshButton.setText("Refresh");
                }
            });
        });

        refreshTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                if (refreshButton != null) {
                    refreshButton.setDisable(false);
                    refreshButton.setText("Refresh");
                }
            });
        });

        new Thread(refreshTask).start();
        Node focusTarget = doctorSearchBar;
        focusTarget.requestFocus();
    }

    @FXML
    private void searchDoctor() {
        applyCurrentFilters();
        Node focusTarget = doctorSearchBar;
        focusTarget.requestFocus();
    }

    @FXML
    private void clearSearch() {
        doctorSearchBar.clear();
        applyCurrentFilters();
        Node focusTarget = doctorSearchBar;
        focusTarget.requestFocus();
    }

    @FXML
    private void applyFilters() {
        applyCurrentFilters();
        Node focusTarget = doctorSearchBar;
        focusTarget.requestFocus();
    }

    @FXML
    private void resetFilters() {
        fromDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE).minusMonths(3));
        toDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE));
        statusFilter.setValue("All");
        sortComboBox.setValue("Date (Newest First)");
        doctorSearchBar.clear();
        applyCurrentFilters();
        Node focusTarget = doctorSearchBar;
        focusTarget.requestFocus();
    }

    private void applyCurrentFilters() {
        if (originalHistoryData == null) return;

        List<Map<String, Object>> filtered = originalHistoryData.stream()
                .filter(this::matchesFilters)
                .collect(Collectors.toList());

        filtered = applySorting(filtered);

        filteredHistoryData = filtered;
        setHistoryData(filteredHistoryData);
    }

    private boolean matchesFilters(Map<String, Object> appointment) {
        String searchQuery = doctorSearchBar.getText().trim().toLowerCase();
        if (!searchQuery.isEmpty()) {
            String doctorName = String.valueOf(appointment.get("doctor_name")).toLowerCase();
            String hospitalName = String.valueOf(appointment.get("hospital_name")).toLowerCase();
            if (!doctorName.contains(searchQuery) && !hospitalName.contains(searchQuery)) {
                return false;
            }
        }

        String statusFilterValue = statusFilter.getValue();
        if (!"All".equals(statusFilterValue)) {
            String appointmentStatus = String.valueOf(appointment.get("status"));
            if (!statusFilterValue.equalsIgnoreCase(appointmentStatus)) {
                return false;
            }
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate != null || toDate != null) {
            try {
                String dateStr = String.valueOf(appointment.get("appointment_date"));
                LocalDate appointmentDate = LocalDate.parse(dateStr.split(" ")[0]); // Adjust parsing

                if (fromDate != null && appointmentDate.isBefore(fromDate)) {
                    return false;
                }
                if (toDate != null && appointmentDate.isAfter(toDate)) {
                    return false;
                }
            } catch (Exception e) {
                // Handle date parsing error
            }
        }

        return true;
    }

    private List<Map<String, Object>> applySorting(List<Map<String, Object>> data) {
        String sortBy = sortComboBox.getValue();

        return switch (sortBy) {
            case "Date (Newest First)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(b.get("appointment_date"))
                                    .compareTo(String.valueOf(a.get("appointment_date"))))
                            .collect(Collectors.toList());
            case "Date (Oldest First)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(a.get("appointment_date"))
                                    .compareTo(String.valueOf(b.get("appointment_date"))))
                            .collect(Collectors.toList());
            case "Doctor Name (A-Z)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(a.get("doctor_name"))
                                    .compareToIgnoreCase(String.valueOf(b.get("doctor_name"))))
                            .collect(Collectors.toList());
            case "Doctor Name (Z-A)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(b.get("doctor_name"))
                                    .compareToIgnoreCase(String.valueOf(a.get("doctor_name"))))
                            .collect(Collectors.toList());
            case "Hospital Name (A-Z)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(a.get("hospital_name"))
                                    .compareToIgnoreCase(String.valueOf(b.get("hospital_name"))))
                            .collect(Collectors.toList());
            case "Status" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(a.get("status"))
                                    .compareToIgnoreCase(String.valueOf(b.get("status"))))
                            .collect(Collectors.toList());
            default -> data;
        };
    }

    private void viewPrescription(Map<String, Object> appointmentData) {
        try {
            String appointmentNumber = String.valueOf(appointmentData.get("appointment_number"));
            if (appointmentNumber == null || appointmentNumber.equals("null")) {
                showError("No appointment number found for this record.");
                return;
            }

            // Fetch prescription using appointment number
            PrescriptionData prescriptionData = Prescription.fetchPrescriptionById(appointmentNumber);
            if (prescriptionData == null) {
                showError("No prescription found for appointment #" + appointmentNumber);
                return;
            }

            // Fetch doctor info using doctor_id from appointment data
            String doctorId = String.valueOf(appointmentData.get("doctor_id"));
            if (doctorId != null && !doctorId.equals("null")) {
                DoctorInfo doctorInfo = fetchDoctorInfo(doctorId);
                if (doctorInfo != null) {
                    prescriptionData.setDoctorName(doctorInfo.getFullName());
                    prescriptionData.setDoctorSpecialization(doctorInfo.getSpecialization());
                    prescriptionData.setLicenseNumber(doctorInfo.getLicenseNumber());
                    prescriptionData.setHospitalName(doctorInfo.getHospitalName());
                    prescriptionData.setHospitalAddress(doctorInfo.getHospitalAddress());
                    prescriptionData.setDoctorPhone(doctorInfo.getDoctorPhone());
                    prescriptionData.setDoctorEmail(doctorInfo.getDoctorEmail());
                }
            }

            // Set current patient info
            String patientPhone = Patient.getPhone();
            if (patientPhone != null) {
                prescriptionData.setPatientId(patientPhone);

                // Get patient details from session or database
                Map<String, Object> patientInfo = getPatientInfoByPhone(patientPhone);
                if (patientInfo != null) {
                    prescriptionData.setPatientName(String.valueOf(patientInfo.get("patient_name")));
                    String ageStr = String.valueOf(patientInfo.get("patient_age"));
                    prescriptionData.setPatientAge(ageStr != null && !ageStr.equals("null") ? Integer.parseInt(ageStr) : 0);
                    prescriptionData.setPatientGender(String.valueOf(patientInfo.get("patient_gender")));
                }
            }

            // Load the PrescriptionView and pass prescription data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/PrescriptionView.fxml"));
            Parent root = loader.load();

            PrescriptionViewController controller = loader.getController();
            controller.loadPrescription(prescriptionData);

            Stage prescriptionStage = new Stage();
            prescriptionStage.setTitle("View Prescription - " + (prescriptionData.getPatientName() != null ? prescriptionData.getPatientName() : "Unknown"));
            prescriptionStage.initModality(Modality.APPLICATION_MODAL);
            prescriptionStage.setScene(new Scene(root));
            prescriptionStage.setResizable(false);
            prescriptionStage.initStyle(StageStyle.DECORATED);
            controller.setParentStage(prescriptionStage);
            prescriptionStage.showAndWait();
        } catch (NumberFormatException e) {
            showError("Invalid patient age format: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open prescription view: " + e.getMessage());
        }
    }

    private DoctorInfo fetchDoctorInfo(String doctorId) {
        if (doctorId == null) {
            return new DoctorInfo("Unknown", "Unknown", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
        }
        String query = "SELECT d.first_name, d.last_name, pd.specialization, pd.medical_license_number, " +
                "pi.hospital_name, pi.hospital_address, d.phone_number, d.email " +
                "FROM doctors d " +
                "LEFT JOIN professional_details pd ON d.govt_id = pd.govt_id " +
                "LEFT JOIN practice_information pi ON d.govt_id = pi.govt_id " +
                "WHERE d.govt_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String specialization = rs.getString("specialization");
                String licenseNumber = rs.getString("medical_license_number");
                String hospitalName = rs.getString("hospital_name");
                String hospitalAddress = rs.getString("hospital_address");
                String doctorPhone = rs.getString("phone_number");
                String doctorEmail = rs.getString("email");
                return new DoctorInfo(
                        (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""),
                        specialization != null ? specialization : "Unknown",
                        licenseNumber != null ? licenseNumber : "N/A",
                        "N/A", // No registration_number in schema
                        hospitalName != null ? hospitalName : "N/A",
                        hospitalAddress != null ? hospitalAddress : "N/A",
                        doctorPhone != null ? doctorPhone : "N/A",
                        doctorEmail != null ? doctorEmail : "N/A"
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor info: " + e.getMessage());
        }
        return new DoctorInfo("Unknown", "Unknown", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
    }

    // Database methods for patient-specific queries
    private List<Map<String, Object>> getAppointmentHistoryForPatient(String patientPhone) {
//        return Appointments.getAppointmentHistoryForPatient(patientPhone);
    }

    private int getTotalAppointmentsForPatient(String patientPhone) {
        String query = "SELECT COUNT(*) FROM appointments WHERE patient_phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, patientPhone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total appointments: " + e.getMessage());
        }
        return 0;
    }

    private int getAppointmentsThisMonthForPatient(String patientPhone) {
        String query = "SELECT COUNT(*) FROM appointments WHERE patient_phone = ? AND " +
                "MONTH(appointment_date) = MONTH(CURRENT_DATE()) AND YEAR(appointment_date) = YEAR(CURRENT_DATE())";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, patientPhone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting appointments this month: " + e.getMessage());
        }
        return 0;
    }

    private int getCompletedAppointmentsForPatient(String patientPhone) {
        String query = "SELECT COUNT(*) FROM appointments WHERE patient_phone = ? AND status = 'Completed'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, patientPhone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments: " + e.getMessage());
        }
        return 0;
    }

    private Map<String, Object> getPatientInfoByPhone(String patientPhone) {
        return Appointments.getPatientInfoByPhone(patientPhone);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Public utility methods
    public void refreshData() {
        refreshHistory();
    }

    public void clearAllFilters() {
        resetFilters();
    }

    public void setSearchQuery(String query) {
        if (doctorSearchBar != null) {
            doctorSearchBar.setText(query);
            searchDoctor();
        }
    }
// Helper class for doctor information
public static class DoctorInfo {
    private final String fullName;
    private final String specialization;
    private final String licenseNumber;
    private final String registrationNumber;
    private final String hospitalName;
    private final String hospitalAddress;
    private final String doctorPhone;
    private final String doctorEmail;

    public DoctorInfo(String fullName, String specialization, String licenseNumber,
                      String registrationNumber, String hospitalName, String hospitalAddress,
                      String doctorPhone, String doctorEmail) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.registrationNumber = registrationNumber;
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.doctorPhone = doctorPhone;
        this.doctorEmail = doctorEmail;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getHospitalName() { return hospitalName; }
    public String getHospitalAddress() { return hospitalAddress; }
    public String getDoctorPhone() { return doctorPhone; }
    public String getDoctorEmail() { return doctorEmail; }
}

// Additional utility methods for better user experience
public void filterByStatus(String status) {
    if (statusFilter != null) {
        statusFilter.setValue(status);
        applyCurrentFilters();
    }
}

public void filterByDateRange(LocalDate fromDate, LocalDate toDate) {
    if (fromDatePicker != null && toDatePicker != null) {
        fromDatePicker.setValue(fromDate);
        toDatePicker.setValue(toDate);
        applyCurrentFilters();
    }
}

public void sortBy(String sortOption) {
    if (sortComboBox != null) {
        sortComboBox.setValue(sortOption);
        applyCurrentFilters();
    }
}

// Method to get current filter state
public Map<String, Object> getCurrentFilterState() {
    Map<String, Object> filterState = new java.util.HashMap<>();
    filterState.put("searchQuery", doctorSearchBar != null ? doctorSearchBar.getText() : "");
    filterState.put("fromDate", fromDatePicker != null ? fromDatePicker.getValue() : null);
    filterState.put("toDate", toDatePicker != null ? toDatePicker.getValue() : null);
    filterState.put("statusFilter", statusFilter != null ? statusFilter.getValue() : "All");
    filterState.put("sortBy", sortComboBox != null ? sortComboBox.getValue() : "Date (Newest First)");
    return filterState;
}

// Method to restore filter state
public void restoreFilterState(Map<String, Object> filterState) {
    if (filterState == null) return;

    Platform.runLater(() -> {
        if (doctorSearchBar != null && filterState.get("searchQuery") != null) {
            doctorSearchBar.setText((String) filterState.get("searchQuery"));
        }
        if (fromDatePicker != null && filterState.get("fromDate") != null) {
            fromDatePicker.setValue((LocalDate) filterState.get("fromDate"));
        }
        if (toDatePicker != null && filterState.get("toDate") != null) {
            toDatePicker.setValue((LocalDate) filterState.get("toDate"));
        }
        if (statusFilter != null && filterState.get("statusFilter") != null) {
            statusFilter.setValue((String) filterState.get("statusFilter"));
        }
        if (sortComboBox != null && filterState.get("sortBy") != null) {
            sortComboBox.setValue((String) filterState.get("sortBy"));
        }
        applyCurrentFilters();
    });
}

// Export functionality (if needed)
public void exportHistory() {
    if (filteredHistoryData == null || filteredHistoryData.isEmpty()) {
        showError("No data to export");
        return;
    }

    // Implementation for CSV export could be added here
    // For now, just show a placeholder message
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Export Functionality");
        alert.setContentText("Export feature would be implemented here to save appointment history as CSV or PDF.");
        alert.showAndWait();
    });
}

// Method to get statistics summary
public Map<String, Object> getStatisticsSummary() {
    Map<String, Object> stats = new java.util.HashMap<>();

    if (filteredHistoryData != null) {
        stats.put("totalFiltered", filteredHistoryData.size());

        long completedCount = filteredHistoryData.stream()
                .filter(apt -> "Completed".equalsIgnoreCase(String.valueOf(apt.get("status"))))
                .count();
        stats.put("completedFiltered", completedCount);

        long cancelledCount = filteredHistoryData.stream()
                .filter(apt -> "Cancelled".equalsIgnoreCase(String.valueOf(apt.get("status"))))
                .count();
        stats.put("cancelledFiltered", cancelledCount);

        long pendingCount = filteredHistoryData.stream()
                .filter(apt -> "Pending".equalsIgnoreCase(String.valueOf(apt.get("status"))))
                .count();
        stats.put("pendingFiltered", pendingCount);
    }

    return stats;
}

// Method to handle keyboard shortcuts
private void setupKeyboardShortcuts() {
    if (historyTable != null) {
        historyTable.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case R:
                        refreshHistory();
                        event.consume();
                        break;
                    case F:
                        if (doctorSearchBar != null) {
                            doctorSearchBar.requestFocus();
                        }
                        event.consume();
                        break;
                    case E:
                        exportHistory();
                        event.consume();
                        break;
                }
            }
        });
    }
}

// Cleanup method
public void cleanup() {
    if (historyTable != null) {
        historyTable.getItems().clear();
    }
    if (historyStats != null) {
        historyStats.getChildren().clear();
    }
    originalHistoryData = null;
    filteredHistoryData = null;
}

// Method to show success message
private void showSuccess(String message) {
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    });
}

// Method to show confirmation dialog
private boolean showConfirmation(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);

    return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
}

// Method to validate date range
private boolean isValidDateRange(LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null || toDate == null) {
        return true; // Allow null dates
    }
    return !fromDate.isAfter(toDate);
}

// Method to format status for display
private String formatStatus(String status) {
    if (status == null || status.isEmpty()) {
        return "Unknown";
    }
    return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
}

// Method to get appointment count by status
private Map<String, Long> getAppointmentCountByStatus() {
    if (filteredHistoryData == null) {
        return new java.util.HashMap<>();
    }

    return filteredHistoryData.stream()
            .collect(Collectors.groupingBy(
                    apt -> String.valueOf(apt.get("status")),
                    Collectors.counting()
            ));
}

// Method to check if prescription exists for appointment
private boolean hasPrescription(String appointmentNumber) {
    if (appointmentNumber == null || appointmentNumber.equals("null")) {
        return false;
    }

    String query = "SELECT COUNT(*) FROM prescriptions WHERE appointment_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, appointmentNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error checking prescription existence: " + e.getMessage());
    }
    return false;
}
}