package com.example.demo;

import com.example.demo.domain.RentalClubs;
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

public class ClubRentalController {

    @FXML
    private TableView<RentalClubs> clubTable;
    @FXML
    private TableColumn<RentalClubs, Integer> clubsIDColumn;
    @FXML
    private TableColumn<RentalClubs, Integer> courseIDColumn;
    @FXML
    private TableColumn<RentalClubs, Boolean> isReservedColumn;
    @FXML
    private TableColumn<RentalClubs, String> rentedTimeColumn;
    @FXML
    private TableColumn<RentalClubs, String> returnedTimeColumn;
    @FXML
    private TableColumn<RentalClubs, String> renterNameColumn;

    @FXML
    private TextField renterNameField;
    @FXML
    private DatePicker rentalDatePicker;
    @FXML
    private TextField rentalTimeField;

    private List<RentalClubs> rentalClubsList;

    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {
        clubsIDColumn.setCellValueFactory(new PropertyValueFactory<>("clubsID"));
        courseIDColumn.setCellValueFactory(new PropertyValueFactory<>("courseID"));
        isReservedColumn.setCellValueFactory(new PropertyValueFactory<>("isReserved"));
        rentedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("rentedTime"));
        returnedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("returnedTime"));
        renterNameColumn.setCellValueFactory(new PropertyValueFactory<>("renterName"));

        loadClubInventory();
    }

    private void loadClubInventory() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("demo/clubInventory.json")) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            int courseID = Integer.parseInt((String) jsonObject.get("courseID"));
            JSONArray clubList = (JSONArray) jsonObject.get("clubInventory");

            rentalClubsList = new ArrayList<>();
            for (Object clubObj : clubList) {
                JSONObject clubJson = (JSONObject) clubObj;
                int clubsID = ((Long) clubJson.get("clubsID")).intValue();
                boolean isReserved = (Boolean) clubJson.get("isReserved");
                String rentedTime = (String) clubJson.getOrDefault("rentedTime", "");
                String returnedTime = (String) clubJson.getOrDefault("returnedTime", "");
                String renterName = (String) clubJson.getOrDefault("renterName", "");

                rentalClubsList.add(new RentalClubs(clubsID, rentedTime, returnedTime, courseID, isReserved, renterName));
            }

            clubTable.getItems().setAll(rentalClubsList);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void confirmRental() {
        RentalClubs selectedClub = clubTable.getSelectionModel().getSelectedItem();
        if (selectedClub == null) {
            showAlert("No Selection", "Please select a set of clubs to rent.");
            return;
        }

        if (selectedClub.isIsReserved()) {
            showAlert("Already Reserved", "This set of clubs is already reserved. Please select another set.");
            return;
        }

        if (renterNameField.getText().isEmpty() || rentalDatePicker.getValue() == null || rentalTimeField.getText().isEmpty()) {
            showAlert("Missing Information", "Please enter your name, date, and time for the rental.");
            return;
        }

        String renterName = renterNameField.getText();
        String rentedTime = rentalDatePicker.getValue().toString() + " " + rentalTimeField.getText();
        selectedClub.setReserved(true);
        selectedClub.setRentedTime(rentedTime);
        selectedClub.setRenterName(renterName);
        selectedClub.setReturnedTime("");

        updateJsonFile(selectedClub);

        clubTable.refresh();
        showAlert("Success", "The club set has been reserved successfully!");
    }

    @FXML
    private void returnClubs() {
        RentalClubs selectedClub = clubTable.getSelectionModel().getSelectedItem();
        if (selectedClub == null) {
            showAlert("No Selection", "Please select a set of clubs to return.");
            return;
        }

        if (!selectedClub.isIsReserved()) {
            showAlert("Not Reserved", "This set of clubs is not currently reserved.");
            return;
        }

        String returnedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        selectedClub.setReserved(false);
        selectedClub.setReturnedTime(returnedTime);

        updateJsonFile(selectedClub);

        clubTable.refresh();
        showAlert("Success", "The club set has been returned successfully!");
    }

    private void updateJsonFile(RentalClubs updatedClub) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("demo/clubInventory.json")) {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONArray clubList = (JSONArray) jsonObject.get("clubInventory");

            for (Object clubObj : clubList) {
                JSONObject clubJson = (JSONObject) clubObj;
                int clubsID = ((Long) clubJson.get("clubsID")).intValue();
                if (clubsID == updatedClub.getClubsID()) {
                    clubJson.put("isReserved", updatedClub.isIsReserved());
                    clubJson.put("rentedTime", updatedClub.getRentedTime());
                    clubJson.put("returnedTime", updatedClub.getReturnedTime());
                    clubJson.put("renterName", updatedClub.getRenterName());
                    break;
                }
            }

            try (FileWriter file = new FileWriter("demo/clubInventory.json")) {
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
