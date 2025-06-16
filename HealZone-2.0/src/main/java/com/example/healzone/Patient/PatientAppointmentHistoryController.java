//package com.example.healzone.Patient;
//
//import com.example.healzone.DatabaseConnection.Appointments;
//import com.example.healzone.DatabaseConnection.Prescription;
//import com.example.healzone.Doctor.DoctorInfo;
//import com.example.healzone.PrescriptionData;
//import com.example.healzone.PrescriptionViewController;
//import com.example.healzone.SessionManager;
//import javafx.application.Platform;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.concurrent.Task;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.input.KeyCode;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//import javafx.util.Callback;
//import org.kordamp.ikonli.javafx.FontIcon;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import static com.example.healzone.DatabaseConnection.DatabaseConnection.connection;
//
//public class PatientAppointmentHistoryController {
//    @FXML
//    private TableView<Map<String, Object>> historyTable;
//    @FXML
//    private TableColumn<Map<String, Object>, String> appointmentNumberColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> doctorNameColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> hospitalColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> dateColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> statusColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> diagnosisColumn;
//    @FXML
//    private TableColumn<Map<String, Object>, String> actionColumn;
//    @FXML
//    private Label noHistoryLabel;
//    @FXML
//    private ScrollPane scrollPane;
//    @FXML
//    private Button refreshButton;
//    @FXML
//    private HBox historyStats;
//    @FXML
//    private TextField doctorSearchBar;
//    @FXML
//    private Button searchButton;
//    @FXML
//    private Button clearSearchButton;
//    @FXML
//    private DatePicker fromDatePicker;
//    @FXML
//    private DatePicker toDatePicker;
//    @FXML
//    private ComboBox<String> statusFilter;
//    @FXML
//    private ComboBox<String> sortComboBox;
//    @FXML
//    private Button applyFiltersButton;
//    @FXML
//    private Button resetFiltersButton;
//
//    private static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    private List<Map<String, Object>> originalHistoryData;
//    private List<Map<String, Object>> filteredHistoryData;
//
//    @FXML
//    private void initialize() {
//        if (historyTable == null || doctorSearchBar == null || fromDatePicker == null || toDatePicker == null ||
//                statusFilter == null || sortComboBox == null || applyFiltersButton == null || resetFiltersButton == null) {
//            System.err.println("One or more FXML elements are null in PatientAppointmentHistoryController");
//            return;
//        }
//        setupTableColumns();
//        setupTableStyling();
//        setupScrollPane();
//        setupFiltersAndSearch();
//        loadHistoryStats();
//        loadAppointmentHistory();
//    }
//
//    private void setupFiltersAndSearch() {
//        statusFilter.getItems().addAll("All", "Completed", "Cancelled", "No-Show", "Pending", "Confirmed");
//        statusFilter.setValue("All");
//
//        sortComboBox.getItems().addAll(
//                "Date (Newest First)",
//                "Date (Oldest First)",
//                "Doctor Name (A-Z)",
//                "Doctor Name (Z-A)",
//                "Hospital Name (A-Z)",
//                "Status"
//        );
//        sortComboBox.setValue("Date (Newest First)");
//
//        searchButton.setOnAction(event -> searchDoctor());
//        clearSearchButton.setOnAction(event -> clearSearch());
//
//        doctorSearchBar.setOnKeyPressed(event -> {
//            if (event.getCode() == KeyCode.ENTER) {
//                searchDoctor();
//            }
//        });
//
//        doctorSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal.isEmpty()) {
//                clearSearch();
//            }
//        });
//
//        fromDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE).minusMonths(3));
//        toDatePicker.setValue(LocalDate.now(PAKISTAN_ZONE));
//    }
//
//    private void setupTableColumns() {
//        appointmentNumberColumn.setCellValueFactory(data ->
//                new SimpleStringProperty("#" + String.valueOf(data.getValue().get("appointment_number"))));
//        appointmentNumberColumn.setCellFactory(createStyledCellFactory("appointment-number-cell"));
//
//        doctorNameColumn.setCellValueFactory(data ->
//                new SimpleStringProperty(String.valueOf(data.getValue().get("doctor_name"))));
//        doctorNameColumn.setCellFactory(createStyledCellFactory("doctor-name-cell"));
//
//        hospitalColumn.setCellValueFactory(data ->
//                new SimpleStringProperty(String.valueOf(data.getValue().get("hospital_name"))));
//        hospitalColumn.setCellFactory(createStyledCellFactory("hospital-cell"));
//
//        dateColumn.setCellValueFactory(data -> {
//            Object dateObj = data.getValue().get("appointment_date");
//            if (dateObj != null) {
//                try {
//                    LocalDateTime dateTime = LocalDateTime.parse(dateObj.toString(), DATE_FORMATTER);
//                    return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")));
//                } catch (Exception e) {
//                    return new SimpleStringProperty(dateObj.toString());
//                }
//            }
//            return new SimpleStringProperty("N/A");
//        });
//        dateColumn.setCellFactory(createStyledCellFactory("date-cell"));
//
//        statusColumn.setCellValueFactory(data ->
//                new SimpleStringProperty(String.valueOf(data.getValue().get("status"))));
//        statusColumn.setCellFactory(createStatusCellFactory());
//
//        diagnosisColumn.setCellValueFactory(data -> {
//            Object diagnosis = data.getValue().get("diagnosis");
//            String diagnosisText = (diagnosis != null && !diagnosis.toString().isEmpty())
//                    ? diagnosis.toString()
//                    : "Not specified";
//            return new SimpleStringProperty(diagnosisText);
//        });
//        diagnosisColumn.setCellFactory(createStyledCellFactory("diagnosis-cell"));
//
//        actionColumn.setCellFactory(createActionCellFactory());
//    }
//
//    private String formatDate(String dateStr) {
//        try {
//            LocalDateTime dateTime = LocalDateTime.parse(dateStr, DATE_FORMATTER);
//            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"));
//        } catch (Exception e) {
//            return dateStr;
//        }
//    }
//
//    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
//    createStyledCellFactory(String styleClass) {
//        return column -> {
//            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                        setGraphic(null);
//                    } else {
//                        setText(item);
//                    }
//                }
//            };
//            cell.getStyleClass().add(styleClass);
//            return cell;
//        };
//    }
//
//    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
//    createStatusCellFactory() {
//        return column -> {
//            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty || item == null) {
//                        setText(null);
//                        setGraphic(null);
//                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show", "status-pending", "status-confirmed");
//                    } else {
//                        setText(item);
//                        getStyleClass().removeAll("status-completed", "status-cancelled", "status-no-show", "status-pending", "status-confirmed");
//                        switch (item.toLowerCase()) {
//                            case "completed":
//                                getStyleClass().add("status-completed");
//                                break;
//                            case "cancelled":
//                                getStyleClass().add("status-cancelled");
//                                break;
//                            case "no-show":
//                                getStyleClass().add("status-no-show");
//                                break;
//                            case "pending":
//                                getStyleClass().add("status-pending");
//                                break;
//                            case "confirmed":
//                                getStyleClass().add("status-confirmed");
//                                break;
//                        }
//                    }
//                }
//            };
//            return cell;
//        };
//    }
//
//    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
//    createActionCellFactory() {
//        return column -> {
//            TableCell<Map<String, Object>, String> cell = new TableCell<>() {
//                private final Button viewPrescriptionBtn = new Button("View Prescription");
//                private final HBox actionBox = new HBox(5);
//
//                {
//                    viewPrescriptionBtn.getStyleClass().add("prescription-button");
//                    viewPrescriptionBtn.setPrefHeight(25);
//                    viewPrescriptionBtn.setPrefWidth(120);
//
//                    FontIcon prescriptionIcon = new FontIcon("fas-prescription-bottle-alt");
//                    prescriptionIcon.setIconSize(12);
//                    prescriptionIcon.setIconColor(javafx.scene.paint.Color.WHITE);
//                    viewPrescriptionBtn.setGraphic(prescriptionIcon);
//
//                    actionBox.getChildren().add(viewPrescriptionBtn);
//                    actionBox.setAlignment(javafx.geometry.Pos.CENTER);
//
//                    viewPrescriptionBtn.setOnAction(event -> {
//                        Map<String, Object> appointmentData = getTableView().getItems().get(getIndex());
//                        viewPrescription(appointmentData);
//                    });
//                }
//
//                @Override
//                protected void updateItem(String item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty) {
//                        setGraphic(null);
//                    } else {
//                        Map<String, Object> appointmentData = getTableView().getItems().get(getIndex());
//                        String status = String.valueOf(appointmentData.get("status"));
//                        if ("Completed".equalsIgnoreCase(status) && hasPrescription(String.valueOf(appointmentData.get("appointment_number")))) {
//                            setGraphic(actionBox);
//                        } else {
//                            setGraphic(null);
//                        }
//                    }
//                }
//            };
//            return cell;
//        };
//    }
//
//    private void setupTableStyling() {
//        historyTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//
//        historyTable.setRowFactory(tv -> {
//            TableRow<Map<String, Object>> row = new TableRow<>();
//            row.itemProperty().addListener((obs, oldItem, newItem) -> {
//                if (newItem != null) {
//                    row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-hover"));
//                    row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-hover"));
//                }
//            });
//            return row;
//        });
//
//        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//    }
//
//    private void setupScrollPane() {
//        if (scrollPane != null) {
//            scrollPane.setFitToWidth(true);
//            scrollPane.setFitToHeight(true);
//            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        }
//    }
//
//    private void loadHistoryStats() {
//        if (historyStats == null) return;
//
//        Task<Map<String, Integer>> statsTask = new Task<>() {
//            @Override
//            protected Map<String, Integer> call() throws Exception {
//                String patientPhone = Patient.getPhone();
//                if (patientPhone == null) return new java.util.HashMap<>();
//                Map<String, Integer> stats = new java.util.HashMap<>();
//                stats.put("total", getTotalAppointmentsForPatient(patientPhone));
//                stats.put("thisMonth", getAppointmentsThisMonthForPatient(patientPhone));
//                stats.put("completed", getCompletedAppointmentsForPatient(patientPhone));
//                return stats;
//            }
//        };
//
//        statsTask.setOnSucceeded(event -> {
//            Platform.runLater(() -> {
//                Map<String, Integer> stats = statsTask.getValue();
//                createStatCards(stats);
//            });
//        });
//
//        new Thread(statsTask).start();
//    }
//
//    private void createStatCards(Map<String, Integer> stats) {
//        if (historyStats == null) return;
