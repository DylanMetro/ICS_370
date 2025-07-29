package com.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    @FXML
    private Label statusLabel;

    private SportSelectionController.Sport selectedSport;

    private String selectedTime;

    private final String currentUser = LoginService.getLoggedInUsername();
    private final LoginService.Role currentUserRole = LoginService.getRole(currentUser);

    @FXML
    public void initialize() {
        datePicker.setOnAction(event -> onDateSelected());
        bookingListView.setOnMouseClicked(this::onTimeSelected);
    }

    public void initData(SportSelectionController.Sport sport) {
        this.selectedSport = sport;
        // You might want to update a label to show the selected sport
    }

    @FXML
    private void onDateSelected() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            populateBookingListView(selectedDate);
        }
    }

    @FXML
    private void onTimeSelected(MouseEvent event) {
        selectedTime = bookingListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onBookTime() {
        String playerName = playerNameField.getText().trim();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedTime == null || playerName.isEmpty() || selectedDate == null) {
            showAlert("Error", "Please select a date, time, and enter a player name.");
            return;
        }

        String time = selectedTime.split(" ")[0];

        // Check if the time slot is full
        String countSql = "SELECT COUNT(*) AS count FROM bookings WHERE booking_date = ? AND booking_time = ? AND sport_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(countSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setInt(3, selectedSport.getId());
            ResultSet rs = pstmt.executeQuery();

            // Use the sport-specific max player limit
            if (rs.next() && rs.getInt("count") >= selectedSport.getMaxPlayers()) {
                showAlert("Error", "This time is fully booked for this sport.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error while checking booking count.");
            return;
        }

        // Insert the new booking with the sport_id
        String insertSql = "INSERT INTO bookings (booking_date, booking_time, player_name, sport_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setString(3, playerName);
            pstmt.setInt(4, selectedSport.getId()); // Add the sport_id
            pstmt.executeUpdate();

            showAlert("Confirmation", "Time booked successfully");
            populateBookingListView(selectedDate);
            playerNameField.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to book the time.");
        }
    }

    @FXML
    private void onCancelBookingTime() {
        String playerName = playerNameField.getText().trim();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedTime == null || playerName.isEmpty() || selectedDate == null) {
            showAlert("Error", "Please select a time and enter a player name to cancel.");
            return;
        }

        String time = selectedTime.split(" ")[0];

        String sql = "DELETE FROM bookings WHERE booking_date = ? AND booking_time = ? AND player_name = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setString(3, playerName);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Confirmation", "Booking successfully cancelled");
                populateBookingListView(selectedDate);
                playerNameField.clear();
            } else {
                showAlert("Error", "Player not found for this booking or the booking does not exist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel booking.");
        }
    }

    @FXML
    private void onShowPlayers() {
        if (selectedTime == null || datePicker.getValue() == null) {
            showAlert("Error", "Please select a date and time to view players.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        String time = selectedTime.split(" ")[0];
        List<String> players = new ArrayList<>();
        String sql = "SELECT player_name FROM bookings WHERE booking_date = ? AND booking_time = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    players.add(rs.getString("player_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve the player list.");
            return;
        }

        StringBuilder playerList = new StringBuilder("Players for " + time + ":\n");
        if (players.isEmpty()) {
            playerList.append("No players are booked for this time.");
        } else {
            for (String player : players) {
                playerList.append(player).append("\n");
            }
        }
        showAlert("Players", playerList.toString());
    }

    private void populateBookingListView(LocalDate date) {
        ObservableList<String> items = FXCollections.observableArrayList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Generate all possible time slots from 7:00 AM to 5:00 PM
        for (int hour = 7; hour <= 17; hour++) {
            for (int minute = 0; minute < 60; minute += 10) {
                LocalTime time = LocalTime.of(hour, minute);
                int playerCount = 0;

                // Get player count for the specific time and sport
                String countSql = "SELECT COUNT(*) FROM bookings WHERE booking_date = ? AND booking_time = ? AND sport_id = ?";
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    countStmt.setDate(1, java.sql.Date.valueOf(date));
                    countStmt.setTime(2, java.sql.Time.valueOf(time));
                    countStmt.setInt(3, selectedSport.getId());
                    ResultSet countRs = countStmt.executeQuery();
                    if (countRs.next()) {
                        playerCount = countRs.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (currentUserRole == LoginService.Role.MANAGER) {
                    items.add(String.format("%s (%d/%d booked)",
                            time.format(timeFormatter),
                            playerCount,
                            selectedSport.getMaxPlayers()
                    ));
                } else if (currentUserRole == LoginService.Role.MEMBER) {
                    boolean isPlayerBooked = false;
                    // Check if the current member is booked for this time and sport
                    String memberCheckSql = "SELECT COUNT(*) FROM bookings WHERE booking_date = ? AND booking_time = ? AND player_name = ? AND sport_id = ?";
                    try (Connection conn = DatabaseConnector.getConnection();
                         PreparedStatement memberStmt = conn.prepareStatement(memberCheckSql)) {
                        memberStmt.setDate(1, java.sql.Date.valueOf(date));
                        memberStmt.setTime(2, java.sql.Time.valueOf(time));
                        memberStmt.setString(3, currentUser);
                        memberStmt.setInt(4, selectedSport.getId());
                        ResultSet memberRs = memberStmt.executeQuery();
                        if (memberRs.next() && memberRs.getInt(1) > 0) {
                            isPlayerBooked = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (playerCount == 0 || isPlayerBooked) {
                        // Also use the sport's max players here
                        items.add(String.format("%s (%d/%d booked)",
                                time.format(timeFormatter),
                                playerCount,
                                selectedSport.getMaxPlayers()
                        ));
                    }
                }
            }
        }
        bookingListView.setItems(items);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Remaining navigation methods (goToHomeScreen, logout, navigateToLogin) are unchanged
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