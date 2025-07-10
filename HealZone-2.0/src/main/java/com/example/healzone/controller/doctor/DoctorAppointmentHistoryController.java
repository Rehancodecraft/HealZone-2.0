package com.example.healzone.controller.doctor;

import com.example.healzone.repository.AppointmentRepository;
import com.example.healzone.repository.PrescriptionRepository;
import com.example.healzone.model.PrescriptionPrintModel;
import com.example.healzone.controller.shared.PrescriptionViewController;
import com.example.healzone.util.SessionManager;
import com.example.healzone.model.DoctorDetailsModel;
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

import static com.example.healzone.config.DatabaseConfig.connection;

public class DoctorAppointmentHistoryController {
    @FXML
    private TableView<Map<String, Object>> historyTable;
    @FXML
    private TableColumn<Map<String, Object>, String> appointmentNumberColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> patientNameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> phoneColumn;
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
    private TextField patientSearchBar;
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
//    @FXML
//    private Button applyFiltersButton;
//    @FXML
//    private Button resetFiltersButton;

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
        statusFilter.setDisable(true); // Disable status filter
        statusFilter.setValue("Completed"); // Reflect the applied filter
    }

    private void setupFiltersAndSearch() {
        statusFilter.getItems().addAll("All", "Completed", "Cancelled", "No-Show");
        statusFilter.setValue("All");

        sortComboBox.getItems().addAll(
                "Date (Newest First)",
                "Date (Oldest First)",
                "Patient Name (A-Z)",
                "Patient Name (Z-A)",
                "Status"
        );
        sortComboBox.setValue("Date (Newest First)");

        searchButton.setOnAction(event -> searchPatient());
        clearSearchButton.setOnAction(event -> clearSearch());

        patientSearchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchPatient();
            }
        });

        patientSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
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

        patientNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("patient_name"))));
        patientNameColumn.setCellFactory(createStyledCellFactory("patient-name-cell"));

        phoneColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("patient_phone"))));
        phoneColumn.setCellFactory(createStyledCellFactory("phone-cell"));

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
                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show");
                    } else {
                        setText(item);
                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show");
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
                        setGraphic(actionBox);
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
                String doctorId = SessionManager.getCurrentDoctorId();
                Map<String, Integer> stats = new java.util.HashMap<>();
                stats.put("total", AppointmentRepository.getTotalCompletedAppointments(doctorId));
                stats.put("thisMonth", AppointmentRepository.getCompletedAppointmentsThisMonth(doctorId));
                stats.put("cancelled", AppointmentRepository.getCancelledAppointments(doctorId));
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

        VBox totalCard = createStatCard("Total History", String.valueOf(stats.get("total")), "fas-calendar-check");
        VBox monthCard = createStatCard("This Month", String.valueOf(stats.get("thisMonth")), "fas-calendar-day");
        VBox cancelledCard = createStatCard("Cancelled", String.valueOf(stats.get("cancelled")), "fas-calendar-times");

        historyStats.getChildren().addAll(totalCard, monthCard, cancelledCard);
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
                String doctorId = SessionManager.getCurrentDoctorId();
                return AppointmentRepository.getAppointmentHistoryForDoctor(doctorId)
                        .stream()
                        .filter(appointment -> "Completed".equalsIgnoreCase(String.valueOf(appointment.get("status"))))
                        .collect(Collectors.toList());
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                originalHistoryData = loadTask.getValue();
                filteredHistoryData = originalHistoryData; // No further status filtering needed
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
                String doctorId = SessionManager.getCurrentDoctorId();
                return AppointmentRepository.getAppointmentHistoryForDoctor(doctorId)
                        .stream()
                        .filter(appointment -> "Completed".equalsIgnoreCase(String.valueOf(appointment.get("status"))))
                        .collect(Collectors.toList());
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
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
    }

    @FXML
    private void searchPatient() {
        applyCurrentFilters();
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
    }

    @FXML
    private void clearSearch() {
        patientSearchBar.clear();
        applyCurrentFilters();
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
    }

    @FXML
    private void applyFilters() {
        applyCurrentFilters();
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
    }

    @FXML
    private void resetFilters() {
        fromDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE).minusMonths(3));
        toDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE));
        statusFilter.setValue("All");
        sortComboBox.setValue("Date (Newest First)");
        patientSearchBar.clear();
        applyCurrentFilters();
        Node focustarget = patientSearchBar;
        focustarget.requestFocus();
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
        String searchQuery = patientSearchBar.getText().trim().toLowerCase();
        if (!searchQuery.isEmpty()) {
            String patientName = String.valueOf(appointment.get("patient_name")).toLowerCase();
            String patientPhone = String.valueOf(appointment.get("patient_phone"));
            if (!patientName.contains(searchQuery) && !patientPhone.contains(searchQuery)) {
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
            case "Patient Name (A-Z)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(a.get("patient_name"))
                                    .compareToIgnoreCase(String.valueOf(b.get("patient_name"))))
                            .collect(Collectors.toList());
            case "Patient Name (Z-A)" ->
                    data.stream()
                            .sorted((a, b) -> String.valueOf(b.get("patient_name"))
                                    .compareToIgnoreCase(String.valueOf(a.get("patient_name"))))
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
            String appointmentNumber = (String) appointmentData.get("appointment_number");
            if (appointmentNumber == null) {
                showError("No appointment number found for this record.");
                return;
            }

            // Fetch prescription using appointment number
            PrescriptionPrintModel prescriptionData = PrescriptionRepository.fetchPrescriptionById(appointmentNumber);
            if (prescriptionData == null) {
                showError("No prescription found for appointment #" + appointmentNumber);
                return;
            }

            // Fetch doctor info using the current doctor ID
            String doctorId = SessionManager.getCurrentDoctorId();
            if (doctorId != null) {
                DoctorDetailsModel doctorInfo = fetchDoctorInfo(doctorId);
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

            // Fetch additional patient info from appointments table if missing
            String patientPhone = (String) appointmentData.get("patient_phone");
            if (patientPhone != null && (prescriptionData.getPatientAge() == 0 || prescriptionData.getPatientGender() == null)) {
                Map<String, Object> patientInfo = AppointmentRepository.getPatientInfoByPhone(patientPhone);
                if (patientInfo != null) {
                    String ageStr = (String) patientInfo.get("patient_age");
                    prescriptionData.setPatientAge(ageStr != null ? Integer.parseInt(ageStr) : 0);
                    prescriptionData.setPatientGender((String) patientInfo.get("patient_gender"));
                    if (prescriptionData.getPatientName() == null) {
                        prescriptionData.setPatientName((String) appointmentData.get("patient_name"));
                    }
                }
            }

            // Ensure patient phone is set
            if (prescriptionData.getPatientId() == null && patientPhone != null) {
                prescriptionData.setPatientId(patientPhone);
            }

            // Load the PrescriptionView and pass prescription data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/prescription-view.fxml"));
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
    private DoctorDetailsModel fetchDoctorInfo(String doctorId) {
        // Reuse or implement similar to DoctorPrescriptionController.fetchDoctorInfo
        if (doctorId == null) {
            return new DoctorDetailsModel("Unknown", "Unknown", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
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
                return new DoctorDetailsModel(
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
        return new DoctorDetailsModel("Unknown", "Unknown", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
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

//    public void refreshData() {
//        refreshHistory();
//    }
//
//    public void clearAllFilters() {
//        resetFilters();
//    }
//
//    public void setSearchQuery(String query) {
//        if (patientSearchBar != null) {
//            patientSearchBar.setText(query);
//            searchPatient();
//        }
//    }

    public List<Map<String, Object>> getCurrentFilteredData() {
        return filteredHistoryData != null ? filteredHistoryData : originalHistoryData;
    }

    public int getTotalHistoryCount() {
        return originalHistoryData != null ? originalHistoryData.size() : 0;
    }

    public int getFilteredHistoryCount() {
        return filteredHistoryData != null ? filteredHistoryData.size() : 0;
    }
}