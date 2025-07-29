package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SportSelectionController {

    @FXML
    private ListView<Sport> sportListView;
    @FXML
    private Button continueButton;

    @FXML
    public void initialize() {
        loadSports();
    }

    private void loadSports() {
        ObservableList<Sport> sports = FXCollections.observableArrayList();
        // Update the SQL query to include the new column
        String sql = "SELECT id, name, max_players FROM sports";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Update the Sport object creation to include maxPlayers
                sports.add(new Sport(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("max_players")
                ));
            }
            sportListView.setItems(sports);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load sports.");
        }
    }

    @FXML
    private void onContinue() {
        Sport selectedSport = sportListView.getSelectionModel().getSelectedItem();
        if (selectedSport == null) {
            showAlert("No Selection", "Please select a sport to continue.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/home-view.fxml"));
            Parent root = loader.load();

            // Pass the selected sport to the HomeController
            HomeController homeController = loader.getController();
            homeController.setSelectedSport(selectedSport);

            Stage stage = (Stage) continueButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(selectedSport.getName() + " Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the home view.");
        }
    }

    // You'll also need a Sport model class
    // You can create a new file Sport.java for this
    public static class Sport {
        private final int id;
        private final String name;
        private final int maxPlayers; // Add this line

        public Sport(int id, String name, int maxPlayers) { // Update the constructor
            this.id = id;
            this.name = name;
            this.maxPlayers = maxPlayers; // Add this line
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getMaxPlayers() { return maxPlayers; } // Add this getter

        @Override
        public String toString() { return name; }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void logout() {
        // Standard logout logic
    }
}