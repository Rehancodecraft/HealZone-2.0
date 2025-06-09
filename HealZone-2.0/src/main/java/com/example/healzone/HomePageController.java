package com.example.healzone;

import com.example.healzone.DatabaseConnection.Doctors;
import com.example.healzone.Doctor.DoctorCardData;
import com.example.healzone.Doctor.DoctorCardsController;
import com.example.healzone.StartView.MainViewController;
import javafx.animation.*;
import javafx.application.Platform;
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
    private Button loginLink;
    @FXML
    private ScrollPane doctorScrollPane;
    @FXML
    private VBox loadingIndicator;
    @FXML
    private VBox noResultsMessage;
    @FXML
    private FlowPane doctorCardsContainer;

    // State variables
    private boolean sidebarVisible = true;
    private Set<String> loadedDoctorIds = new HashSet<>();
    private boolean scrollPaneStyled = false;
    private Timeline cardAnimationTimeline;

    @FXML
    protected void initialize() {
        displayName.setText(SessionManager.getCurrentUser());
        setupInitialScrollPaneStyle();
        setupResponsiveLayout();
        setupSearchFunctionality();
        constrainWelcomeBar();

        // Delay the background setup to ensure ScrollPane is fully rendered
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                setupScrollPaneBackground();
                loadDoctorCards("");
            });
        });
    }

    private void constrainWelcomeBar() {
        // Ensure welcome bar doesn't expand beyond its intended size
        if (welcomeBar != null) {
            welcomeBar.setMaxHeight(110.0);
            welcomeBar.setPrefHeight(110.0);
            welcomeBar.setMinHeight(110.0);
        }
    }

    private void setupInitialScrollPaneStyle() {
        // Set basic transparent style immediately
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

            // Listen for sidebar visibility changes
            sideBar.visibleProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> {
                    adjustScrollPaneSize();
                    System.out.println("Sidebar visibility changed: " + newVal);
                });
            });

            // Listen for main content pane size changes
            mainContentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    Platform.runLater(this::adjustScrollPaneSize);
                }
            });

            // Listen for root pane size changes
            rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    Platform.runLater(this::adjustScrollPaneSize);
                }
            });

            // Listen for scene size changes
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
        if (doctorScrollPane == null || rootPane == null) return;

        try {
            double sceneWidth = 0;
            double rootWidth = rootPane.getWidth();

            // Try to get scene width if root width is 0
            if (rootWidth <= 0 && rootPane.getScene() != null) {
                sceneWidth = rootPane.getScene().getWidth();
                rootWidth = sceneWidth;
            }

            // If still 0, use a reasonable default
            if (rootWidth <= 0) {
                rootWidth = 1200; // Default width
            }

            // Calculate sidebar width - use 0 when sidebar is hidden
            double sidebarWidth = (sideBar != null && sidebarVisible) ? sideBar.getPrefWidth() : 0;

            // Calculate main content width - this should be the full available space when sidebar is hidden
            double mainContentWidth = rootWidth - sidebarWidth;

            // Ensure we have a reasonable width
            if (mainContentWidth <= 0) {
                mainContentWidth = rootWidth; // Use full width as fallback
            }

            // Calculate available width for scroll pane (minus padding)
            double availableWidth = mainContentWidth - 40; // Reduced padding for better full-screen utilization

            // Ensure minimum width constraints but allow full expansion
            availableWidth = Math.max(availableWidth, 300); // Minimum width

            // When sidebar is hidden, allow full screen utilization
            if (!sidebarVisible) {
                availableWidth = Math.max(availableWidth, rootWidth - 60); // Full width minus small margins
            }

            // Update scroll pane size
            doctorScrollPane.setPrefWidth(availableWidth);
            doctorScrollPane.setMaxWidth(availableWidth);
            doctorScrollPane.setMinWidth(300);

            // Update main content pane to use full available space
            if (mainContentPane != null) {
                mainContentPane.setPrefWidth(mainContentWidth);
                mainContentPane.setMaxWidth(mainContentWidth);
            }

            // Ensure scroll pane is visible and positioned correctly
            if (!doctorScrollPane.isVisible()) {
                doctorScrollPane.setVisible(true);
            }

            // Update scroll pane position for full-screen when sidebar is hidden
            if (!sidebarVisible) {
                doctorScrollPane.setLayoutX(20.0); // Small left margin
            } else {
                doctorScrollPane.setLayoutX(20.0); // Standard left margin
            }

            // Update flow pane wrap length
            if (doctorCardsContainer != null) {
                doctorCardsContainer.setPrefWrapLength(availableWidth - 40);
                doctorCardsContainer.setMaxWidth(availableWidth - 20);

                // Ensure container is visible
                if (!doctorCardsContainer.isVisible()) {
                    doctorCardsContainer.setVisible(true);
                }
            }

            System.out.println("Adjusted scroll pane - Root: " + rootWidth +
                    ", Scene: " + sceneWidth +
                    ", Sidebar: " + sidebarWidth +
                    ", MainContent: " + mainContentWidth +
                    ", Available: " + availableWidth +
                    ", SidebarVisible: " + sidebarVisible +
                    ", ScrollPane Visible: " + doctorScrollPane.isVisible());

        } catch (Exception e) {
            System.err.println("Error adjusting scroll pane size: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupScrollPaneBackground() {
        if (doctorScrollPane == null || scrollPaneStyled) return;

        try {
            // Wait for the ScrollPane to be fully rendered
            Platform.runLater(() -> {
                try {
                    // Apply CSS class for basic styling
                    doctorScrollPane.getStyleClass().add("doctorScrollPane");

                    // Try to style internal components if they exist
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
                    System.out.println("ScrollPane background styling applied successfully");

                } catch (Exception e) {
                    System.err.println("Error styling ScrollPane internals: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error in setupScrollPaneBackground: " + e.getMessage());
        }
    }

    private void setupSearchFunctionality() {
        if (searchBar != null) {
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                // Add a small delay to prevent excessive API calls while typing
                if (cardAnimationTimeline != null) {
                    cardAnimationTimeline.stop();
                }

                cardAnimationTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                    loadDoctorCards(newValue.trim());
                }));
                cardAnimationTimeline.play();
            });
        }
    }

    @FXML
    protected void toggleSideBar() {
        boolean isSidebarVisible = sideBar.isVisible();
        double width = sideBar.getPrefWidth();

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideBar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), sideBar);

        if (!isSidebarVisible) {
            // Prepare for entry from left
            sideBar.setVisible(true);
            sideBar.setTranslateX(-width);
            slide.setFromX(-width);
            slide.setToX(0);

            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            sidebarVisible = true; // Update state immediately when showing

            // Adjust scroll pane size immediately when showing sidebar
            Platform.runLater(this::adjustScrollPaneSize);
        } else {
            // Slide out to left
            slide.setFromX(0);
            slide.setToX(-width);

            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            sidebarVisible = false; // Update state immediately when hiding

            // Adjust scroll pane size immediately when hiding sidebar
            Platform.runLater(this::adjustScrollPaneSize);

            slide.setOnFinished(event -> {
                sideBar.setVisible(false);
            });
        }

        ParallelTransition animation = new ParallelTransition(slide, fade);
        animation.setOnFinished(event -> {
            // Final adjustment after animation completes
            Platform.runLater(() -> {
                Platform.runLater(this::adjustScrollPaneSize);
            });
        });
        animation.play();
    }

    @FXML
    protected void onLogOutButtonClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/StartView/MainView.fxml"));
        Parent mainView = loader.load();

        MainViewController mainController = loader.getController();
        mainView.getProperties().put("controller", mainController);

        mainView.setOpacity(0);
        mainView.setScaleX(0.98);
        mainView.setScaleY(0.98);

        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(mainView);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), mainView);
        scaleIn.setFromX(0.98);
        scaleIn.setToX(1);
        scaleIn.setFromY(0.98);
        scaleIn.setToY(1);

        ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
        transition.play();
    }

    private void loadDoctorCards(String searchTerm) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }
        if (noResultsMessage != null) {
            noResultsMessage.setVisible(false);
        }

        doctorCardsContainer.getChildren().clear();
        loadedDoctorIds.clear();

        try {
            // Load doctor data
            List<DoctorCardData> doctorList = Doctors.loadDoctorCards(searchTerm);
            doctorList = removeDuplicates(doctorList);

            // If no results found, load random ones
            if (doctorList.isEmpty() && !searchTerm.isEmpty()) {
                doctorList = Doctors.loadDoctorCards("");
                doctorList = removeDuplicates(doctorList);
                Collections.shuffle(doctorList);
                if (doctorList.size() > 5) {
                    doctorList = doctorList.subList(0, 5);
                }
            }

            // Hide loading indicator
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }

            if (doctorList.isEmpty()) {
                if (noResultsMessage != null) {
                    noResultsMessage.setVisible(true);
                }
                return;
            }

            for (DoctorCardData data : doctorList) {
                try {
                    String doctorId = data.getGovtId();
                    if (loadedDoctorIds.contains(doctorId)) continue;

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorCards.fxml"));
                    VBox card = loader.load();
                    DoctorCardsController controller = loader.getController();
                    controller.setDoctorData(data);
                    card.setOpacity(0.95); // almost visible initially
                    card.setScaleX(0.99);  // minimal scale difference
                    card.setScaleY(0.99);

                    doctorCardsContainer.getChildren().add(card);
                    loadedDoctorIds.add(doctorId);

// Subtle fade + scale transition
                    FadeTransition fade = new FadeTransition(Duration.millis(150), card);
                    fade.setFromValue(0.95);
                    fade.setToValue(1.0);

                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), card);
                    scale.setFromX(0.99);
                    scale.setToX(1.0);
                    scale.setFromY(0.99);
                    scale.setToY(1.0);

                    new ParallelTransition(fade, scale).play();

                } catch (IOException e) {
                    System.err.println("Error loading doctor card: " + e.getMessage());
                }
            }

            Platform.runLater(this::adjustScrollPaneSize);

        } catch (Exception e) {
            System.err.println("Error loading doctor cards: " + e.getMessage());
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
        }
    }



    private void animateCardsOut(Runnable onComplete) {
        if (doctorCardsContainer.getChildren().isEmpty()) {
            onComplete.run();
            return;
        }

        List<Node> cards = new ArrayList<>(doctorCardsContainer.getChildren());
        ParallelTransition parallelTransition = new ParallelTransition();

        for (int i = 0; i < cards.size(); i++) {
            Node card = cards.get(i);

            FadeTransition fade = new FadeTransition(Duration.millis(10), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(10), card);
            scale.setFromX(1.0);
            scale.setToX(0.8);
            scale.setFromY(1.0);
            scale.setToY(0.8);

            // Add slight delay for staggered effect
            fade.setDelay(Duration.millis(i * 10));
            scale.setDelay(Duration.millis(i * 10));

            parallelTransition.getChildren().addAll(fade, scale);
        }

        parallelTransition.setOnFinished(e -> onComplete.run());
        parallelTransition.play();
    }

    private void animateCardsIn(List<VBox> cards) {
        if (cards.isEmpty()) return;

        for (int i = 0; i < cards.size(); i++) {
            VBox card = cards.get(i);

            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
            scale.setFromX(0.8);
            scale.setToX(1.0);
            scale.setFromY(0.8);
            scale.setToY(1.0);

            TranslateTransition translate = new TranslateTransition(Duration.millis(400), card);
            translate.setFromY(20);
            translate.setToY(0);

            // Add staggered delay for smooth appearance
            Duration delay = Duration.millis(i * 100);
            fade.setDelay(delay);
            scale.setDelay(delay);
            translate.setDelay(delay);

            // Set easing for smooth animation
            fade.setInterpolator(Interpolator.EASE_OUT);
            scale.setInterpolator(Interpolator.EASE_OUT);
            translate.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition cardAnimation = new ParallelTransition(fade, scale, translate);
            cardAnimation.play();
        }
    }

    private List<DoctorCardData> removeDuplicates(List<DoctorCardData> doctorList) {
        Set<String> seenIds = new HashSet<>();
        doctorList.removeIf(doctor -> {
            String doctorId = doctor.getGovtId();
            return !seenIds.add(doctorId);
        });
        return doctorList;
    }
}