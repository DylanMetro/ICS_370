package com.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML
    private Label statusLabel;

    @FXML
    private void onReserveCancelTeeTimeClicked() {
        System.out.println("Reserve/Cancel Tee Time button clicked.");
        updateStatus("Navigating to Reserve/Cancel Tee Time...");
        loadTeeSheetView();
    }

    @FXML
    private void onRentClubsClicked() {
        System.out.println("Rent a Set of Clubs button clicked.");
        updateStatus("Navigating to Rent a Set of Clubs...");
        loadRentClubsView();
    }

    private void loadTeeSheetView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/first-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tee Sheet");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Tee Sheet view.");
        }
    }

    private void loadRentClubsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/clubRental.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Rent a Set of Golf Clubs");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Rent a Set of Clubs view.");
        }
    }
    @FXML
    private void logout() {
        LoginService.logout(); // Clear the session
        navigateToLogin(); // Redirect to the login page
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Login view.");
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
