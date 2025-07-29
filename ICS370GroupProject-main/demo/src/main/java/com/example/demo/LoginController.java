package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void initialize() {
        System.out.println("LoginController initialized!");
        System.out.println("Login button: " + loginButton); // Debugging statement
    }

    @FXML
    private void loginButtonClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        LoginService.User user = LoginService.authenticate(username, password);

        if (user != null) {
            System.out.println("Login successful for user: " + user.username);
            loadHomeView();
        } else {
            showAlert("Error", "Invalid username or password.");
        }
    }

    private void loadHomeView() {
        try {
            // Change this to load the sport selection view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/sport-selection-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Sport"); // New title
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the view.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToRegistration() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/demo/registration-view.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
