package com.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.example.demo.LoginService.Role;
import com.example.demo.LoginService;

public class GolfController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private ListView<String> teeTimesListView;
    @FXML
    private TextField playerNameField;
    @FXML
    private Button bookTeeTimeButton;
    @FXML
    private Button cancelTeeTimeButton;
    @FXML
    private Button showPlayersButton;

    private JSONObject teeSheetData;
    private final String teeSheetFilePath = "teeSheet.json";
    private String selectedTeeTime;

    private String currentUser = LoginService.getLoggedInUsername();
    private Role currentUserRole = LoginService.getRole(currentUser);

    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {
        loadTeeSheet();
        datePicker.setOnAction(event -> onDateSelected());
        teeTimesListView.setOnMouseClicked(this::onTeeTimeSelected);
    }

    @FXML
    private void onDateSelected() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = selectedDate.format(formatter);
            System.out.println("Date selected: " + formattedDate);
            populateTeeTimesListView(formattedDate);
        }
    }

    @FXML
    private void onTeeTimeSelected(MouseEvent event) {
        selectedTeeTime = teeTimesListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onBookTeeTime() {
        String playerName = playerNameField.getText().trim();
        if (selectedTeeTime == null || playerName.isEmpty()) {
            showAlert("Error", "Please select a tee time and enter a player name.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray teeTimes = (JSONArray) teeSheetData.get(formattedDate);
        for (Object obj : teeTimes) {
            JSONObject teeTimeObj = (JSONObject) obj;
            if (teeTimeObj.get("time").equals(selectedTeeTime.split(" ")[0])) {
                JSONArray players = (JSONArray) teeTimeObj.get("players");
                if (players.size() >= 4) {
                    showAlert("Error", "This tee time is fully booked.");
                    return;
                }
                else {
                    players.add(playerName);
                    saveTeeSheet();
                    populateTeeTimesListView(formattedDate);
                    playerNameField.clear();
                    showAlert("Confirmation", "Tee Time booked successfully");
                    return;
                }
            }
        }
    }

    @FXML
    private void onCancelTeeTime() {
        String playerName = playerNameField.getText().trim();
        if (selectedTeeTime == null || playerName.isEmpty()) {
            showAlert("Error", "Please select a tee time and enter a player name.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray teeTimes = (JSONArray) teeSheetData.get(formattedDate);
        for (Object obj : teeTimes) {
            JSONObject teeTimeObj = (JSONObject) obj;
            if (teeTimeObj.get("time").equals(selectedTeeTime.split(" ")[0])) {
                JSONArray players = (JSONArray) teeTimeObj.get("players");
                if (players.contains(playerName)) {
                    players.remove(playerName);
                    saveTeeSheet();
                    populateTeeTimesListView(formattedDate);
                    playerNameField.clear();
                    showAlert("Confirmation", "Tee time successfully cancelled");
                    return;
                } else {
                    showAlert("Error", "Player not found in this tee time.");
                    return;
                }
            }
        }
    }

    @FXML
    private void onShowPlayers() {
        if (selectedTeeTime == null) {
            showAlert("Error", "Please select a tee time to view players.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray teeTimes = (JSONArray) teeSheetData.get(formattedDate);
        for (Object obj : teeTimes) {
            JSONObject teeTimeObj = (JSONObject) obj;
            if (teeTimeObj.get("time").equals(selectedTeeTime.split(" ")[0])) {
                JSONArray players = (JSONArray) teeTimeObj.get("players");
                StringBuilder playerList = new StringBuilder("Players:\n");
                for (Object player : players) {
                    playerList.append(player.toString()).append("\n");
                }
                showAlert("Players in " + selectedTeeTime, playerList.toString());
                return;
            }
        }
    }

    private void loadTeeSheet() {
        try (FileReader reader = new FileReader(teeSheetFilePath)) {
            JSONParser parser = new JSONParser();
            teeSheetData = (JSONObject) parser.parse(reader);
            System.out.println("Tee sheet loaded successfully.");
        } catch (IOException | ParseException e) {
            teeSheetData = new JSONObject();
            System.out.println("Tee sheet file not found or invalid, starting with an empty sheet.");
        }
    }

    private void saveTeeSheet() {
        try (FileWriter writer = new FileWriter(teeSheetFilePath)) {
            writer.write(teeSheetData.toJSONString());
            writer.flush();
            System.out.println("Tee sheet saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save tee sheet: " + e.getMessage());
        }
    }

    private void populateTeeTimesListView(String date) {
        JSONArray teeTimes = (JSONArray) teeSheetData.get(date);
        if (teeTimes == null) {
            teeTimes = initializeTeeTimesForDate(date);
            teeSheetData.put(date, teeTimes);
            saveTeeSheet();
        }

        ObservableList<String> items = FXCollections.observableArrayList();

        if (currentUserRole == Role.MANAGER) {
            // Show all tee times
            for (Object obj : teeTimes) {
                JSONObject teeTimeObj = (JSONObject) obj;
                String time = (String) teeTimeObj.get("time");
                JSONArray players = (JSONArray) teeTimeObj.get("players");
                items.add(time + " (" + players.size() + "/4 booked)"); // Add all tee times without filtering
            }
        } else if (currentUserRole == Role.MEMBER) {
            // Show member's tee times and empty tee times
            String memberName = getCurrentUserName(); // Get the current member's name
            for (Object obj : teeTimes) {
                JSONObject teeTimeObj = (JSONObject) obj;
                String time = (String) teeTimeObj.get("time");
                JSONArray players = (JSONArray) teeTimeObj.get("players");
                if (players.contains(memberName) || players.isEmpty()) {
                    items.add(time + " (" + players.size() + "/4 booked)");
                }
            }
        }

        teeTimesListView.setItems(items);
    }



    private JSONArray initializeTeeTimesForDate(String date) {
        JSONArray teeTimes = new JSONArray();
        int startHour = 7;
        int endHour = 17;
        int intervalMinutes = 10;

        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute < 60; minute += intervalMinutes) {
                String time = String.format("%02d:%02d", hour, minute);
                JSONObject teeTime = new JSONObject();
                teeTime.put("time", time);
                teeTime.put("players", new JSONArray());
                teeTimes.add(teeTime);
            }
        }
        return teeTimes;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Role getCurrentUserRole() {
        String username = LoginService.getLoggedInUsername(); // Get the logged-in username
        if (username != null) {
            return LoginService.getRole(username); // Get the role based on the username
        } else {
            // No user is logged in
            // Handle this case appropriately (e.g., redirect to login)
            return Role.MEMBER; // Or some other default role
        }
    }

    private String getCurrentUserName() {
        return LoginService.getLoggedInUsername(); // Get the logged-in username
    }

    public void goToHomeScreen(ActionEvent actionEvent) {


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