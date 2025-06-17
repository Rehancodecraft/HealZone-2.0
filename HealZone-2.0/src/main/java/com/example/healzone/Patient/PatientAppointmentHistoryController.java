package com.example.healzone.Patient;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.DatabaseConnection.Prescription;
import com.example.healzone.Doctor.DoctorInfo;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");

    private List<Map<String, Object>> originalHistoryData;
    private List<Map<String, Object>> filteredHistoryData;

    @FXML
    private void initialize() {
        // Check for null elements and log if any are missing
        if (historyTable == null || doctorSearchBar == null || fromDatePicker == null || toDatePicker == null ||
                statusFilter == null || sortComboBox == null || applyFiltersButton == null || resetFiltersButton == null ||
                noHistoryLabel == null || scrollPane == null || refreshButton == null || historyStats == null) {
            System.err.println("One or more FXML elements are null in PatientAppointmentHistoryController");
            return;
        }

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
            if (dateObj instanceof java.sql.Date) {
                return new SimpleStringProperty(((java.sql.Date) dateObj).toLocalDate()
                        .atStartOfDay(PAKISTAN_ZONE)
                        .format(DISPLAY_DATE_FORMATTER));
            } else if (dateObj instanceof String) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse((String) dateObj, DATE_FORMATTER);
                    return new SimpleStringProperty(dateTime.format(DISPLAY_DATE_FORMATTER));
                } catch (Exception e) {
                    return new SimpleStringProperty("N/A");
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
        return column -> new TableCell<>() {
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
                        case "completed" -> getStyleClass().add("status-completed");
                        case "cancelled" -> getStyleClass().add("status-cancelled");
                        case "no-show" -> getStyleClass().add("status-no-show");
                        case "pending" -> getStyleClass().add("status-pending");
                        case "confirmed" -> getStyleClass().add("status-confirmed");
                    }
                }
            }
        };
    }

    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
    createActionCellFactory() {
        return column -> new TableCell<>() {
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
                    String status = String.valueOf(appointmentData.getOrDefault("status", ""));
                    String appointmentNumber = String.valueOf(appointmentData.getOrDefault("appointment_number", ""));
                    if ("Completed".equalsIgnoreCase(status) && hasPrescription(appointmentNumber)) {
                        setGraphic(actionBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
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
                String patientPhone = Patient.getPhone();
                if (patientPhone == null) return new java.util.HashMap<>();
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
        if (historyStats == null) return;
        historyStats.getChildren().clear();

        VBox totalCard = createStatCard("Total Appointments", String.valueOf(stats.getOrDefault("total", 0)), "fas-calendar-check");
        VBox monthCard = createStatCard("This Month", String.valueOf(stats.getOrDefault("thisMonth", 0)), "fas-calendar-day");
        VBox completedCard = createStatCard("Completed", String.valueOf(stats.getOrDefault("completed", 0)), "fas-check-circle");

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
                if (patientPhone == null) return List.of();
                return getAppointmentHistoryForPatient(patientPhone);
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                originalHistoryData = loadTask.getValue();
                filteredHistoryData = new ArrayList<>(originalHistoryData); // Initialize with original data
                applyCurrentFilters();
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                noHistoryLabel.setVisible(true);
                System.err.println("Failed to load appointment history: " + loadTask.getException().getMessage());
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
                if (patientPhone == null) return List.of();
                return getAppointmentHistoryForPatient(patientPhone);
            }
        };

        refreshTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                originalHistoryData = refreshTask.getValue();
                filteredHistoryData = new ArrayList<>(originalHistoryData); // Reset filtered data
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
                noHistoryLabel.setVisible(true);
                System.err.println("Refresh failed: " + refreshTask.getException().getMessage());
            });
        });

        new Thread(refreshTask).start();
        if (doctorSearchBar != null) doctorSearchBar.requestFocus();
    }

    @FXML
    private void searchDoctor() {
        applyCurrentFilters();
        if (doctorSearchBar != null) doctorSearchBar.requestFocus();
    }

    @FXML
    private void clearSearch() {
        if (doctorSearchBar != null) doctorSearchBar.clear();
        applyCurrentFilters();
        if (doctorSearchBar != null) doctorSearchBar.requestFocus();
    }

    @FXML
    private void applyFilters() {
        applyCurrentFilters();
        if (doctorSearchBar != null) doctorSearchBar.requestFocus();
    }

    @FXML
    private void resetFilters() {
        if (fromDatePicker != null) fromDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE).minusMonths(3));
        if (toDatePicker != null) toDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE));
        if (statusFilter != null) statusFilter.setValue("All");
        if (sortComboBox != null) sortComboBox.setValue("Date (Newest First)");
        if (doctorSearchBar != null) doctorSearchBar.clear();
        applyCurrentFilters();
        if (doctorSearchBar != null) doctorSearchBar.requestFocus();
    }

    private void applyCurrentFilters() {
        if (originalHistoryData == null) {
            setHistoryData(List.of());
            return;
        }

        filteredHistoryData = originalHistoryData.stream()
                .filter(this::matchesFilters)
                .collect(Collectors.toList());

        filteredHistoryData = applySorting(filteredHistoryData);
        setHistoryData(filteredHistoryData);
    }

    private boolean matchesFilters(Map<String, Object> appointment) {
        String searchQuery = doctorSearchBar != null ? doctorSearchBar.getText().trim().toLowerCase() : "";
        if (!searchQuery.isEmpty()) {
            String doctorName = String.valueOf(appointment.getOrDefault("doctor_name", "")).toLowerCase();
            String hospitalName = String.valueOf(appointment.getOrDefault("hospital_name", "")).toLowerCase();
            if (!doctorName.contains(searchQuery) && !hospitalName.contains(searchQuery)) {
                return false;
            }
        }

        String statusFilterValue = statusFilter != null ? statusFilter.getValue() : "All";
        if (!"All".equals(statusFilterValue)) {
            String appointmentStatus = String.valueOf(appointment.getOrDefault("status", ""));
            if (!statusFilterValue.equalsIgnoreCase(appointmentStatus)) {
                return false;
            }
        }

        LocalDate fromDate = fromDatePicker != null ? fromDatePicker.getValue() : null;
        LocalDate toDate = toDatePicker != null ? toDatePicker.getValue() : null;
        if (fromDate != null || toDate != null) {
            Object dateObj = appointment.get("appointment_date");
            if (dateObj instanceof java.sql.Date) {
                LocalDate appointmentDate = ((java.sql.Date) dateObj).toLocalDate();
                if (fromDate != null && appointmentDate.isBefore(fromDate)) return false;
                if (toDate != null && appointmentDate.isAfter(toDate)) return false;
            }
        }

        return true;
    }

    private List<Map<String, Object>> applySorting(List<Map<String, Object>> data) {
        String sortBy = sortComboBox != null ? sortComboBox.getValue() : "Date (Newest First)";

        return switch (sortBy) {
            case "Date (Newest First)" -> data.stream()
                    .sorted((a, b) -> {
                        Object dateA = a.get("appointment_date");
                        Object dateB = b.get("appointment_date");
                        if (dateA instanceof java.sql.Date && dateB instanceof java.sql.Date) {
                            return ((java.sql.Date) dateB).compareTo((java.sql.Date) dateA);
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            case "Date (Oldest First)" -> data.stream()
                    .sorted((a, b) -> {
                        Object dateA = a.get("appointment_date");
                        Object dateB = b.get("appointment_date");
                        if (dateA instanceof java.sql.Date && dateB instanceof java.sql.Date) {
                            return ((java.sql.Date) dateA).compareTo((java.sql.Date) dateB);
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());
            case "Doctor Name (A-Z)" -> data.stream()
                    .sorted((a, b) -> String.valueOf(a.getOrDefault("doctor_name", ""))
                            .compareToIgnoreCase(String.valueOf(b.getOrDefault("doctor_name", ""))))
                    .collect(Collectors.toList());
            case "Doctor Name (Z-A)" -> data.stream()
                    .sorted((a, b) -> String.valueOf(b.getOrDefault("doctor_name", ""))
                            .compareToIgnoreCase(String.valueOf(a.getOrDefault("doctor_name", ""))))
                    .collect(Collectors.toList());
            case "Hospital Name (A-Z)" -> data.stream()
                    .sorted((a, b) -> String.valueOf(a.getOrDefault("hospital_name", ""))
                            .compareToIgnoreCase(String.valueOf(b.getOrDefault("hospital_name", ""))))
                    .collect(Collectors.toList());
            case "Status" -> data.stream()
                    .sorted((a, b) -> String.valueOf(a.getOrDefault("status", ""))
                            .compareToIgnoreCase(String.valueOf(b.getOrDefault("status", ""))))
                    .collect(Collectors.toList());
            default -> data;
        };
    }

    private void viewPrescription(Map<String, Object> appointmentData) {
        try {
            String appointmentNumber = String.valueOf(appointmentData.getOrDefault("appointment_number", ""));
            if (appointmentNumber.isEmpty() || appointmentNumber.equals("null")) {
                showError("No appointment number found for this record.");
                return;
            }

            PrescriptionData prescriptionData = Prescription.fetchPrescriptionById(appointmentNumber);
            if (prescriptionData == null) {
                showError("No prescription found for appointment #" + appointmentNumber);
                return;
            }

            String doctorId = String.valueOf(appointmentData.getOrDefault("doctor_id", ""));
            if (!doctorId.isEmpty() && !doctorId.equals("null")) {
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

            String patientPhone = Patient.getPhone();
            if (patientPhone != null) {
                prescriptionData.setPatientId(patientPhone);
                Map<String, Object> patientInfo = getPatientInfoByPhone(patientPhone);
                if (patientInfo != null) {
                    prescriptionData.setPatientName(String.valueOf(patientInfo.getOrDefault("patient_name", "")));
                    String ageStr = String.valueOf(patientInfo.getOrDefault("patient_age", ""));
                    prescriptionData.setPatientAge(ageStr.isEmpty() || ageStr.equals("null") ? 0 : Integer.parseInt(ageStr));
                    prescriptionData.setPatientGender(String.valueOf(patientInfo.getOrDefault("patient_gender", "")));
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/PrescriptionView.fxml"));
            Parent root = loader.load();

            PrescriptionViewController controller = loader.getController();
            controller.loadPrescription(prescriptionData);

            Stage prescriptionStage = new Stage();
            prescriptionStage.setTitle("View Prescription - " + (prescriptionData.getPatientName().isEmpty() ? "Unknown" : prescriptionData.getPatientName()));
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
        if (doctorId == null || doctorId.isEmpty()) {
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
                        "N/A",
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

    private List<Map<String, Object>> getAppointmentHistoryForPatient(String patientPhone) {
        String query = """
            SELECT a.doctor_id, a.appointment_number, a.patient_phone, a.appointment_date, a.day_of_week,
                   a.consultation_fee, a.hospital_name, a.hospital_address, a.doctor_name, a.patient_name,
                   a.status, a.created_at, a.diagnosis
            FROM appointments a
            WHERE a.patient_phone = ?
            ORDER BY a.appointment_date DESC
            """;
        List<Map<String, Object>> appointments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, patientPhone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("doctor_id", rs.getString("doctor_id"));
                    appointment.put("appointment_number", rs.getString("appointment_number"));
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
                    appointment.put("diagnosis", rs.getString("diagnosis")); // Added diagnosis
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting appointment history for patient: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    private int getTotalAppointmentsForPatient(String patientPhone) {
        String query = "SELECT COUNT(*) FROM appointments WHERE patient_phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, patientPhone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting completed appointments: " + e.getMessage());
        }
        return 0;
    }

    private Map<String, Object> getPatientInfoByPhone(String patientPhone) {
        String query = """
            SELECT name AS patient_name, age AS patient_age, gender AS patient_gender
            FROM patients
            WHERE phone_number = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, patientPhone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> patientInfo = new HashMap<>();
                    patientInfo.put("patient_name", rs.getString("patient_name"));
                    patientInfo.put("patient_age", rs.getString("patient_age"));
                    patientInfo.put("patient_gender", rs.getString("patient_gender"));
                    return patientInfo;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient info by phone: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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

    private boolean hasPrescription(String appointmentNumber) {
        if (appointmentNumber == null || appointmentNumber.isEmpty() || appointmentNumber.equals("null")) {
            return false;
        }
        String query = "SELECT COUNT(*) FROM prescriptions WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking prescription existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}