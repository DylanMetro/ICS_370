package com.example.demo;

import com.example.demo.SportSelectionController.Sport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

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

public class BookingController {

    // FXML fields for UI components
    @FXML
    private DatePicker datePicker;
    @FXML
    private ListView<String> bookingListView;
    @FXML
    private Label statusLabel;

    // Instance variables to hold state
    private Sport selectedSport;
    private String selectedTime;
    private final String currentUser = LoginService.getLoggedInUsername();
    private final LoginService.Role currentUserRole = LoginService.getRole(currentUser);


    /**
     * Receives the selected sport from a previous screen to provide context.
     * @param sport The sport for which bookings are being managed.
     */
    public void initData(Sport sport) {
        this.selectedSport = sport;
    }

    /**
     * Initializes the controller, setting up listeners for UI components.
     */
    @FXML
    public void initialize() {
        datePicker.setOnAction(event -> onDateSelected());
        bookingListView.setOnMouseClicked(this::onTimeSelected);
    }

    /**
     * Handles date selection from the DatePicker, triggering the booking list to populate.
     */
    @FXML
    private void onDateSelected() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            populateBookingListView(selectedDate);
        }
    }

    /**
     * Captures the selected time slot string from the ListView.
     */
    @FXML
    private void onTimeSelected(MouseEvent event) {
        selectedTime = bookingListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Handles the "Book Time" button action. It uses the currently logged-in user's
     * name to create the booking.
     */
    @FXML
    private void onBookTime() {
        String playerName = LoginService.getLoggedInUsername();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedTime == null || playerName == null || selectedDate == null) {
            showAlert("Error", "Please select a date and time to book.");
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

            if (rs.next() && rs.getInt("count") >= selectedSport.getMaxPlayers()) {
                showAlert("Error", "This time is fully booked for this sport.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error while checking booking count.");
            return;
        }

        // Insert the new booking
        String insertSql = "INSERT INTO bookings (booking_date, booking_time, player_name, sport_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setString(3, playerName);
            pstmt.setInt(4, selectedSport.getId());
            pstmt.executeUpdate();

            showAlert("Confirmation", "Time booked successfully");
            populateBookingListView(selectedDate);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to book the time. You may already be booked for another time.");
        }
    }

    /**
     * Handles the "Cancel My Booking" button action. It attempts to cancel the booking
     * for the currently logged-in user at the selected time slot.
     */
    @FXML
    private void onCancelBookingTime() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedTime == null || selectedDate == null) {
            showAlert("Error", "Please select a time slot to cancel.");
            return;
        }

        String time = selectedTime.split(" ")[0];

        // --- Role-based logic starts here ---
        if (currentUserRole == LoginService.Role.MANAGER) {
            // For Managers: Prompt for the player name to cancel
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Manager Cancellation");
            dialog.setHeaderText("Cancel Booking for " + time);
            dialog.setContentText("Enter the name of the player to cancel:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(playerName -> {
                if (playerName.trim().isEmpty()) {
                    showAlert("Error", "Player name cannot be empty.");
                    return;
                }
                // Execute cancellation for the specified player
                cancelBookingForPlayer(playerName.trim(), selectedDate, time);
            });

        } else {
            // For Members: Cancel their own booking automatically
            String playerName = LoginService.getLoggedInUsername();
            cancelBookingForPlayer(playerName, selectedDate, time);
        }
    }

    private void cancelBookingForPlayer(String playerName, LocalDate date, String time) {
        String sql = "DELETE FROM bookings WHERE booking_date = ? AND booking_time = ? AND player_name = ? AND sport_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setString(3, playerName);
            pstmt.setInt(4, selectedSport.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert("Confirmation", "Booking for '" + playerName + "' has been cancelled.");
                populateBookingListView(date);
            } else {
                showAlert("Error", "No booking found for player '" + playerName + "' at this time slot.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel booking due to a database error.");
        }
    }

    /**
     * Shows a list of all players booked for the selected time slot.
     */
    @FXML
    private void onShowPlayers() {
        if (selectedTime == null || datePicker.getValue() == null) {
            showAlert("Error", "Please select a date and time to view players.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        String time = selectedTime.split(" ")[0];
        List<String> players = new ArrayList<>();
        String sql = "SELECT player_name FROM bookings WHERE booking_date = ? AND booking_time = ? AND sport_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(selectedDate));
            pstmt.setTime(2, java.sql.Time.valueOf(LocalTime.parse(time)));
            pstmt.setInt(3, selectedSport.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    players.add(rs.getString("player_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to retrieve player list.");
            return;
        }

        StringBuilder playerList = new StringBuilder("Players for " + time + ":\n");
        if (players.isEmpty()) {
            playerList.append("No players booked for this time.");
        } else {
            for (String player : players) {
                playerList.append(player).append("\n");
            }
        }
        showAlert("Players", playerList.toString());
    }

    /**
     * Populates the ListView with all available time slots for a given date and sport,
     * showing how many players are booked for each slot.
     * @param date The date to load bookings for.
     */
    private void populateBookingListView(LocalDate date) {
        ObservableList<String> items = FXCollections.observableArrayList();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int hour = 7; hour <= 17; hour++) {
            for (int minute = 0; minute < 60; minute += 10) {
                LocalTime time = LocalTime.of(hour, minute);
                int playerCount = 0;

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

                items.add(String.format("%s (%d/%d booked)",
                        time.format(timeFormatter),
                        playerCount,
                        selectedSport.getMaxPlayers()
                ));
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

    // --- Navigation Methods ---

    @FXML
    private void goToHomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/home-view.fxml"));
            Parent root = loader.load();
            HomeController homeController = loader.getController();
            homeController.setSelectedSport(selectedSport);
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(selectedSport.getName() + " Home");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void changeSport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example.demo/sport-selection-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Select Sport");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        LoginService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example.demo/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}