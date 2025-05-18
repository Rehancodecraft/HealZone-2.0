package com.example.healzone.Doctor;

import com.example.healzone.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginDoctor {
    public static void appearDoctorLoginPage() throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Doctor/DoctorLogin.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }
}