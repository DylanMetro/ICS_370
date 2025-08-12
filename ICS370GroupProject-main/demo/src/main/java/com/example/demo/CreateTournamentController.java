package com.example.demo;

import com.example.demo.SportSelectionController.Sport;
import com.example.demo.domain.Tournament;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import com.example.demo.domain.Team;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class CreateTournamentController {

    // --- FXML Fields for UI components ---
    @FXML
    private TextField tournamentNameField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<Tournament> tournamentsTable;
    @FXML
    private TableColumn<Tournament, String> tournamentNameColumn;
    @FXML
    private TableColumn<Tournament, LocalDate> tournamentDateColumn;
    @FXML
    private TableColumn<Tournament, String> tournamentStatusColumn;
    @FXML
    private Button deleteTournamentButton;

    private Sport selectedSport;

    /**
     * Initializes the controller and sets up the TableView columns.
     */
    @FXML
    public void initialize() {
        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tournamentDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        tournamentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    /**
     * Receives the selected sport from the previous screen and loads all associated tournaments.
     * @param sport The sport selected by the manager.
     */
    public void initData(Sport sport) {
        this.selectedSport = sport;
        loadAllTournaments();
    }

    /**
     * Loads all tournaments for the selected sport from the database into the TableView.
     */
    private void loadAllTournaments() {
        if (selectedSport == null) return;
        ObservableList<Tournament> allTournaments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tournaments WHERE sport_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedSport.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                allTournaments.add(new Tournament(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("sport_id"),
                        rs.getDate("start_date").toLocalDate(),
                        Tournament.Status.valueOf(rs.getString("status"))
                ));
            }
            tournamentsTable.setItems(allTournaments);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading tournaments.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Handles the "Create Tournament" button action.
     */
    @FXML
    private void createTournament() {
        String tournamentName = tournamentNameField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();

        if (tournamentName.isEmpty() || startDate == null) {
            statusLabel.setText("Tournament name and start date are required.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String sql = "INSERT INTO tournaments (name, sport_id, start_date, status) VALUES (?, ?, ?, 'REGISTRATION_OPEN')";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tournamentName);
            pstmt.setInt(2, selectedSport.getId());
            pstmt.setDate(3, java.sql.Date.valueOf(startDate));
            pstmt.executeUpdate();

            statusLabel.setText("Tournament created successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            tournamentNameField.clear();
            startDatePicker.setValue(null);

            loadAllTournaments(); // Refresh the table to show the new tournament
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Failed to create tournament.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Handles the "Delete Selected Tournament" button action.
     * Includes a confirmation dialog and deletes the tournament and all its related data.
     */
    @FXML
    private void deleteTournament() {
        Tournament selected = tournamentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Please select a tournament to delete.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Tournament");
        confirmation.setHeaderText("Are you sure you want to delete '" + selected.getName() + "'?");
        confirmation.setContentText("This will delete all associated matches and team registrations. This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Proceed with deletion
            performDeletion(selected);
        }
    }

    private void performDeletion(Tournament tournament) {
        String deleteMatchesSql = "DELETE FROM matches WHERE tournament_id = ?";
        String deleteRegistrationsSql = "DELETE FROM tournament_registrations WHERE tournament_id = ?";
        String deleteTournamentSql = "DELETE FROM tournaments WHERE id = ?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement delMatches = conn.prepareStatement(deleteMatchesSql);
                 PreparedStatement delRegs = conn.prepareStatement(deleteRegistrationsSql);
                 PreparedStatement delTourn = conn.prepareStatement(deleteTournamentSql)) {

                // 1. Delete dependent records from 'matches'
                delMatches.setInt(1, tournament.getId());
                delMatches.executeUpdate();

                // 2. Delete dependent records from 'tournament_registrations'
                delRegs.setInt(1, tournament.getId());
                delRegs.executeUpdate();

                // 3. Delete the tournament itself
                delTourn.setInt(1, tournament.getId());
                delTourn.executeUpdate();

                conn.commit(); // Commit all changes if successful
                statusLabel.setText("Tournament deleted successfully.");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadAllTournaments(); // Refresh the table

            } catch (SQLException e) {
                conn.rollback(); // Rollback all changes on error
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Failed to delete tournament.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void generateMatches() {
        Tournament selectedTournament = tournamentsTable.getSelectionModel().getSelectedItem();
        if (selectedTournament == null) {
            statusLabel.setText("Please select a tournament to generate matches for.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // --- Confirmation Dialog ---
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Generate Matches");
        confirmation.setHeaderText("Generate match schedule for '" + selectedTournament.getName() + "'?");
        confirmation.setContentText("This will create a round-robin schedule. This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // --- Fetch Registered Teams ---
            List<Team> registeredTeams = new ArrayList<>();
            String fetchTeamsSql = "SELECT t.id, t.name FROM teams t JOIN tournament_registrations tr ON t.id = tr.team_id WHERE tr.tournament_id = ?";

            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fetchTeamsSql)) {

                pstmt.setInt(1, selectedTournament.getId());
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    registeredTeams.add(new Team(rs.getInt("id"), rs.getString("name")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error fetching registered teams.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (registeredTeams.size() < 2) {
                statusLabel.setText("At least two teams must be registered to generate matches.");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // --- Generate and Insert Matches ---
            String insertMatchSql = "INSERT INTO matches (tournament_id, team1_id, team2_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertMatchSql)) {

                conn.setAutoCommit(false); // Start transaction

                // Round-robin algorithm
                for (int i = 0; i < registeredTeams.size(); i++) {
                    for (int j = i + 1; j < registeredTeams.size(); j++) {
                        Team team1 = registeredTeams.get(i);
                        Team team2 = registeredTeams.get(j);

                        pstmt.setInt(1, selectedTournament.getId());
                        pstmt.setInt(2, team1.getId());
                        pstmt.setInt(3, team2.getId());
                        pstmt.addBatch(); // Add to batch for efficient insertion
                    }
                }
                pstmt.executeBatch(); // Execute all insert statements
                conn.commit(); // Commit the transaction

                statusLabel.setText("Match schedule generated successfully!");
                statusLabel.setStyle("-fx-text-fill: green;");

            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error generating matches.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/login-view.fxml"));
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