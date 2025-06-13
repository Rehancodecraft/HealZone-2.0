package com.example.healzone;

import com.example.healzone.DatabaseConnection.Appointments;
import com.example.healzone.DatabaseConnection.Doctors;
import com.example.healzone.DatabaseConnection.Patients;
import com.example.healzone.Doctor.DoctorCardData;
import com.example.healzone.Doctor.DoctorCardsController;
import com.example.healzone.Patient.Patient;
import com.example.healzone.Patient.UpcomingAppointmentController;
import com.example.healzone.Patient.UpcomingAppointmentModel;
import com.example.healzone.StartView.MainViewController;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.*;

public class HomePageController {
    @FXML
    private BorderPane rootPane;
    @FXML
    private AnchorPane sideBar;
    @FXML
    private AnchorPane mainContentPane;
    @FXML
    private AnchorPane welcomeBar;
    @FXML
    private Label displayName;
    @FXML
    private TextField searchBar;
    @FXML
    private Button loginButton;
    @FXML
    private ScrollPane doctorScrollPane;
    @FXML
    private VBox loadingIndicator;
    @FXML
    private VBox noResultsMessage;
    @FXML
    private FlowPane doctorCardsContainer;
    @FXML
    private Button upcomingButton;
    @FXML
    private Label sectionTitle;
    @FXML
    private Button filterButton;
    @FXML
    private Button sortButton;
    @FXML
    private Button backButton;

    // State variables
    private boolean sidebarVisible = true;
    private Set<String> loadedDoctorIds = new HashSet<>();
    private boolean scrollPaneStyled = false;
    private Timeline cardAnimationTimeline;
    private boolean showingAppointments = false;
    private boolean isLoadingMore = false;
    private static final int INITIAL_CARD_LIMIT = 10; // Load 10 cards initially
    private static final int LOAD_MORE_LIMIT = 5; // Load 5 more cards on scroll

    @FXML
    protected void initialize() {
        displayName.setText(SessionManager.getCurrentUser());
        setupInitialScrollPaneStyle();
        setupResponsiveLayout();
        setupSearchFunctionality();
        setupScrollListener();
        constrainWelcomeBar();

        Platform.runLater(() -> {
            setupScrollPaneBackground();
            loadDoctorCardsAsync("", INITIAL_CARD_LIMIT);
        });
    }

    private void constrainWelcomeBar() {
        if (welcomeBar != null) {
            welcomeBar.setMaxHeight(110.0);
            welcomeBar.setPrefHeight(110.0);
            welcomeBar.setMinHeight(110.0);
        }
    }

    private void setupInitialScrollPaneStyle() {
        if (doctorScrollPane != null) {
            doctorScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            doctorScrollPane.getStyleClass().add("doctorScrollPane");
        }

        if (doctorCardsContainer != null) {
            doctorCardsContainer.setStyle("-fx-background-color: transparent;");
        }
    }

    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            adjustScrollPaneSize();

