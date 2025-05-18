package com.example.healzone;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import java.io.IOException;
import javafx.animation.FadeTransition;


import static com.example.healzone.Doctor.LoginDoctor.appearDoctorLoginPage;
import static com.example.healzone.Patient.LoginPatient.appearPatientLoginPage;

public class HomePageController {
    @FXML
    private AnchorPane sideBar;
    @FXML
    private Label displayName;
    @FXML
    private Button loginLink;

    @FXML
//    protected void toggleSideBar(){
//        Boolean isSidebarVisible = sideBar.isVisible();
//        TranslateTransition slide = new TranslateTransition();
//        slide.setNode(sideBar);
//        slide.setDuration(Duration.millis(300));
//
//        if (!isSidebarVisible) {
//            // Slide in
//            sideBar.setVisible(true);
//            slide.setFromX(-sideBar.getPrefWidth());
//            slide.setToX(0);
//        } else {
//            // Slide out
//            slide.setFromX(0);
//            slide.setToX(-sideBar.getPrefWidth());
//            slide.setOnFinished(event -> sideBar.setVisible(false)); // Hide after slide
//        }
//
//        slide.play();
//    }
    protected void toggleSideBar() {
        boolean isSidebarVisible = sideBar.isVisible();
        double height = sideBar.getPrefHeight();

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideBar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), sideBar);

        if (!isSidebarVisible) {
            // Prepare for entry
            sideBar.setVisible(true);
            sideBar.setTranslateY(-height);  // Start above the view
            slide.setFromY(-height);
            slide.setToY(0);

            fade.setFromValue(0.0);
            fade.setToValue(1.0);
        } else {
            // Slide out upwards
            slide.setFromY(0);
            slide.setToY(-height);

            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            slide.setOnFinished(event -> sideBar.setVisible(false)); // Hide after slide out
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