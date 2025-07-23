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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private TextField renterNameField;
    @FXML
    private DatePicker rentalDatePicker;
    @FXML
    private TextField rentalTimeField;

    private List<RentalItem> rentalEquipmentList;

    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {
        EquipmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentID"));
        venueIDColumn.setCellValueFactory(new PropertyValueFactory<>("venueeID"));
        isReservedColumn.setCellValueFactory(new PropertyValueFactory<>("isReserved"));
        rentedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("rentedTime"));
        returnedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("returnedTime"));
        renterNameColumn.setCellValueFactory(new PropertyValueFactory<>("renterName"));

        loadEquipmentInventory();
    }

    private void loadEquipmentInventory() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("demo/equipmentInventory.json")) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            int venueID = Integer.parseInt((String) jsonObject.get("venueID"));
            JSONArray equipmentList = (JSONArray) jsonObject.get("equipmentInventory");

            rentalEquipmentList = new ArrayList<>();
            for (Object equipmentObj : equipmentList) {
                JSONObject equipmentJson = (JSONObject) equipmentObj;
                int equipmentID = ((Long) equipmentJson.get("equipmentID")).intValue();
                boolean isReserved = (Boolean) equipmentJson.get("isReserved");
                String rentedTime = (String) equipmentJson.getOrDefault("rentedTime", "");
                String returnedTime = (String) equipmentJson.getOrDefault("returnedTime", "");
                String renterName = (String) equipmentJson.getOrDefault("renterName", "");
                String itemType = (String) equipmentJson.getOrDefault("itemType", "");

                rentalEquipmentList.add(new RentalItem(equipmentID, rentedTime, returnedTime, venueID, isReserved, renterName, itemType));
            }

            equipmentTable.getItems().setAll(rentalEquipmentList);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void confirmRental() {
        RentalItem selectedEquipment = equipmentTable.getSelectionModel().getSelectedItem();
        if (selectedEquipment == null) {
            showAlert("No Selection", "Please select equipoment to rent.");
            return;
        }

        if (selectedEquipment.isIsReserved()) {
            showAlert("Already Reserved", "This equipment is already reserved. Please select another item.");
            return;
        }

        if (renterNameField.getText().isEmpty() || rentalDatePicker.getValue() == null || rentalTimeField.getText().isEmpty()) {
            showAlert("Missing Information", "Please enter your name, date, and time for the rental.");
            return;
        }

        String renterName = renterNameField.getText();
        String rentedTime = rentalDatePicker.getValue().toString() + " " + rentalTimeField.getText();
        selectedEquipment.setReserved(true);
        selectedEquipment.setRentedTime(rentedTime);
        selectedEquipment.setRenterName(renterName);
        selectedEquipment.setReturnedTime("");

        updateJsonFile(selectedEquipment);

        equipmentTable.refresh();
        showAlert("Success", "This item has been reserved successfully!");
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

        String returnedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        selectedEquipment.setReserved(false);
        selectedEquipment.setReturnedTime(returnedTime);

        updateJsonFile(selectedEquipment);

        equipmentTable.refresh();
        showAlert("Success", "The equipment has been returned successfully!");
    }

    private void updateJsonFile(RentalItem updatedEquipment) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("demo/equipmentInventory.json")) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONArray equipmentList = (JSONArray) jsonObject.get("equipmentInventory");

            for (Object equipmentObj : equipmentList) {
                JSONObject equipmentJson = (JSONObject) equipmentObj;
                int equipmentID = ((Long) equipmentJson.get("equipmentID")).intValue();
                if (equipmentID == updatedEquipment.getItemID()) {
                    equipmentJson.put("isReserved", updatedEquipment.isIsReserved());
                    equipmentJson.put("rentedTime", updatedEquipment.getRentedTime());
                    equipmentJson.put("returnedTime", updatedEquipment.getReturnedTime());
                    equipmentJson.put("renterName", updatedEquipment.getRenterName());
                    break;
                }
            }

            try (FileWriter file = new FileWriter("demo/equipmentInventory.json")) {
                file.write(jsonObject.toJSONString());
                file.flush();
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


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

}
