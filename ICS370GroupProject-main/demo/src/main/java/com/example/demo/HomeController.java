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

    private SportSelectionController.Sport selectedSport;

    @FXML
    private void onManageBookingTimeClicked() { 
        System.out.println("Reserve/Cancel Booking button clicked.");
        updateStatus("Navigating to Reserve/Cancel Booking...");
        loadBookingView();
    }

    @FXML
    private void onRentEquipClicked() {
        System.out.println("Rent equipment button clicked.");
        updateStatus("Navigating to Rent Equipment...");
        loadRentEquipView();
    }

    private void loadBookingView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/first-view.fxml"));
            Parent root = loader.load();

            BookingController controller = loader.getController();
            controller.initData(selectedSport); // Pass sport data

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(selectedSport.getName() + " Bookings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Booking view.");
        }
    }

    private void loadRentEquipView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/equipRental.fxml"));
            Parent root = loader.load();

            EquipmentRentalController controller = loader.getController();
            controller.initData(selectedSport);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(selectedSport.getName() + " Rent Equipment");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Rent Equipment view.");
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

    public void setSelectedSport(SportSelectionController.Sport sport) {
        this.selectedSport = sport;
    }

    @FXML
    private void changeSport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/sport-selection-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Sport");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the sport selection view.");
        }
    }

}
