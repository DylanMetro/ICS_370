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

public class BookingController {
    @FXML
    private DatePicker datePicker;
    @FXML
    private ListView<String> bookingListView;
    @FXML
    private TextField playerNameField;
    @FXML
    private Button bookTimeButton;
    @FXML
    private Button cancelBookingButton;
    @FXML
    private Button showPlayersButton;

    private JSONObject bookingSheetData;
    private final String bookingFilePath = "booking.json";
    private String selectedTime;

    private String currentUser = LoginService.getLoggedInUsername();
    private Role currentUserRole = LoginService.getRole(currentUser);

    @FXML
    private Label statusLabel;


    @FXML
    public void initialize() {
        loadBookingSheet();
        datePicker.setOnAction(event -> onDateSelected());
        bookingListView.setOnMouseClicked(this::onTimeSelected);
    }

    @FXML
    private void onDateSelected() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = selectedDate.format(formatter);
            System.out.println("Date selected: " + formattedDate);
            populateBookingListView(formattedDate);
        }
    }

    @FXML
    private void onTimeSelected(MouseEvent event) {
        selectedTime = bookingListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onBookTime() {
        String playerName = playerNameField.getText().trim();
        if (selectedTime == null || playerName.isEmpty()) {
            showAlert("Error", "Please select a time and enter a player name.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray bookingTimes = (JSONArray) bookingSheetData.get(formattedDate);
        for (Object obj : bookingTimes) {
            JSONObject bookingTimeObj = (JSONObject) obj;
            if (bookingTimeObj.get("time").equals(selectedTime.split(" ")[0])) {
                JSONArray players = (JSONArray) bookingTimeObj.get("players");
                if (players.size() >= 12) {
                    showAlert("Error", "This time is fully booked.");
                    return;
                }
                else {
                    players.add(playerName);
                    saveBookingSheet();
                    populateBookingListView(formattedDate);
                    playerNameField.clear();
                    showAlert("Confirmation", "Time booked successfully");
                    return;
                }
            }
        }
    }

    @FXML
    private void onCancelBookingTime() {
        String playerName = playerNameField.getText().trim();
        if (selectedTime == null || playerName.isEmpty()) {
            showAlert("Error", "Please select a tee time and enter a player name.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray bookingTimes = (JSONArray) bookingSheetData.get(formattedDate);
        for (Object obj : bookingTimes) {
            JSONObject bookingTimeObj = (JSONObject) obj;
            if (bookingTimeObj.get("time").equals(selectedTime.split(" ")[0])) {
                JSONArray players = (JSONArray) bookingTimeObj.get("players");
                if (players.contains(playerName)) {
                    players.remove(playerName);
                    saveBookingSheet();
                    populateBookingListView(formattedDate);
                    playerNameField.clear();
                    showAlert("Confirmation", "Time successfully cancelled");
                    return;
                } else {
                    showAlert("Error", "Player not found in this time.");
                    return;
                }
            }
        }
    }

    @FXML
    private void onShowPlayers() {
        if (selectedTime == null) {
            showAlert("Error", "Please select a time to view players.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            showAlert("Error", "Please select a date.");
            return;
        }

        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        JSONArray bookingTimes = (JSONArray) bookingSheetData.get(formattedDate);
        for (Object obj : bookingTimes) {
            JSONObject bookingTimeObj = (JSONObject) obj;
            if (bookingTimeObj.get("time").equals(selectedTime.split(" ")[0])) {
                JSONArray players = (JSONArray) bookingTimeObj.get("players");
                StringBuilder playerList = new StringBuilder("Players:\n");
                for (Object player : players) {
                    playerList.append(player.toString()).append("\n");
                }
                showAlert("Players in " + selectedTime, playerList.toString());
                return;
            }
        }
    }

    private void loadBookingSheet() {
        try (FileReader reader = new FileReader(bookingFilePath)) {
            JSONParser parser = new JSONParser();
            bookingSheetData = (JSONObject) parser.parse(reader);
            System.out.println("Tee sheet loaded successfully.");
        } catch (IOException | ParseException e) {
            bookingSheetData = new JSONObject();
            System.out.println("Booking file not found or invalid, starting with an empty sheet.");
        }
    }

    private void saveBookingSheet() {
        try (FileWriter writer = new FileWriter(bookingFilePath)) {
            writer.write(bookingSheetData.toJSONString());
            writer.flush();
            System.out.println("Bookings saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save bookings: " + e.getMessage());
        }
    }

    private void populateBookingListView(String date) {
        JSONArray bookingTimes = (JSONArray) bookingSheetData.get(date);
        if (bookingTimes == null) {
            bookingTimes = initializeBookingsForDate(date);
            bookingSheetData.put(date, bookingTimes);
            saveBookingSheet();
        }

        ObservableList<String> items = FXCollections.observableArrayList();

        if (currentUserRole == Role.MANAGER) {
            // Show all times
            for (Object obj : bookingTimes) {
                JSONObject bookingTimeObj = (JSONObject) obj;
                String time = (String) bookingTimeObj.get("time");
                JSONArray players = (JSONArray) bookingTimeObj.get("players");
                items.add(time + " (" + players.size() + "/12 booked)"); // Add all times without filtering
            }
        } else if (currentUserRole == Role.MEMBER) {
            // Show member's times and empty times
            String memberName = getCurrentUserName(); // Get the current member's name
            for (Object obj : bookingTimes) {
                JSONObject bookingTimeObj = (JSONObject) obj;
                String time = (String) bookingTimeObj.get("time");
                JSONArray players = (JSONArray) bookingTimeObj.get("players");
                if (players.contains(memberName) || players.isEmpty()) {
                    items.add(time + " (" + players.size() + "/12 booked)");
                }
            }
        }

        bookingListView.setItems(items);
    }



    private JSONArray initializeBookingsForDate(String date) {
        JSONArray bookingTimes = new JSONArray();
        int startHour = 7;
        int endHour = 17;
        int intervalMinutes = 10;

        for (int hour = startHour; hour <= endHour; hour++) {
            for (int minute = 0; minute < 60; minute += intervalMinutes) {
                String time = String.format("%02d:%02d", hour, minute);
                JSONObject bookingTime = new JSONObject();
                bookingTime.put("time", time);
                bookingTime.put("players", new JSONArray());
                bookingTimes.add(bookingTime);
            }
        }
        return bookingTimes;
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