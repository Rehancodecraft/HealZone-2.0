module com.example.healzone {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires jakarta.activation;
    requires org.kordamp.ikonli.javafx;

    opens com.example.healzone to javafx.fxml;
    exports com.example.healzone;
    exports com.example.healzone.Patient;
    opens com.example.healzone.Patient to javafx.fxml;
    exports com.example.healzone.Doctor;
    opens com.example.healzone.Doctor to javafx.fxml;
    exports com.example.healzone.DatabaseConnection;
    opens com.example.healzone.DatabaseConnection to javafx.fxml;
    exports com.example.healzone.PasswordEncryption;
    opens com.example.healzone.PasswordEncryption to javafx.fxml;
    exports com.example.healzone.StartView;
    opens com.example.healzone.StartView to javafx.fxml;
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
    exports com.example.healzone.Doctor.SignUp;
    opens com.example.healzone.Doctor.SignUp to javafx.fxml;
}