package com.example.healzone.Doctor;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.SessionManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DoctorHomePageController {
    @FXML
    private BorderPane rootPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private AnchorPane welcomeBar, sideBar, mainContentPane, rootContainer;
    @FXML
    private Label displayName;
    @FXML
    private FontIcon sideBarButton;
    @FXML
    private Button loginButton, dashboardButton, upcomingButton, historyButton, profileButton, logoutButton, addDoctorButton, availabilityButton; // Added availabilityButton

    private static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");
    private boolean sidebarVisible = true;
    private String doctorId;
    private static final double SIDEBAR_WIDTH = 228.0;
    private boolean scrollPaneStyled = false;

    @FXML
    private void initialize() {
        doctorId = SessionManager.getCurrentDoctorId();
        if (doctorId == null) {
            showErrorAlert("Session Error", "No doctor logged in.");
            logout();
            return;
        }

        // Set doctor name - Add null check
        try {
            String firstName = Doctor.getFirstName();
            String lastName = Doctor.getLastName();
            if (firstName != null && lastName != null) {
                displayName.setText("Dr." + firstName + " " + lastName);
            } else {
                displayName.setText("Doctor");
            }
        } catch (Exception e) {
            System.err.println("Error setting doctor name: " + e.getMessage());
            displayName.setText("Doctor");
        }

        // Initialize UI
        constrainWelcomeBar();
        setupInitialScrollPaneStyle();
        setupResponsiveLayout();
        setupScrollPaneBackground();

        // Load dashboard by default with delay to ensure UI is ready
        Platform.runLater(() -> loadDashboard());
        Node focustarget = dashboardButton;
        focustarget.requestFocus();
    }

    // Reused from PatientHomePageController
    private void constrainWelcomeBar() {
        if (welcomeBar != null) {
            welcomeBar.setMaxHeight(110.0);
            welcomeBar.setPrefHeight(110.0);
            welcomeBar.setMinHeight(110.0);
        }
    }

    // Reused from PatientHomePageController
    private void setupInitialScrollPaneStyle() {
        if (rootContainer != null) {
            rootContainer.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            rootContainer.getStyleClass().add("doctorScrollPane");
        }
    }

    // Reused from PatientHomePageController
    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            adjustScrollPaneSize();

            // Listener for sidebar visibility changes
            if (sideBar != null) {
                sideBar.visibleProperty().addListener((obs, oldVal, newVal) -> {
                    sidebarVisible = newVal;
                    Platform.runLater(() -> {
                        adjustScrollPaneSize();
                        if (mainContentPane != null) {
                            mainContentPane.requestLayout();
                        }
                    });
                });
            }

            // Listener for mainContentPane width changes
            if (mainContentPane != null) {
                mainContentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() > 0) {
                        Platform.runLater(this::adjustScrollPaneSize);
                    }
                });
            }

            // Listener for rootPane width changes
            if (rootPane != null) {
                rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() > 0) {
                        Platform.runLater(this::adjustScrollPaneSize);
                    }
                });

                // Listener for scene width changes
                rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        newScene.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                            if (newWidth.doubleValue() > 0) {
                                Platform.runLater(this::adjustScrollPaneSize);
                            }
                        });
                    }
                });
            }
        });
    }

    // Reused from PatientHomePageController
    private void adjustScrollPaneSize() {
        if (rootContainer == null || rootPane == null || mainContentPane == null) return;

        try {
            // Get mainContentPane's width
            double mainContentWidth = mainContentPane.getWidth();
            if (mainContentWidth <= 0) {
                mainContentWidth = rootPane.getWidth() - (sidebarVisible ? SIDEBAR_WIDTH : 0);
            }
            mainContentWidth = Math.max(mainContentWidth, 800);

            // Calculate content width and padding
            double availableWidth = mainContentWidth - 40; // 20px padding on each side
            availableWidth = Math.max(availableWidth, 800);

            // Center content when sidebar is hidden
            double offsetX = sidebarVisible ? 0.0 : (mainContentWidth - availableWidth) / 2.0;
            rootContainer.setLayoutX(offsetX);

            // Update rootContainer dimensions
            rootContainer.setPrefWidth(availableWidth);
            rootContainer.setMaxWidth(availableWidth);
            rootContainer.setMinWidth(500);

            // Ensure visibility
            mainContentPane.setVisible(true);
            rootContainer.setVisible(true);

            // Force layout update
            mainContentPane.requestLayout();
            rootContainer.requestLayout();

            System.out.println("AdjustScrollPaneSize: sidebarVisible=" + sidebarVisible +
                    ", mainContentWidth=" + mainContentWidth +
                    ", availableWidth=" + availableWidth +
                    ", offsetX=" + offsetX +
                    ", containerWidth=" + rootContainer.getWidth());
        } catch (Exception e) {
            System.err.println("Error adjusting container size: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Reused from PatientHomePageController
    private void setupScrollPaneBackground() {
        if (rootContainer == null || scrollPaneStyled) return;

        Platform.runLater(() -> {
            try {
                rootContainer.getStyleClass().add("doctorScrollPane");
                scrollPaneStyled = true;
            } catch (Exception e) {
                System.err.println("Error styling container: " + e.getMessage());
            }
        });
    }

    // Reused from PatientHomePageController
    @FXML
    protected void toggleSideBar() {
        if (sideBar == null) return;

        double width = sideBar.getPrefWidth();
        boolean isSidebarVisible = sideBar.isVisible();

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideBar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), sideBar);

        if (!isSidebarVisible) {
            sideBar.setVisible(true);
            sideBar.setTranslateX(-width);
            slide.setFromX(-width);
            slide.setToX(0);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            sidebarVisible = true;
        } else {
            slide.setFromX(0);
            slide.setToX(-width);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            sidebarVisible = false;
            slide.setOnFinished(event -> sideBar.setVisible(false));
        }

        ParallelTransition animation = new ParallelTransition(slide, fade);
        animation.setOnFinished(event -> {
            Platform.runLater(() -> {
                adjustScrollPaneSize();
                if (rootPane != null) {
                    rootPane.requestLayout();
                }
                System.out.println("Sidebar toggle complete: sidebarVisible=" + sidebarVisible);
            });
        });
        animation.play();
    }

    @FXML
    private void onDashboardButtonClicked() {
        loadDashboard();
        Node focustarget = dashboardButton;
        focustarget.requestFocus();
    }

    private void loadDashboard() {
        System.out.println("Loading dashboard...");
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    System.out.println("Attempting to load FXML: /com/example/healzone/Doctor/DoctorDashboard.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorDashboard.fxml"));

                    if (loader.getLocation() == null) {
                        throw new IOException("FXML file not found: /com/example/healzone/Doctor/DoctorDashboard.fxml");
                    }

                    Parent dashboard = loader.load();
                    DoctorDashboardController controller = loader.getController();
                    if (controller == null) {
                        throw new IOException("Failed to get DoctorDashboardController from FXML");
                    }

                    dashboard.getProperties().put("controller", controller);
                    System.out.println("Dashboard loaded successfully");
                    return dashboard;
                } catch (Exception e) {
                    System.err.println("Error loading dashboard: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Parent dashboard = loadTask.getValue();
                    if (rootContainer != null && dashboard != null) {
                        rootContainer.getChildren().setAll(dashboard);
                        animateContent();

                        Platform.runLater(() -> setDashboardValues());
                    } else {
                        showErrorAlert("Load Error", "Dashboard or container is null");
                    }
                } catch (Exception e) {
                    System.err.println("Error setting dashboard content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set dashboard content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Dashboard load failed: " + errorMessage);
                exception.printStackTrace();
                showErrorAlert("Load Error", "Failed to load dashboard: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    private void setDashboardValues() {
        System.out.println("Setting dashboard values...");

        Task<Map<String, Integer>> statsTask = new Task<>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                try {
                    LocalDate today = LocalDate.now(PAKISTAN_ZONE);
                    Map<String, Integer> stats = new HashMap<>();
                    stats.put("totalBooked", Appointments.getTotalBookedAppointments(doctorId, today));
                    stats.put("attended", Appointments.getAttendedAppointments(doctorId, today));
                    stats.put("remaining", Appointments.getRemainingAppointments(doctorId, today));
                    System.out.println("Stats calculated: " + stats);
                    return stats;
                } catch (Exception e) {
                    System.err.println("Error calculating stats: " + e.getMessage());
                    throw e;
                }
            }
        };

        statsTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Map<String, Integer> stats = statsTask.getValue();
                    if (rootContainer != null && !rootContainer.getChildren().isEmpty()) {
                        Node dashboardNode = rootContainer.getChildren().get(0);
                        if (dashboardNode != null) {
                            DoctorDashboardController controller = (DoctorDashboardController) dashboardNode.getProperties().get("controller");
                            if (controller != null) {
                                controller.setTotalBooked(String.valueOf(stats.get("totalBooked")));
                                controller.setAttended(String.valueOf(stats.get("attended")));
                                controller.setRemaining(String.valueOf(stats.get("remaining")));
                                System.out.println("Dashboard stats set successfully: " + stats);
                            } else {
                                System.err.println("DoctorDashboardController is null");
                            }
                        } else {
                            System.err.println("Dashboard node is null");
                        }
                    } else {
                        System.err.println("Root container is null or empty");
                    }
                } catch (Exception e) {
                    System.err.println("Error setting dashboard values: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        statsTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = statsTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Stats load failed: " + errorMessage);
                showErrorAlert("Stats Error", "Failed to load dashboard stats: " + errorMessage);
            });
        });

        new Thread(statsTask).start();
    }

    @FXML
    private void onUpcomingButtonClicked() {
        Task<List<Map<String, Object>>> loadTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                return Appointments.getUpcomingAppointmentsForDoctor(doctorId);
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    if (rootContainer != null) {
                        rootContainer.getChildren().clear();
                        List<Map<String, Object>> appointments = loadTask.getValue();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorUpcomingAppointments.fxml"));
                        Parent tableView = loader.load();
                        DoctorUpcomingAppointmentsController controller = loader.getController();
                        controller.setAppointments(appointments);
                        rootContainer.getChildren().setAll(tableView);
                        animateContent();
                    }
                } catch (IOException e) {
                    System.err.println("Error loading upcoming appointments table: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to load upcoming appointments: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Upcoming appointments load failed: " + errorMessage);
                showErrorAlert("Load Error", "Failed to load upcoming appointments: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void onHistoryButtonClicked() {
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    System.out.println("Attempting to load FXML: /com/example/healzone/Doctor/DoctorAppointmentHistory.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorAppointmentHistory.fxml"));

                    if (loader.getLocation() == null) {
                        throw new IOException("FXML file not found: /com/example/healzone/Doctor/DoctorAppointmentHistory.fxml");
                    }

                    Parent historyPage = loader.load();
                    DoctorAppointmentHistoryController controller = loader.getController();

                    if (controller == null) {
                        throw new IOException("Failed to get DoctorAppointmentHistoryController from FXML");
                    }

                    List<Map<String, Object>> history = Appointments.getAppointmentHistoryForDoctor(doctorId);
                    controller.setHistoryData(history);

                    historyPage.getProperties().put("controller", controller);
                    System.out.println("History page loaded successfully");
                    return historyPage;
                } catch (Exception e) {
                    System.err.println("Error loading history page: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Parent historyPage = loadTask.getValue();
                    if (rootContainer != null && historyPage != null) {
                        rootContainer.getChildren().setAll(historyPage);
                        animateContent();
                    } else {
                        showErrorAlert("Load Error", "History page or container is null");
                    }
                } catch (Exception e) {
                    System.err.println("Error setting history content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set history content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("History load failed: " + errorMessage);
                showErrorAlert("Load Error", "Failed to load history: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void onProfileButtonClicked() {
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    System.out.println("Attempting to load FXML: /com/example/healzone/Doctor/DoctorProfile.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorProfile.fxml"));
                    Parent profilePage = loader.load();
                    DoctorProfileController controller = loader.getController();

                    if (controller == null) {
                        throw new IOException("Failed to get DoctorProfileController from FXML");
                    }

                    controller.setGovtIdField(Doctor.getGovtID() != null ? Doctor.getGovtID() : "");
                    controller.setFirstNameField(Doctor.getFirstName() != null ? Doctor.getFirstName() : "");
                    controller.setLastNameField(Doctor.getLastName() != null ? Doctor.getLastName() : "");
                    controller.setEmailField(Doctor.getEmail() != null ? Doctor.getEmail() : "");
                    controller.setPhoneNumberField(Doctor.getPhone() != null ? Doctor.getPhone() : "");
                    controller.setSpecializationField(Doctor.getSpecialization() != null ? Doctor.getSpecialization() : "");
                    controller.setLicenseNumberField(Doctor.getMedicalLicenseNumber() != null ? Doctor.getMedicalLicenseNumber() : "");
                    controller.setExperienceField(Doctor.getExperience() != null ? Doctor.getExperience() : "");
                    controller.setDegreesField(Doctor.getDegrees() != null ? Doctor.getDegrees() : "");
                    controller.setBioTextArea(Doctor.getBio() != null ? Doctor.getBio() : "");
                    controller.setHospitalNameField(Doctor.getHospitalName() != null ? Doctor.getHospitalName() : "");
                    controller.setConsultationFeeField(Doctor.getConsultationFee() != null ? Doctor.getConsultationFee() : "");
                    controller.setHospitalAddressArea(Doctor.getHospitalAddress() != null ? Doctor.getHospitalAddress() : "");

                    Map<String, TimeSlot> availability = Doctor.getAvailability();
                    if (availability != null && !availability.isEmpty()) {
                        for (Map.Entry<String, TimeSlot> entry : availability.entrySet()) {
                            String day = entry.getKey();
                            TimeSlot slot = entry.getValue();
//                            controller.addAvailabilityItem(day, slot.getStartTime().toString(), slot.getEndTime().toString());
                        }
                    }

                    profilePage.getProperties().put("controller", controller);
                    System.out.println("Profile page loaded successfully");
                    return profilePage;
                } catch (Exception e) {
                    System.err.println("Error loading profile page: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Parent profilePage = loadTask.getValue();
                    if (rootContainer != null && profilePage != null) {
                        rootContainer.getChildren().setAll(profilePage);
                        animateContent();
                    } else {
                        showErrorAlert("Load Error", "Profile page or container is null");
                    }
                } catch (Exception e) {
                    System.err.println("Error setting profile content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set profile content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Profile load failed: " + errorMessage);
                showErrorAlert("Load Error", "Failed to load profile: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void onAvailabilityButtonClicked() {
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    System.out.println("Attempting to load FXML: /com/example/healzone/Doctor/DoctorAvailability.fxml");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorAvailability.fxml"));
                    Parent availabilityPage = loader.load();
                    DoctorAvailabilityController controller = loader.getController();

                    if (controller == null) {
                        throw new IOException("Failed to get DoctorAvailabilityController from FXML");
                    }

                    // No additional data initialization needed here as loadCurrentAvailability handles it
                    availabilityPage.getProperties().put("controller", controller);
                    System.out.println("Availability page loaded successfully");
                    return availabilityPage;
                } catch (Exception e) {
                    System.err.println("Error loading availability page: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    Parent availabilityPage = loadTask.getValue();
                    if (rootContainer != null && availabilityPage != null) {
                        rootContainer.getChildren().setAll(availabilityPage);
                        animateContent();
                    } else {
                        showErrorAlert("Load Error", "Availability page or container is null");
                    }
                } catch (Exception e) {
                    System.err.println("Error setting availability content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set availability content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Availability load failed: " + errorMessage);
                showErrorAlert("Load Error", "Failed to load availability: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void onLogOutButtonClicked() {
        SessionManager.logOut();
        logout();
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/StartView/MainView.fxml"));
            Parent loginPage = loader.load();
            if (rootPane != null) {
                Scene scene = rootPane.getScene();
                if (scene != null) {
                    scene.setRoot(loginPage);
                    FadeTransition fade = new FadeTransition(Duration.millis(300), loginPage);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                }
            }
        } catch (IOException e) {
            System.err.println("Logout error: " + e.getMessage());
            showErrorAlert("Logout Error", "Failed to load login page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void animateContent() {
        if (rootContainer != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), rootContainer);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}