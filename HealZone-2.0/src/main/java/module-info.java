module com.example.healzone {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop; // Required for java.awt.image.BufferedImage
    requires jakarta.activation;
    requires org.kordamp.ikonli.javafx;
    requires javafx.swing;
    requires org.apache.pdfbox; // Ensures javafx.embed.swing is available
    opens com.example.healzone to javafx.fxml;
    exports com.example.healzone;
    opens com.example.healzone.Patient to javafx.fxml;
    opens com.example.healzone.Doctor to javafx.fxml;
    exports com.example.healzone.repository;
    opens com.example.healzone.repository to javafx.fxml;
    exports com.example.healzone.ResetPassword;
    opens com.example.healzone.ResetPassword to javafx.fxml;
    exports com.example.healzone.EmailVerificationForRegistration;
    opens com.example.healzone.EmailVerificationForRegistration to javafx.fxml;
    exports com.example.healzone.Checks;
    opens com.example.healzone.Checks to javafx.fxml;
    exports com.example.healzone.EmailVerificationForResetPasssword;
    opens com.example.healzone.EmailVerificationForResetPasssword to javafx.fxml;
    exports com.example.healzone.OTPVerificationForResetPassword;
    opens com.example.healzone.OTPVerificationForResetPassword to javafx.fxml;
    exports com.example.healzone.OTPVerificationForRegisteration;
    opens com.example.healzone.OTPVerificationForRegisteration to javafx.fxml;
    exports com.example.healzone.controller.doctor.SignUp;
    opens com.example.healzone.controller.doctor.SignUp to javafx.fxml;
    exports com.example.healzone.config;
    opens com.example.healzone.config to javafx.fxml;
    exports com.example.healzone.model;
    opens com.example.healzone.model to javafx.fxml;
    exports com.example.healzone.controller.doctor;
    opens com.example.healzone.controller.doctor to javafx.fxml;
    exports com.example.healzone.controller.patient;
    opens com.example.healzone.controller.patient to javafx.fxml;
    exports com.example.healzone.controller.shared;
    opens com.example.healzone.controller.shared to javafx.fxml;
    exports com.example.healzone.util;
    opens com.example.healzone.util to javafx.fxml;
}