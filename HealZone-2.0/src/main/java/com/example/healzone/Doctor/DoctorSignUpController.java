package com.example.healzone.Doctor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DoctorSignUpController implements Initializable {
    @FXML
    private StackPane rootStackPane;

    @FXML
    private ImageView backgroundImageView;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bind ImageView size to StackPane size
        backgroundImageView.fitWidthProperty().bind(rootStackPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootStackPane.heightProperty());
    }
}
