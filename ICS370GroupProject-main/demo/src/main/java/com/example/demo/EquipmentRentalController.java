package com.example.demo;

import com.example.demo.domain.RentalItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EquipmentRentalController {

    @FXML
    private TableView<RentalItem> equipmentTable;
    @FXML
    private TableColumn<RentalItem, Integer> EquipmentIDColumn;
    @FXML
    private TableColumn<RentalItem, Integer> venueIDColumn;
    @FXML
    private TableColumn<RentalItem, Boolean> isReservedColumn;
    @FXML
    private TableColumn<RentalItem, String> rentedTimeColumn;
    @FXML
    private TableColumn<RentalItem, String> returnedTimeColumn;
    @FXML
    private TableColumn<RentalItem, String> renterNameColumn;

    @FXML
    private TableColumn<RentalItem, String> itemTypeColumn;

    @FXML
    private TextField renterNameField;
    @FXML
    private DatePicker rentalDatePicker;
    @FXML
    private TextField rentalTimeField;

    @FXML
    private Label statusLabel;

    private SportSelectionController.Sport selectedSport;

    @FXML
    public void initialize() {
        itemTypeColumn.setCellValueFactory(new PropertyValueFactory<>("itemType"));
        EquipmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentID"));
        venueIDColumn.setCellValueFactory(new PropertyValueFactory<>("venueID"));
        isReservedColumn.setCellValueFactory(new PropertyValueFactory<>("isReserved"));
        rentedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("rentedTime"));
        returnedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("returnedTime"));
        renterNameColumn.setCellValueFactory(new PropertyValueFactory<>("renterName"));

        loadEquipmentInventory();
    }

    public void initData(SportSelectionController.Sport sport) {
        this.selectedSport = sport;
        loadEquipmentInventory(); // Load equipment for the selected sport
    }

    private void loadEquipmentInventory() {
        List<RentalItem> rentalEquipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE sport_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Check if a sport has been selected before running the query
            if (this.selectedSport == null) {
                // This can happen if the view is loaded without the initData method being called.
                // You might want to show a message or leave the table empty.
                equipmentTable.getItems().clear();
                return;
            }

            pstmt.setInt(1, selectedSport.getId()); // Filter by the selected sport
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int equipmentID = rs.getInt("equipmentID");
                int venueID = rs.getInt("venueID");
                boolean isReserved = rs.getBoolean("isReserved");
                Timestamp rentedTimestamp = rs.getTimestamp("rentedTime");
                Timestamp returnedTimestamp = rs.getTimestamp("returnedTime");
                String renterName = rs.getString("renterName");
                String itemType = rs.getString("itemType");

                String rentedTime = (rentedTimestamp != null) ? rentedTimestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
                String returnedTime = (returnedTimestamp != null) ? returnedTimestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";

                rentalEquipmentList.add(new RentalItem(equipmentID, rentedTime, returnedTime, venueID, isReserved, renterName, itemType));
            }

            equipmentTable.getItems().setAll(rentalEquipmentList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load equipment from the database.");
        }
    }

    @FXML
    private void confirmRental() {
        RentalItem selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedEquipment == null) {
            showAlert("No Selection", "Please select equipment to rent.");
            return;
        }

        if (selectedEquipment.isIsReserved()) {
            showAlert("Already Reserved", "This equipment is already reserved.");
            return;
        }

        if (renterNameField.getText().isEmpty() || rentalDatePicker.getValue() == null || rentalTimeField.getText().isEmpty()) {
            showAlert("Missing Information", "Please enter your name, and a rental date and time.");
            return;
        }

        String renterName = renterNameField.getText();
        LocalDateTime rentedTime = LocalDateTime.of(rentalDatePicker.getValue(), java.time.LocalTime.parse(rentalTimeField.getText()));

        String sql = "UPDATE equipment SET isReserved = ?, rentedTime = ?, renterName = ?, returnedTime = NULL WHERE equipmentID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, true);
            pstmt.setTimestamp(2, Timestamp.valueOf(rentedTime));
            pstmt.setString(3, renterName);
            pstmt.setInt(4, selectedEquipment.getItemID());
            pstmt.executeUpdate();

            showAlert("Success", "This item has been reserved successfully!");
            loadEquipmentInventory(); // Refresh the table from the database

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update the equipment status.");
        }
    }

    @FXML
    private void returnEquipment() {
        RentalItem selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedEquipment == null) {
            showAlert("No Selection", "Please select an item to return.");
            return;
        }

        if (!selectedEquipment.isIsReserved()) {
            showAlert("Not Reserved", "This item is not currently reserved.");
            return;
        }

        LocalDateTime returnedTime = LocalDateTime.now();
        String sql = "UPDATE equipment SET isReserved = ?, returnedTime = ? WHERE equipmentID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, false);
            pstmt.setTimestamp(2, Timestamp.valueOf(returnedTime));
            pstmt.setInt(3, selectedEquipment.getItemID());
            pstmt.executeUpdate();

            showAlert("Success", "The equipment has been returned successfully!");
            loadEquipmentInventory(); // Refresh the table

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to process the equipment return.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods (goToHomeScreen, logout, etc.) remain unchanged
    public void goToHomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/home-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the Home view.");
        }
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

    @FXML
    private void logout() {
        LoginService.logout();
        navigateToLogin();
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
}