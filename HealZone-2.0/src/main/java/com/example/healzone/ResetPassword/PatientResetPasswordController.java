package com.example.healzone.ResetPassword;

import com.example.healzone.model.PatientModel;
import com.example.healzone.controller.shared.MainViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;



import static com.example.healzone.Checks.ChecksForPatientFields.isPasswordMatch;
import static com.example.healzone.Checks.ChecksForPatientFields.isPasswordValid;
import static com.example.healzone.repository.PatientRepository.resetPassword;

public class PatientResetPasswordController {
    @FXML
    private TextField password;
    @FXML
    private TextField confirmPassword;
    @FXML
    private Label resetPasswordErrorMessage;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private CheckBox showPasswordBox;
    @FXML
    private TextField visibleConfirmPasswordField;
    @FXML
    private CheckBox showConfirmPasswordBox;
    @FXML
    private void initialize() {
        // Sync content when typing
        password.textProperty().bindBidirectional(visiblePasswordField.textProperty());
        confirmPassword.textProperty().bindBidirectional(visibleConfirmPasswordField.textProperty());

        // Hide visiblePasswordFields initially
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.setManaged(false);

        // Toggle for Password field
        showPasswordBox.setOnAction(event -> {
            if (showPasswordBox.isSelected()) {
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);

                password.setVisible(false);
                password.setManaged(false);
            } else {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);

                password.setVisible(true);
                password.setManaged(true);
            }
        });

        // Toggle for Confirm Password field
        showConfirmPasswordBox.setOnAction(event -> {
            if (showConfirmPasswordBox.isSelected()) {
                visibleConfirmPasswordField.setVisible(true);
                visibleConfirmPasswordField.setManaged(true);

                confirmPassword.setVisible(false);
                confirmPassword.setManaged(false);
            } else {
                visibleConfirmPasswordField.setVisible(false);
                visibleConfirmPasswordField.setManaged(false);

                confirmPassword.setVisible(true);
                confirmPassword.setManaged(true);
            }
        });
    }
    @FXML
    private void onClickResetPasswordButton(ActionEvent event) {
        PatientModel.setPassword(password.getText());
        PatientModel.setConfirmPassword(confirmPassword.getText());
        if (!isPasswordMatch()) {
            resetPasswordErrorMessage.setText("⚠️ Password and Confirm Password do not match!");
            return;
        } else if (!isPasswordValid()) {
            resetPasswordErrorMessage.setText("⚠️ Password must be at least 8 characters!");
            return;
        } else {
            resetPassword(PatientModel.getPassword(), PatientModel.getEmail());
            try {
                StackPane root = (StackPane) ((Node) event.getSource()).getScene().getRoot();
                MainViewController mainController = (MainViewController) root.getProperties().get("controller");
                if (mainController != null) {
                    mainController.loadPatientLogin();
                } else {
                    System.err.println("MainViewController not found in root properties.");
                }
            } catch (Exception e) {
                System.err.println("Error loading PatientLogin: ");
                e.printStackTrace();
            }
        }
    }
}
