package com.example.healzone.controller.doctor;

import com.example.healzone.repository.AppointmentRepository;
import com.example.healzone.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DoctorUpcomingAppointmentsController {
    @FXML
    private TableView<Map<String, Object>> appointmentsTable;
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
    private Label noAppointmentsLabel;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button refreshButton;
    @FXML
    private HBox appointmentStats;
    @FXML
    private TextField patientSearchBar;
    @FXML
    private Button searchButton;

    private static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");

    @FXML
    private void initialize() {
        setupTableColumns();
        setupTableStyling();
        setupScrollPane();
        loadAppointmentStats();

        // Setup search functionality
        searchButton.setOnAction(event -> searchPatient());
        patientSearchBar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchPatient();
            }
        });
        patientSearchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshAppointments();
            }
        });
    }

    private void setupTableColumns() {
        // Appointment Number Column
        appointmentNumberColumn.setCellValueFactory(data ->
                new SimpleStringProperty("#" + String.valueOf(data.getValue().get("appointment_number"))));
        appointmentNumberColumn.setCellFactory(createStyledCellFactory("appointment-number-cell"));

        // Patient Name Column
        patientNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("patient_name"))));
        patientNameColumn.setCellFactory(createStyledCellFactory("patient-name-cell"));

        // Phone Column
        phoneColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("patient_phone"))));
        phoneColumn.setCellFactory(createStyledCellFactory("phone-cell"));

        // Date Column with better formatting
        dateColumn.setCellValueFactory(data -> {
            Object dateObj = data.getValue().get("appointment_date");
            if (dateObj != null) {
                try {
                    // Assuming the date comes as a string, format it nicely
                    String dateStr = dateObj.toString();
                    return new SimpleStringProperty(formatDate(dateStr));
                } catch (Exception e) {
                    return new SimpleStringProperty(dateObj.toString());
                }
            }
            return new SimpleStringProperty("N/A");
        });
        dateColumn.setCellFactory(createStyledCellFactory("date-cell"));

        // Status Column
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("status"))));
        statusColumn.setCellFactory(createStatusCellFactory());
    }

    private String formatDate(String dateStr) {
        try {
            // Parse and reformat the date string for better display
            // Adjust this based on your actual date format from database
            return dateStr; // For now, return as-is
        } catch (Exception e) {
            return dateStr;
        }
    }

    private Callback<TableColumn<Map<String, Object>, String>, TableCell<Map<String, Object>, String>>
    createStyledCellFactory(String styleClass) {
        return column -> {
            TableCell<Map<String, Object>, String> cell = new TableCell<Map<String, Object>, String>() {
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
            TableCell<Map<String, Object>, String> cell = new TableCell<Map<String, Object>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("status-pending", "status-confirmed", "status-cancelled");
                    } else {
                        setText(item);
                        // Add status-specific styling
                        getStyleClass().removeAll("status-pending", "status-confirmed", "status-cancelled");
                        switch (item.toLowerCase()) {
                            case "upcoming":
                            case "confirmed":
                                getStyleClass().add("status-confirmed");
                                break;
                            case "pending":
                                getStyleClass().add("status-pending");
                                break;
                            case "cancelled":
                                getStyleClass().add("status-cancelled");
                                break;
                        }
                    }
                }
            };
            return cell;
        };
    }

    private void setupTableStyling() {
        // Enable table selection
        appointmentsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Set row factory for custom styling
        appointmentsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    // Add hover effects and selection styling
                    row.setOnMouseEntered(e -> row.getStyleClass().add("table-row-hover"));
                    row.setOnMouseExited(e -> row.getStyleClass().remove("table-row-hover"));
                }
            });
            return row;
        });

        // Ensure table resizes properly
        appointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupScrollPane() {
        if (scrollPane != null) {
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
    }

    private void loadAppointmentStats() {
        if (appointmentStats == null) return;

        Task<Map<String, Integer>> statsTask = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                String doctorId = SessionManager.getCurrentDoctorId();
                LocalDate today = LocalDate.now(PAKISTAN_ZONE);

                java.util.Map<String, Integer> stats = new java.util.HashMap<>();
                stats.put("total", AppointmentRepository.getTotalBookedAppointments(doctorId, today));
                stats.put("remaining", AppointmentRepository.getRemainingAppointments(doctorId, today));
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
        appointmentStats.getChildren().clear();

        // Total Appointments Card
        VBox totalCard = createStatCard("Total Today", String.valueOf(stats.get("total")), "fas-calendar-day");

        // Remaining Appointments Card
        VBox remainingCard = createStatCard("Remaining", String.valueOf(stats.get("remaining")), "fas-clock");

        appointmentStats.getChildren().addAll(totalCard, remainingCard);
    }

    private VBox createStatCard(String label, String value, String iconLiteral) {
        VBox card = new VBox(2);
        card.getStyleClass().add("stat-mini-card");
        card.setAlignment(javafx.geometry.Pos.CENTER);

        // Icon
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(16);
        icon.setIconColor(javafx.scene.paint.Color.web("#60a5fa"));

        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-mini-number");
        valueLabel.setFont(Font.font("SansSerif Bold", 18));

        // Label
        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-mini-label");
        labelText.setFont(Font.font("SansSerif", 16));

        card.getChildren().addAll(icon, valueLabel, labelText);
        return card;
    }

    public void setAppointments(List<Map<String, Object>> appointments) {
        Platform.runLater(() -> {
            if (appointments == null || appointments.isEmpty()) {
                // Show no appointments message
                appointmentsTable.setVisible(false);
                scrollPane.setVisible(false);
                noAppointmentsLabel.setVisible(true);
            } else {
                // Show appointments table
                ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(appointments);
                appointmentsTable.setItems(data);
                appointmentsTable.setVisible(true);
                scrollPane.setVisible(true);
                noAppointmentsLabel.setVisible(false);
            }
        });
    }

    @FXML
    private void refreshAppointments() {
        if (refreshButton != null) {
            refreshButton.setDisable(true);
            refreshButton.setText("Refreshing...");
        }

        Task<List<Map<String, Object>>> refreshTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String doctorId = SessionManager.getCurrentDoctorId();
                return AppointmentRepository.getUpcomingAppointmentsForDoctor(doctorId);
            }
        };

        refreshTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Map<String, Object>> appointments = refreshTask.getValue();
                setAppointments(appointments);
                loadAppointmentStats(); // Refresh stats as well

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

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Refresh Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to refresh appointments. Please try again.");
                alert.showAndWait();
            });
        });

        new Thread(refreshTask).start();
    }

    private void searchPatient() {
        String query = patientSearchBar.getText().trim();
        if (query.isEmpty()) {
            refreshAppointments();
            return;
        }

        Task<List<Map<String, Object>>> searchTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String doctorId = SessionManager.getCurrentDoctorId();
                List<Map<String, Object>> appointments = AppointmentRepository.getUpcomingAppointmentsForDoctor(doctorId);
                if (query.isEmpty()) return appointments;
                return appointments.stream()
                        .filter(a -> {
                            String patientName = String.valueOf(a.get("patient_name")).toLowerCase();
                            String patientPhone = String.valueOf(a.get("patient_phone"));
                            return patientName.contains(query.toLowerCase()) ||
                                    patientPhone.contains(query);
                        })
                        .toList();
            }
        };

        searchTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<Map<String, Object>> filteredAppointments = searchTask.getValue();
                setAppointments(filteredAppointments);
            });
        });

        searchTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Search Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to search appointments. Please try again.");
                alert.showAndWait();
            });
        });

        new Thread(searchTask).start();
    }
}