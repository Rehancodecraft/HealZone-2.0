package com.example.healzone;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import java.io.IOException;
import javafx.animation.FadeTransition;


public class HomePageController {
    @FXML
    private AnchorPane sideBar;
    @FXML
    private Label displayName;
    @FXML
    private Button loginLink;

    @FXML
    protected void toggleSideBar() {
        boolean isSidebarVisible = sideBar.isVisible();
        double width = sideBar.getPrefWidth();  // Width for left-right movement

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideBar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), sideBar);

        if (!isSidebarVisible) {
            // Prepare for entry from left
            sideBar.setVisible(true);
            sideBar.setTranslateX(-width);  // Start off-screen to the left
            slide.setFromX(-width);
            slide.setToX(0);

            fade.setFromValue(0.0);
            fade.setToValue(1.0);
        } else {
            // Slide out to left
            slide.setFromX(0);
            slide.setToX(-width);

            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            slide.setOnFinished(event -> sideBar.setVisible(false)); // Hide after animation
        }

        ParallelTransition animation = new ParallelTransition(slide, fade);
        animation.play();
    }

    @FXML
    protected void onLogOutButtonClicked() throws IOException {
        SessionManager.logOut();
        loginLink.setVisible(true);
    }
    @FXML
    protected void initialize() {
        displayName.setText(SessionManager.getCurrentUser());
    }

}