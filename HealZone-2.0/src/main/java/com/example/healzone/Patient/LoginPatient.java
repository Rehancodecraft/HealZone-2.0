package com.example.healzone.Patient;

import com.example.healzone.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.example.healzone.DatabaseConnection.getCurrentPatientDetails;

public class LoginPatient {
    public static void appearPatientLoginPage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Patient/PatientLogin.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    public static boolean getCurrentPatient(String email, String password) {
        try {
            ResultSet rs = getCurrentPatientDetails(email, password);
            if(rs.next()) {
                Patient.setName(rs.getString("name"));
                Patient.setFatherName(rs.getString("father_name"));
                Patient.setGender(rs.getString("gender"));
                Patient.setAge(rs.getString("age"));
                Patient.setPhone(rs.getString("phone_number"));
                return true;
            }else{
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
