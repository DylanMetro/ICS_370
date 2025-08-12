package com.example.demo;

import com.example.demo.SportSelectionController.Sport;
import com.example.demo.domain.Match;
import com.example.demo.domain.Team;
import com.example.demo.domain.Tournament;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class TournamentViewController {

    // FXML fields for the UI components
    @FXML private TableView<Tournament> tournamentsTable;
    @FXML private TableColumn<Tournament, String> tournamentNameColumn;
    @FXML private TableColumn<Tournament, LocalDate> tournamentDateColumn;
    @FXML private Button registerTeamButton;

    @FXML private TableView<Match> matchesTable;
    @FXML private Label matchesLabel;
    @FXML private TableColumn<Match, String> team1Column;
    @FXML private TableColumn<Match, String> team2Column;
    @FXML private TableColumn<Match, String> matchDateColumn;
    @FXML private TableColumn<Match, String> scoreColumn;

    @FXML private TextField team1ScoreField;
    @FXML private TextField team2ScoreField;
    @FXML private Button submitScoreButton;

    @FXML private Label statusLabel;

    private Sport selectedSport;

    /**
     * Initializes the controller, setting up table columns and listeners.
     */
    @FXML
    public void initialize() {
        // Setup columns for the Tournaments table
        tournamentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        tournamentDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        // Setup columns for the Matches table
        team1Column.setCellValueFactory(new PropertyValueFactory<>("team1Name"));
        team2Column.setCellValueFactory(new PropertyValueFactory<>("team2Name"));
        matchDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMatchDate() != null ?
                        cellData.getValue().getMatchDate().toLocalDate().toString() : "TBD"
        ));
        scoreColumn.setCellValueFactory(cellData -> {
            Integer score1 = cellData.getValue().getTeam1Score();
            Integer score2 = cellData.getValue().getTeam2Score();
            if (score1 != null && score2 != null) {
                return new SimpleStringProperty(score1 + " - " + score2);
            }
            return new SimpleStringProperty("Not Played");
        });

        // Add a listener to load matches when a tournament is selected
        tournamentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadMatchesForTournament(newSelection);
                    } else {
                        matchesTable.getItems().clear();
                        matchesLabel.setText("Matches (Select a tournament to view)");
                    }
                });
    }

    /**
     * Receives the selected sport from the home screen and loads open tournaments.
     */
    public void initData(Sport sport) {
        this.selectedSport = sport;
        loadOpenTournaments();
    }

    /**
     * Loads tournaments that are open for registration for the selected sport.
     */
    private void loadOpenTournaments() {
        ObservableList<Tournament> openTournaments = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tournaments WHERE sport_id = ? AND status = 'REGISTRATION_OPEN'";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedSport.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                openTournaments.add(new Tournament(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("sport_id"),
                        rs.getDate("start_date").toLocalDate(),
                        Tournament.Status.valueOf(rs.getString("status"))
                ));
            }
            tournamentsTable.setItems(openTournaments);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load tournaments.");
        }
    }

    /**
     * Loads the match list for the given tournament.
     */
    private void loadMatchesForTournament(Tournament tournament) {
        matchesLabel.setText("Matches for: " + tournament.getName());
        ObservableList<Match> matches = FXCollections.observableArrayList();
        String sql = "SELECT m.id, m.tournament_id, m.team1_id, t1.name as team1_name, m.team2_id, t2.name as team2_name, m.match_date, m.team1_score, m.team2_score " +
                "FROM matches m " +
                "JOIN teams t1 ON m.team1_id = t1.id " +
                "JOIN teams t2 ON m.team2_id = t2.id " +
                "WHERE m.tournament_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tournament.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp matchTimestamp = rs.getTimestamp("match_date");
                matches.add(new Match(
                        rs.getInt("id"),
                        rs.getInt("tournament_id"),
                        rs.getInt("team1_id"),
                        rs.getString("team1_name"),
                        rs.getInt("team2_id"),
                        rs.getString("team2_name"),
                        matchTimestamp != null ? matchTimestamp.toLocalDateTime() : null,
                        (Integer) rs.getObject("team1_score"),
                        (Integer) rs.getObject("team2_score")
                ));
            }
            matchesTable.setItems(matches);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load matches.");
        }
    }

    /**
     * Handles the "Register Team" button action.
     */
    @FXML
    private void registerTeam() {
        Tournament selectedTournament = tournamentsTable.getSelectionModel().getSelectedItem();
        if (selectedTournament == null) {
            showAlert("Error", "Please select a tournament to register for.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Team Registration");
        dialog.setHeaderText("Register for: " + selectedTournament.getName());
        dialog.setContentText("Please enter your team name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(teamName -> {
            if (teamName.trim().isEmpty()) {
                showAlert("Error", "Team name cannot be empty.");
                return;
            }

            // --- MODIFICATION: Use the new method to get the correct user ID ---
            Integer userId = LoginService.getLoggedInUserId();
            if (userId == null) {
                showAlert("Error", "Could not verify user session. Please log in again.");
                return;
            }

            try {
                // Step 1: Insert into teams table
                int teamId = 0;
                String teamSql = "INSERT INTO teams (name) VALUES (?)";
                try (Connection conn = DatabaseConnector.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(teamSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, teamName.trim());
                    pstmt.executeUpdate();
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        teamId = generatedKeys.getInt(1);
                    }
                }

                // Step 2: Insert into tournament_registrations table
                if (teamId > 0) {
                    String regSql = "INSERT INTO tournament_registrations (tournament_id, team_id, captain_user_id) VALUES (?, ?, ?)";
                    try (Connection conn = DatabaseConnector.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(regSql)) {
                        pstmt.setInt(1, selectedTournament.getId());
                        pstmt.setInt(2, teamId);
                        // Use the correct userId variable here
                        pstmt.setInt(3, userId);
                        pstmt.executeUpdate();
                        showAlert("Success", "Team '" + teamName + "' registered successfully!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to register team. The team name may already be taken.");
            }
        });
    }

    /**
     * Handles the "Submit Score" button action.
     */
    @FXML
    private void submitScore() {
        Match selectedMatch = matchesTable.getSelectionModel().getSelectedItem();
        if (selectedMatch == null) {
            showAlert("Error", "Please select a match to submit a score for.");
            return;
        }

        try {
            int score1 = Integer.parseInt(team1ScoreField.getText().trim());
            int score2 = Integer.parseInt(team2ScoreField.getText().trim());

            String sql = "UPDATE matches SET team1_score = ?, team2_score = ? WHERE id = ?";
            try (Connection conn = DatabaseConnector.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, score1);
                pstmt.setInt(2, score2);
                pstmt.setInt(3, selectedMatch.getId());
                pstmt.executeUpdate();

                showAlert("Success", "Score submitted successfully!");
                // Refresh the matches table to show the new score
                loadMatchesForTournament(tournamentsTable.getSelectionModel().getSelectedItem());
                team1ScoreField.clear();
                team2ScoreField.clear();
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Scores must be valid numbers.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit score.");
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/sport-selection-view.fxml"));
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