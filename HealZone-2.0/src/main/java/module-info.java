module com.example.healzone {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires jakarta.activation;

    opens com.example.healzone to javafx.fxml;
    exports com.example.healzone;
    exports com.example.healzone.Patient;
    opens com.example.healzone.Patient to javafx.fxml;
    exports com.example.healzone.Doctor;
    opens com.example.healzone.Doctor to javafx.fxml;
}