            sideBar.visibleProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(this::adjustScrollPaneSize);
            });

            mainContentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    Platform.runLater(this::adjustScrollPaneSize);
                }
            });

            rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    Platform.runLater(this::adjustScrollPaneSize);
                }
            });

            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.widthProperty().addListener((obsWidth, oldWidth, newWidth) -> {
                        if (newWidth.doubleValue() > 0) {
                            Platform.runLater(this::adjustScrollPaneSize);
                        }
                    });
                }
            });
        });
    }

    private void adjustScrollPaneSize() {
        if (doctorScrollPane == null || rootPane == null || mainContentPane == null) return;

        try {
            double rootWidth = rootPane.getWidth();
            if (rootWidth <= 0 && rootPane.getScene() != null) {
                rootWidth = rootPane.getScene().getWidth();
            }
            if (rootWidth <= 0) {
                rootWidth = 1200;
            }

            double sidebarWidth = (sideBar != null && sidebarVisible) ? sideBar.getPrefWidth() : 0;
            double mainContentWidth = rootWidth - sidebarWidth;
            mainContentWidth = Math.max(mainContentWidth, 300);

            double availableWidth = mainContentWidth - 40;
            availableWidth = Math.max(availableWidth, 300);

            mainContentPane.setPrefWidth(mainContentWidth);
            mainContentPane.setMaxWidth(mainContentWidth);
            mainContentPane.setMinWidth(300);
            mainContentPane.setVisible(true);

            doctorScrollPane.setPrefWidth(availableWidth);
            doctorScrollPane.setMaxWidth(availableWidth);
            doctorScrollPane.setMinWidth(300);
            doctorScrollPane.setVisible(true);
            doctorScrollPane.setLayoutX(20.0);

            if (doctorCardsContainer != null) {
                doctorCardsContainer.setPrefWrapLength(availableWidth - 40);
                doctorCardsContainer.setMaxWidth(availableWidth - 20);
                doctorCardsContainer.setVisible(true);
            }

            System.out.println("AdjustScrollPaneSize: sidebarVisible=" + sidebarVisible +
                    ", mainContentWidth=" + mainContentWidth +
                    ", availableWidth=" + availableWidth);
        } catch (Exception e) {
            System.err.println("Error adjusting scroll pane size: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupScrollPaneBackground() {
        if (doctorScrollPane == null || scrollPaneStyled) return;

        Platform.runLater(() -> {
            try {
                doctorScrollPane.getStyleClass().add("doctorScrollPane");
                Node viewport = doctorScrollPane.lookup(".viewport");
                if (viewport != null) {
                    viewport.setStyle("-fx-background-color: transparent;");
                }

                Node scrollBarVertical = doctorScrollPane.lookup(".scroll-bar:vertical");
                if (scrollBarVertical != null) {
                    scrollBarVertical.setStyle(
                            "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                    "-fx-background-radius: 10px; " +
                                    "-fx-border-radius: 10px;"
                    );
                }

                Node scrollBarHorizontal = doctorScrollPane.lookup(".scroll-bar:horizontal");
                if (scrollBarHorizontal != null) {
                    scrollBarHorizontal.setStyle(
                            "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                                    "-fx-background-radius: 10px; " +
                                    "-fx-border-radius: 10px;"
                    );
                }

                scrollPaneStyled = true;
            } catch (Exception e) {
                System.err.println("Error styling ScrollPane internals: " + e.getMessage());
            }
        });
    }

    private void setupSearchFunctionality() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (cardAnimationTimeline != null) {
                    cardAnimationTimeline.stop();
                }

                cardAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                    if (!showingAppointments) {
                        System.out.println("Search triggered: " + newValue.trim());
                        loadDoctorCardsAsync(newValue.trim(), INITIAL_CARD_LIMIT);
                    }
                }));
                cardAnimationTimeline.play();
            });
        }
    }

    private void setupScrollListener() {
        doctorScrollPane.vvalueProperty().addListener((obs, oldValue, newValue) -> {
            if (!isLoadingMore && !showingAppointments && newValue.doubleValue() > 0.9) { // Prevent loading on appointments page
                isLoadingMore = true;
                System.out.println("Scroll triggered: Loading more doctor cards");
                loadDoctorCardsAsync(searchBar.getText().trim(), LOAD_MORE_LIMIT, true);
            }
        });
    }

    private void loadDoctorCardsAsync(String searchTerm, int limit) {
        loadDoctorCardsAsync(searchTerm, limit, false);
    }

    private void loadDoctorCardsAsync(String searchTerm, int limit, boolean append) {
        if (showingAppointments) {
            System.out.println("loadDoctorCardsAsync skipped: showingAppointments=true");
            return; // Prevent loading doctor cards on appointments page
        }

        System.out.println("loadDoctorCardsAsync: searchTerm=" + searchTerm + ", limit=" + limit + ", append=" + append);

        if (loadingIndicator != null) {
            Platform.runLater(() -> loadingIndicator.setVisible(true));
        }
        if (noResultsMessage != null) {
            Platform.runLater(() -> noResultsMessage.setVisible(false));
        }

        Task<List<VBox>> loadTask = new Task<>() {
            @Override
            protected List<VBox> call() throws Exception {
                List<VBox> cardNodes = new ArrayList<>();
                List<DoctorCardData> doctorList = Doctors.loadDoctorCards(searchTerm, limit);
                doctorList = removeDuplicates(doctorList);

                if (doctorList.isEmpty() && !searchTerm.isEmpty()) {
                    doctorList = Doctors.loadDoctorCards("", limit);
                    doctorList = removeDuplicates(doctorList);
                }

                for (DoctorCardData data : doctorList) {
                    String doctorId = data.getGovtId();
                    if (loadedDoctorIds.contains(doctorId)) continue;

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorCards.fxml"));
                    VBox card = loader.load();
                    DoctorCardsController controller = loader.getController();
                    controller.setDoctorData(data);
                    card.setOpacity(0.95);
                    card.setScaleX(0.99);
                    card.setScaleY(0.99);
                    cardNodes.add(card);
                    loadedDoctorIds.add(doctorId);
                }

                return cardNodes;
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<VBox> cardNodes = loadTask.getValue();
            Platform.runLater(() -> {
                if (!append) {
                    doctorCardsContainer.getChildren().clear();
                    loadedDoctorIds.clear();
                }

                if (cardNodes.isEmpty()) {
                    noResultsMessage.setVisible(true);
                    loadingIndicator.setVisible(false);
                    isLoadingMore = false;
                    return;
                }

                for (VBox card : cardNodes) {
                    doctorCardsContainer.getChildren().add(card);

                    FadeTransition fade = new FadeTransition(Duration.millis(150), card);
                    fade.setFromValue(0.95);
                    fade.setToValue(1.0);

                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
                    scale.setFromX(0.99);
                    scale.setToX(1.0);
                    scale.setFromY(0.99);
                    scale.setToY(1.0);

                    new ParallelTransition(fade, scale).play();
                }

                loadingIndicator.setVisible(false);
                isLoadingMore = false;
                adjustScrollPaneSize();
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Error loading doctor cards: " + loadTask.getException().getMessage());
                loadingIndicator.setVisible(false);
                isLoadingMore = false;
            });
        });

        new Thread(loadTask).start();
    }

    @FXML
    protected void toggleSideBar() {
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
            Platform.runLater(this::adjustScrollPaneSize);
            if (mainContentPane != null) {
                mainContentPane.setVisible(true);
            }
            if (doctorScrollPane != null) {
                doctorScrollPane.setVisible(true);
            }
        });
        animation.play();
    }

    @FXML
    protected void onLogOutButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/StartView/MainView.fxml"));
        Parent mainView = loader.load();
        MainViewController mainController = loader.getController();
        mainView.getProperties().put("controller", mainView);

        mainView.setVisible(true);
        mainView.setOpacity(0);
        mainView.setScaleX(0.98);
        mainView.setScaleY(0.98);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainView);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), mainView);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), mainView);
        scaleTransition.setFromX(0.98);
        scaleTransition.setToX(1.0);
        scaleTransition.setFromY(0.98);
        scaleTransition.setToY(1.0);

        ParallelTransition transition = new ParallelTransition(fadeTransition, scaleTransition);
        transition.play();
    }

    @FXML
    protected void onUpcomingButtonClicked() {
        if (showingAppointments) {
            return;
        }

        System.out.println("Switching to upcoming appointments");
        showingAppointments = true;
        filterButton.setVisible(false);
        sortButton.setVisible(false);
        sectionTitle.setText("Upcoming Appointments");
        backButton.setVisible(true);
        searchBar.setVisible(false);

        doctorCardsContainer.getChildren().clear();
        noResultsMessage.setVisible(false);
        loadUpcomingAppointments();
    }

    @FXML
    protected void onBackButtonClicked() {
        System.out.println("Switching back to doctor cards");
        showingAppointments = false;
        doctorCardsContainer.setVisible(true);
        filterButton.setVisible(true);
        sortButton.setVisible(true);
        sectionTitle.setText("Available Doctors");
        backButton.setVisible(false);
        doctorCardsContainer.getChildren().clear();
        searchBar.setVisible(true);
        loadDoctorCardsAsync("", INITIAL_CARD_LIMIT);
    }

    @FXML
    protected void onDashboardButtonClicked() {
        System.out.println("Switching back to dashboard");
        showingAppointments = false;
        doctorCardsContainer.setVisible(true);
        filterButton.setVisible(true);
        sortButton.setVisible(true);
        sectionTitle.setText("Available Doctors");
        backButton.setVisible(false);
        searchBar.setVisible(true);
        doctorCardsContainer.getChildren().clear();
        loadDoctorCardsAsync("", INITIAL_CARD_LIMIT);
    }

    private void loadUpcomingAppointments() {
        System.out.println("Loading upcoming appointments");
        List<UpcomingAppointmentModel> appointments = Appointments.getUpcomingAppointmentsForPatient(Patient.getPhone());

        loadingIndicator.setVisible(true);
        noResultsMessage.setVisible(false);
        doctorCardsContainer.getChildren().clear();
        doctorCardsContainer.setVisible(true);

        if (appointments.isEmpty()) {
            loadingIndicator.setVisible(false);
            noResultsMessage.setVisible(true);
            noResultsMessage.getChildren().setAll(
                    new FontIcon("fas fa-calendar-alt:60"),
                    new Label("No upcoming appointments") {{
                        setText("No upcoming appointments");
                        setTextFill(javafx.scene.paint.Color.WHITE);
                        setFont(new javafx.scene.text.Font("SansSerif Bold", 20.0));
                    }},
                    new Label("Book an appointment to get started") {{
                        setTextFill(javafx.scene.paint.Color.WHITE);
                        setFont(new javafx.scene.text.Font(14.0));
                    }}
            );
        } else {
            for (UpcomingAppointmentModel appt : appointments) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/UpcomingAppointments.fxml"));
                    VBox card = loader.load();
                    UpcomingAppointmentController controller = loader.getController();
                    controller.initializeReceipt(appt);

                    card.setVisible(true);
                    card.setOpacity(1.0);
                    card.setScaleX(1.0);
                    card.setScaleY(1.0);

                    doctorCardsContainer.getChildren().add(card);

                    FadeTransition fade = new FadeTransition(Duration.millis(150), card);
                    fade.setFromValue(0.95);
                    fade.setToValue(1.0);

                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
                    scale.setToX(0.99);
                    scale.setToX(1.0);
                    scale.setToY(0.99);
                    scale.setToY(1.0);

                    new ParallelTransition(fade, scale).play();
                } catch (IOException e) {
                    System.err.println("Error loading appointment card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            loadingIndicator.setVisible(false);
        }

        Platform.runLater(this::adjustScrollPaneSize);
    }

    private List<DoctorCardData> removeDuplicates(List<DoctorCardData> doctorList) {
        Set<String> seenIds = new HashSet<>();
        doctorList.removeIf(doctor -> !seenIds.add(doctor.getGovtId()));
        return doctorList;
    }
}