package com.example.healzone.controller.patient;

import com.example.healzone.model.UpcomingAppointmentModel;
import com.example.healzone.repository.AppointmentRepository;
import com.example.healzone.repository.DoctorRepository;
import com.example.healzone.model.DoctorCardDataModel;
import com.example.healzone.controller.doctor.DoctorCardsController;
import com.example.healzone.controller.shared.MainViewController;
import com.example.healzone.model.PatientModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PatientHomePageController {
    private static PatientHomePageController instance; // Static instance for access

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
    private Button searchButton;
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
    private Button profileButton;
    @FXML
    private Button dashboardButton; // Add dashboard button reference

    // State variables
    private boolean sidebarVisible = true;
    private Set<String> loadedDoctorIds = new HashSet<>();
    private boolean scrollPaneStyled = false;
    private Timeline cardAnimationTimeline;
    private String currentView = "dashboard"; // Track current view: dashboard, appointments, profile
    private boolean isLoadingMore = false;
    private static final int INITIAL_CARD_LIMIT = 10;
    private static final int LOAD_MORE_LIMIT = 5;
    private static final double SIDEBAR_WIDTH = 228.0;
    private String currentSortType = "name"; // Default sort by name
    private boolean isAscending = true; // Default ascending order
    private String currentSpecialtyFilter = "All"; // Default no specialty filter
    private String currentLocationFilter = "All"; // Default no location filter
    private double minRatingFilter = 0.0; // Default minimum rating
    private List<DoctorCardDataModel> allDoctors = new ArrayList<>();
    private static final int SEARCH_BATCH_SIZE = 20; // Load in batches for better performance
    private static final int MAX_SEARCH_RESULTS = 100; // Limit total results
    private boolean isSearching = false;
    private String lastSearchTerm = "";
    private List<String> availableLocations = new ArrayList<>();
    private boolean locationsLoaded = false;

    @FXML
    protected void initialize() {
        instance = this; // Set the instance
        displayName.setText(PatientModel.getName());
        setupInitialScrollPaneStyle();
        setupResponsiveLayout();
        setupSearchFunctionality();
        setupScrollListener();
        constrainWelcomeBar();

        Platform.runLater(() -> {
            setupScrollPaneBackground();
            loadDashboard(); // Load dashboard by default
        });
    }

    private void loadInitialDoctorCards() {
        System.out.println("Loading initial doctor cards...");

        if (loadingIndicator != null) {
            Platform.runLater(() -> loadingIndicator.setVisible(true));
        }

        Task<List<DoctorCardDataModel>> loadTask = new Task<>() {
            @Override
            protected List<DoctorCardDataModel> call() throws Exception {
                // Load random doctors for initial display
                return DoctorRepository.loadDoctorCards("", INITIAL_CARD_LIMIT);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<DoctorCardDataModel> doctors = loadTask.getValue();
            Platform.runLater(() -> {
                doctorCardsContainer.getChildren().clear();
                loadedDoctorIds.clear();

                if (doctors.isEmpty()) {
                    noResultsMessage.setVisible(true);
                    loadingIndicator.setVisible(false);
                    return;
                }

                // Create cards for initial doctors
                for (DoctorCardDataModel data : doctors) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorCards.fxml"));
                        VBox card = loader.load();
                        DoctorCardsController controller = loader.getController();
                        controller.setDoctorData(data);

                        loadedDoctorIds.add(data.getGovtId());
                        doctorCardsContainer.getChildren().add(card);

                        // Add smooth animation
                        card.setOpacity(0);
                        FadeTransition fade = new FadeTransition(Duration.millis(200), card);
                        fade.setFromValue(0);
                        fade.setToValue(1);
                        fade.play();

                    } catch (IOException e) {
                        System.err.println("Error creating initial card: " + e.getMessage());
                    }
                }

                loadingIndicator.setVisible(false);
                adjustScrollPaneSize();
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Error loading initial cards: " + loadTask.getException().getMessage());
                loadingIndicator.setVisible(false);
                showErrorAlert("Load Error", "Failed to load initial doctor cards.");
            });
        });

        new Thread(loadTask).start();
    }
    @FXML
    protected void onFilterButtonClicked() {
        showFilterDialog();
    }

    @FXML
    protected void onSortButtonClicked() {
        showSortDialog();
    }
    private void showFilterDialog() {
        // Load locations asynchronously if not already loaded
        loadAvailableLocationsAsync();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Filter Doctors");
        dialog.setHeaderText("Select filter criteria");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Specialty filter
        Label specialtyLabel = new Label("Specialty:");
        ComboBox<String> specialtyCombo = new ComboBox<>();
        specialtyCombo.getItems().addAll("All", "Cardiology", "Dermatology", "Neurology",
                "Orthopedics", "Pediatrics", "Psychiatry", "General Medicine",
                "Gynecology", "ENT", "Ophthalmology", "Radiology", "Pathology");
        specialtyCombo.setValue(currentSpecialtyFilter);

        // Location filter
        Label locationLabel = new Label("Location:");
        ComboBox<String> locationCombo = new ComboBox<>();
        locationCombo.getItems().addAll(availableLocations);
        locationCombo.setValue(currentLocationFilter);

        // Rating filter
        Label ratingLabel = new Label("Minimum Rating:");
        Slider ratingSlider = new Slider(0, 5, minRatingFilter);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setBlockIncrement(0.5);
        Label ratingValueLabel = new Label(String.format("%.1f", minRatingFilter));

        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        HBox ratingBox = new HBox(10, ratingSlider, ratingValueLabel);

        content.getChildren().addAll(
                specialtyLabel, specialtyCombo,
                locationLabel, locationCombo,
                ratingLabel, ratingBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentSpecialtyFilter = specialtyCombo.getValue();
            currentLocationFilter = locationCombo.getValue();
            minRatingFilter = ratingSlider.getValue();

            // Apply filters immediately
            applyFiltersAndSort();
        }
    }


    private void showSortDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sort Doctors");
        dialog.setHeaderText("Select sorting criteria");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Sort type
        Label sortLabel = new Label("Sort by:");
        ComboBox<String> sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Name", "Rating", "Experience", "Fee");
        sortCombo.setValue(capitalizeFirst(currentSortType));

        // Sort order
        Label orderLabel = new Label("Order:");
        ComboBox<String> orderCombo = new ComboBox<>();
        orderCombo.getItems().addAll("Ascending", "Descending");
        orderCombo.setValue(isAscending ? "Ascending" : "Descending");

        content.getChildren().addAll(sortLabel, sortCombo, orderLabel, orderCombo);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentSortType = sortCombo.getValue().toLowerCase();
            isAscending = "Ascending".equals(orderCombo.getValue());

            // Apply sorting immediately
            applyFiltersAndSort();
        }
    }

    private void applyFiltersAndSort() {
        if (!"dashboard".equals(currentView)) {
            return;
        }

        // Trigger search with current search term to apply filters and sorting
        String currentSearchTerm = searchBar.getText().trim();
        performSearch(currentSearchTerm);
    }
    private void clearSearchResults() {
        Platform.runLater(() -> {
            doctorCardsContainer.getChildren().clear();
            loadedDoctorIds.clear();
            lastSearchTerm = "";
            isSearching = false;

            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
            if (noResultsMessage != null) {
                noResultsMessage.setVisible(false);
            }
        });
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    public static PatientHomePageController getInstance() {
        return instance;
    }

    public void updateDisplayName(String newName) {
        Platform.runLater(() -> {
            displayName.setText(newName != null ? newName : "");
            if ("profile".equals(currentView)) {
                reloadProfile(); // Optional: Reload profile with updated data
            }
        });
    }

    private void reloadProfile() {
        onProfileButtonClicked(null); // Trigger profile reload
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
            doctorScrollPane.setFitToWidth(true);
        }

        if (doctorCardsContainer != null) {
            doctorCardsContainer.setStyle("-fx-background-color: transparent;");
        }
    }

    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            adjustScrollPaneSize();

            sideBar.visibleProperty().addListener((obs, oldVal, newVal) -> {
                sidebarVisible = newVal;
                Platform.runLater(() -> {
                    adjustScrollPaneSize();
                    mainContentPane.requestLayout();
                });
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
            double mainContentWidth = mainContentPane.getWidth();
            if (mainContentWidth <= 0) {
                mainContentWidth = rootPane.getWidth() - (sidebarVisible ? SIDEBAR_WIDTH : 0);
            }
            mainContentWidth = Math.max(mainContentWidth, 800);

            double availableWidth = mainContentWidth - 40;
            availableWidth = Math.max(availableWidth, 800);

            double offsetX = sidebarVisible ? 0.0 : (mainContentWidth - availableWidth) / 2.0;
            doctorScrollPane.setLayoutX(offsetX);

            doctorScrollPane.setPrefWidth(availableWidth);
            doctorScrollPane.setMaxWidth(availableWidth);
            doctorScrollPane.setMinWidth(500);

            if (doctorCardsContainer != null) {
                doctorCardsContainer.setPrefWrapLength(availableWidth - 40);
                doctorCardsContainer.setMaxWidth(availableWidth - 40);
            }

            mainContentPane.setVisible(true);
            doctorScrollPane.setVisible(true);

            mainContentPane.requestLayout();
            doctorScrollPane.requestLayout();

            System.out.println("AdjustScrollPaneSize: sidebarVisible=" + sidebarVisible +
                    ", mainContentWidth=" + mainContentWidth +
                    ", availableWidth=" + availableWidth +
                    ", offsetX=" + offsetX);
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
                    viewport.setStyle("-fx-background-color: 0px transparent;");
                }

                Node scrollBar = doctorScrollPane.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.setStyle(
                            "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                                    "-fx-background-radius: 10px;" +
                                    "-fx-border-radius: 10px;"
                    );
                }

                Node scrollBarHorizontal = doctorScrollPane.lookup(".scroll-bar:horizontal");
                if (scrollBarHorizontal != null) {
                    scrollBarHorizontal.setStyle(
                            "-fx-background-color: rgba(255, 255, 255, 0.1);" +
                                    "-fx-background-radius: 10px;" +
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
                    if ("dashboard".equals(currentView)) {
                        System.out.println("Search triggered: " + newValue.trim());
                        performSearch(newValue.trim());
                    }
                }));
                cardAnimationTimeline.play();
            });
        }
    }
    private void performSearch(String searchTerm) {
        if (isSearching) {
            return;
        }

        isSearching = true;
        lastSearchTerm = searchTerm;

        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
            if (noResultsMessage != null) {
                noResultsMessage.setVisible(false);
            }
        });

        Task<List<DoctorCardDataModel>> searchTask = new Task<>() {
            @Override
            protected List<DoctorCardDataModel> call() throws Exception {
                List<DoctorCardDataModel> results;

                if (searchTerm.isEmpty()) {
                    // If search is empty, load all doctors
                    results = DoctorRepository.loadDoctorCards("", MAX_SEARCH_RESULTS);
                } else {
                    // Search with the term
                    results = DoctorRepository.loadDoctorCards(searchTerm, MAX_SEARCH_RESULTS);
                }

                // Apply filters
                results = applyCurrentFilters(results);

                // Apply sorting
                results = applyCurrentSorting(results);

                return results;
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<DoctorCardDataModel> searchResults = searchTask.getValue();
            Platform.runLater(() -> {
                doctorCardsContainer.getChildren().clear();
                loadedDoctorIds.clear();

                if (searchResults.isEmpty()) {
                    noResultsMessage.setVisible(true);
                    loadingIndicator.setVisible(false);
                    isSearching = false;
                    return;
                }

                // Create cards for search results
                for (DoctorCardDataModel data : searchResults) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorCards.fxml"));
                        VBox card = loader.load();
                        DoctorCardsController controller = loader.getController();
                        controller.setDoctorData(data);

                        loadedDoctorIds.add(data.getGovtId());
                        doctorCardsContainer.getChildren().add(card);

                        // Add animation
                        card.setOpacity(0);
                        FadeTransition fade = new FadeTransition(Duration.millis(200), card);
                        fade.setFromValue(0);
                        fade.setToValue(1);
                        fade.play();

                    } catch (IOException e) {
                        System.err.println("Error creating search result card: " + e.getMessage());
                    }
                }

                loadingIndicator.setVisible(false);
                adjustScrollPaneSize();
                isSearching = false;
            });
        });

        searchTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                System.err.println("Search failed: " + searchTask.getException().getMessage());
                loadingIndicator.setVisible(false);
                isSearching = false;
            });
        });

        new Thread(searchTask).start();
    }
    private void createCardsInBatches(List<DoctorCardDataModel> doctors, int startIndex) {
        if (startIndex >= doctors.size()) {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                if (doctorCardsContainer.getChildren().isEmpty()) {
                    noResultsMessage.setVisible(true);
                }
                adjustScrollPaneSize();
            });
            return;
        }

        Task<List<VBox>> batchTask = new Task<>() {
            @Override
            protected List<VBox> call() throws Exception {
                List<VBox> cardNodes = new ArrayList<>();
                int endIndex = Math.min(startIndex + SEARCH_BATCH_SIZE, doctors.size());

                for (int i = startIndex; i < endIndex; i++) {
                    DoctorCardDataModel data = doctors.get(i);

                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Doctor/DoctorCards.fxml"));
                            VBox card = loader.load();
                            DoctorCardsController controller = loader.getController();
                            controller.setDoctorData(data);

                            // Add card with animation
                            doctorCardsContainer.getChildren().add(card);

                            // Smooth animation
                            card.setOpacity(0);
                            FadeTransition fade = new FadeTransition(Duration.millis(200), card);
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            fade.play();

                        } catch (IOException e) {
                            System.err.println("Error creating card: " + e.getMessage());
                        }
                    });
                }

                return cardNodes; // Not used but required for Task
            }
        };

        batchTask.setOnSucceeded(event -> {
            // Schedule next batch
            Timeline nextBatch = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                createCardsInBatches(doctors, startIndex + SEARCH_BATCH_SIZE);
            }));
            nextBatch.play();
        });

        batchTask.setOnFailed(event -> {
            System.err.println("Batch creation failed: " + batchTask.getException().getMessage());
            createCardsInBatches(doctors, startIndex + SEARCH_BATCH_SIZE); // Continue with next batch
        });

        new Thread(batchTask).start();
    }

    private List<DoctorCardDataModel> applyCurrentSorting(List<DoctorCardDataModel> doctors) {
        doctors.sort((d1, d2) -> {
            int comparison = 0;

            switch (currentSortType) {
                case "name":
                    comparison = d1.getFullName().compareToIgnoreCase(d2.getFullName());
                    break;
                case "rating":
                    comparison = Double.compare(d1.getAverageRating(), d2.getAverageRating());
                    break;
                case "experience":
                    comparison = d1.getExperience().compareTo(d2.getExperience());
                    break;
                case "fee":
                    comparison = d1.getConsultationFee().compareTo(d2.getConsultationFee());
                    break;
                default:
                    comparison = d1.getFullName().compareToIgnoreCase(d2.getFullName());
            }

            return isAscending ? comparison : -comparison;
        });

        return doctors;
    }
    private List<DoctorCardDataModel> applyCurrentFilters(List<DoctorCardDataModel> doctors) {
        return doctors.stream()
                .filter(doctor -> {
                    // Specialty filter
                    if (!"All".equals(currentSpecialtyFilter)) {
                        if (doctor.getSpecialization() == null ||
                                !doctor.getSpecialization().equalsIgnoreCase(currentSpecialtyFilter)) {
                            return false;
                        }
                    }

                    // Enhanced Location filter - check multiple location fields
                    if (!"All".equals(currentLocationFilter)) {
                        boolean locationMatch = false;

                        // Check hospital address
                        if (doctor.getHospitalAddress() != null) {
                            String address = doctor.getHospitalAddress().toLowerCase();
                            String filterLocation = currentLocationFilter.toLowerCase();

                            // Direct match
                            if (address.contains(filterLocation)) {
                                locationMatch = true;
                            }
                            // Check if address contains the city name
                            else if (address.contains(filterLocation.replace(" ", ""))) {
                                locationMatch = true;
                            }
                        }

                        // Also check hospital name for location indicators
                        if (!locationMatch && doctor.getHospitalName() != null) {
                            String hospitalName = doctor.getHospitalName().toLowerCase();
                            String filterLocation = currentLocationFilter.toLowerCase();
                            if (hospitalName.contains(filterLocation)) {
                                locationMatch = true;
                            }
                        }

                        if (!locationMatch) {
                            return false;
                        }
                    }

                    // Rating filter
                    if (doctor.getAverageRating() < minRatingFilter) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private void setupScrollListener() {
        doctorScrollPane.vvalueProperty().addListener((obs, oldValue, newValue) -> {
            if (!isLoadingMore && "dashboard".equals(currentView) && newValue.doubleValue() > 0.9) {
                isLoadingMore = true;
                System.out.println("Scroll triggered: Loading more doctor cards");
                loadDoctorCardsAsync(searchBar.getText().trim(), LOAD_MORE_LIMIT, true);
            }
        });
    }
    private void loadAvailableLocationsAsync() {
        if (locationsLoaded) {
            return;
        }

        Task<Set<String>> locationTask = new Task<>() {
            @Override
            protected Set<String> call() throws Exception {
                return DoctorRepository.getAvailableLocations();
            }
        };

        locationTask.setOnSucceeded(event -> {
            Set<String> locations = locationTask.getValue();
            availableLocations.clear();
            availableLocations.add("All");
            availableLocations.addAll(locations.stream().sorted().collect(Collectors.toList()));
            locationsLoaded = true;
        });

        new Thread(locationTask).start();
    }
    // Update your existing loadDashboard method
    private void loadDashboard() {
        System.out.println("Loading dashboard...");
        currentView = "dashboard";

        // Clear previous search results
        clearSearchResults();

        // Reset filters when loading dashboard
        currentSpecialtyFilter = "All";
        currentLocationFilter = "All";
        minRatingFilter = 0.0;
        currentSortType = "name";
        isAscending = true;

        // Clear search bar
        if (searchBar != null) {
            searchBar.clear();
        }

        // Restore original content structure
        restoreOriginalScrollPaneContent();

        // Show search and filter controls
        searchBar.setVisible(true);
        searchButton.setVisible(true);
        filterButton.setVisible(true);
        sortButton.setVisible(true);
        sectionTitle.setText("Available Doctors");

        // Load initial random cards instead of empty search
        loadInitialDoctorCards();

        // Animate content
        animateContent();
    }


    private void loadDoctorCardsAsync(String searchTerm, int limit, boolean append) {
        if (!"dashboard".equals(currentView)) {
            System.out.println("loadDoctorCardsAsync skipped: currentView=" + currentView);
            return;
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
                List<DoctorCardDataModel> doctorList = DoctorRepository.loadDoctorCards(searchTerm, limit);

                // Remove duplicates from the fetched list
                doctorList = removeDuplicates(doctorList);

                if (doctorList.isEmpty() && !searchTerm.isEmpty()) {
                    doctorList = DoctorRepository.loadDoctorCards("", limit);
                    doctorList = removeDuplicates(doctorList);
                }

                for (DoctorCardDataModel data : doctorList) {
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
                    // Don't clear loadedDoctorIds here if we want to prevent duplicates across searches
                    // Only clear when switching views
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
            Platform.runLater(() -> {
                adjustScrollPaneSize();
                rootPane.requestLayout();
                System.out.println("Sidebar toggle complete: sidebarVisible=" + sidebarVisible);
            });
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
    protected void onDashboardButtonClicked() {
        System.out.println("Dashboard button clicked");
        loadDashboard();
    }

    @FXML
    protected void onUpcomingButtonClicked() {
        System.out.println("Switching to upcoming appointments");
        currentView = "appointments";

        // Restore original content structure FIRST
        restoreOriginalScrollPaneContent();

        // Hide search and filter controls
        searchBar.setVisible(false);
        searchButton.setVisible(false);
        filterButton.setVisible(false);
        sortButton.setVisible(false);
        sectionTitle.setText("Upcoming Appointments");

        // Clear existing content and load appointments
        doctorCardsContainer.getChildren().clear();
        loadedDoctorIds.clear();
        noResultsMessage.setVisible(false);
        loadUpcomingAppointments();
    }

    private void loadUpcomingAppointments() {
        System.out.println("Loading upcoming appointments");

        Task<List<UpcomingAppointmentModel>> loadTask = new Task<>() {
            @Override
            protected List<UpcomingAppointmentModel> call() throws Exception {
                return AppointmentRepository.getUpcomingAppointmentsForPatient(PatientModel.getPhone());
            }
        };

        loadTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                List<UpcomingAppointmentModel> appointments = loadTask.getValue();

                loadingIndicator.setVisible(false);

                if (appointments.isEmpty()) {
                    noResultsMessage.setVisible(true);
                    noResultsMessage.getChildren().setAll(
                            new Label("No upcoming appointments") {{
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
                            scale.setFromX(0.99);
                            scale.setToX(1.0);
                            scale.setFromY(0.99);
                            scale.setToY(1.0);

                            new ParallelTransition(fade, scale).play();
                        } catch (IOException e) {
                            System.err.println("Error loading appointment card: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                adjustScrollPaneSize();
                animateContent();
            });
        });

        loadTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Appointments load failed: " + errorMessage);
                loadingIndicator.setVisible(false);
                showErrorAlert("Load Error", "Failed to load appointments: " + errorMessage);
            });
        });

        loadingIndicator.setVisible(true);
        new Thread(loadTask).start();
    }
    @FXML
    protected void onHistoryButtonClicked(ActionEvent event) {
        System.out.println("History button clicked at " +
                LocalDateTime.now(ZoneId.of("Asia/Karachi")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        currentView = "history";

        // Hide search and filter controls
        searchBar.setVisible(false);
        searchButton.setVisible(false);
        filterButton.setVisible(false);
        sortButton.setVisible(false);
        sectionTitle.setText("Appointment History");

        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/PatientAppointmentHistory.fxml"));
                    Parent historyView = loader.load();
                    PatientAppointmentHistoryController historyController = loader.getController();

                    // Store the controller reference if needed for future updates
                    historyView.getProperties().put("controller", historyController);
                    return historyView;
                } catch (Exception e) {
                    System.err.println("Error loading appointment history: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(loadEvent -> {
            Platform.runLater(() -> {
                try {
                    Parent historyView = loadTask.getValue();

                    // Clear existing content and set history as the content
                    doctorCardsContainer.getChildren().clear();
                    loadedDoctorIds.clear();
                    VBox contentWrapper = new VBox(historyView);
                    contentWrapper.setPadding(new Insets(20, 20, 20, 20));
                    doctorScrollPane.setContent(contentWrapper);

                    // Apply transition animation
                    animateContent();

                } catch (Exception e) {
                    System.err.println("Error setting history content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set history content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(loadEvent -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("History load failed: " + errorMessage);
//                showErrorAlert("Load Error", "Failed to load appointment history: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    // Update the restoreOriginalScrollPaneContent method to handle the history view
    private void restoreOriginalScrollPaneContent() {
        if (doctorScrollPane.getContent() != doctorCardsContainer) {
            System.out.println("Restoring original scroll pane content from: " + currentView);
            doctorScrollPane.setContent(doctorCardsContainer);

            // Reset visibility of search controls when returning to dashboard
            if ("dashboard".equals(currentView)) {
                searchBar.setVisible(true);
                searchButton.setVisible(true);
                filterButton.setVisible(true);
                sortButton.setVisible(true);
            }
        }
    }

    @FXML
    protected void onProfileButtonClicked(ActionEvent event) {
        System.out.println("Profile button clicked at " +
                LocalDateTime.now(ZoneId.of("Asia/Karachi")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        currentView = "profile";

        // Hide search and filter controls
        searchBar.setVisible(false);
        searchButton.setVisible(false);
        filterButton.setVisible(false);
        sortButton.setVisible(false);
        sectionTitle.setText("Patient Profile");

        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/healzone/Patient/PatientProfile.fxml"));
                    Parent profileView = loader.load();
                    PatientProfileController profileController = loader.getController();

                    // Set initial values in the profile controller
                    profileController.setNameField(PatientModel.getName() != null ? PatientModel.getName() : "");
                    profileController.setFatherNameField(PatientModel.getFatherName() != null ? PatientModel.getFatherName() : "");
                    profileController.setPhoneNumberField(PatientModel.getPhone() != null ? PatientModel.getPhone() : "");
                    profileController.setEmailField(PatientModel.getEmail() != null ? PatientModel.getEmail() : "");
                    profileController.setAgeField(PatientModel.getAge() != null ? PatientModel.getAge() : "");
                    profileController.setGenderComboBox(PatientModel.getGender() != null ? PatientModel.getGender() : "");

                    profileView.getProperties().put("controller", profileController);
                    return profileView;
                } catch (Exception e) {
                    System.err.println("Error loading profile: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        };

        loadTask.setOnSucceeded(loadEvent -> {
            Platform.runLater(() -> {
                try {
                    Parent profileView = loadTask.getValue();

                    // Clear existing content and set profile as the content
                    doctorCardsContainer.getChildren().clear();
                    loadedDoctorIds.clear();
                    VBox contentWrapper = new VBox(profileView);
                    contentWrapper.setPadding(new Insets(20, 20, 20, 20));
                    doctorScrollPane.setContent(contentWrapper);

                    // Apply transition animation
                    animateContent();

                } catch (Exception e) {
                    System.err.println("Error setting profile content: " + e.getMessage());
                    showErrorAlert("Load Error", "Failed to set profile content: " + e.getMessage());
                }
            });
        });

        loadTask.setOnFailed(loadEvent -> {
            Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                System.err.println("Profile load failed: " + errorMessage);
                showErrorAlert("Load Error", "Failed to load profile: " + errorMessage);
            });
        });

        new Thread(loadTask).start();
    }

    private List<DoctorCardDataModel> removeDuplicates(List<DoctorCardDataModel> doctorList) {
        Set<String> seenIds = new HashSet<>();
        doctorList.removeIf(doctor -> !seenIds.add(doctor.getGovtId()));
        return doctorList;
    }

    private void animateContent() {
        if (doctorScrollPane != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), doctorScrollPane);
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