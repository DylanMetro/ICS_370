package com.example.demo.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Match {

    private int id;
    private int tournamentId;

    private int team1Id;
    private int team2Id;

    // These fields are included for display convenience. They would be populated
    // by joining the 'matches' table with the 'teams' table in your SQL query.
    private String team1Name;
    private String team2Name;

    private LocalDateTime matchDate;

    // Use the Integer wrapper class to allow for null scores if a match hasn't been played
    private Integer team1Score;
    private Integer team2Score;

    public Match(int id, int tournamentId, int team1Id, String team1Name, int team2Id, String team2Name, LocalDateTime matchDate, Integer team1Score, Integer team2Score) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.team1Id = team1Id;
        this.team1Name = team1Name;
        this.team2Id = team2Id;
        this.team2Name = team2Name;
        this.matchDate = matchDate;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getTournamentId() { return tournamentId; }
    public int getTeam1Id() { return team1Id; }
    public String getTeam1Name() { return team1Name; }
    public int getTeam2Id() { return team2Id; }
    public String getTeam2Name() { return team2Name; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public Integer getTeam1Score() { return team1Score; }
    public Integer getTeam2Score() { return team2Score; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public void setTeam1Id(int team1Id) { this.team1Id = team1Id; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }
    public void setTeam2Id(int team2Id) { this.team2Id = team2Id; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public void setTeam1Score(Integer team1Score) { this.team1Score = team1Score; }
    public void setTeam2Score(Integer team2Score) { this.team2Score = team2Score; }

    /**
     * Provides a detailed string representation of the match, perfect for a ListView.
     * It includes team names, the match date, and the score if available.
     *
     * @return A formatted string describing the match.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String dateString = (matchDate != null) ? matchDate.format(formatter) : "Date TBD";

        String scoreString;
        if (team1Score != null && team2Score != null) {
            scoreString = " - Score: " + team1Score + " to " + team2Score;
        } else {
            scoreString = " - Not Played";
        }

        return team1Name + " vs. " + team2Name + " (" + dateString + ")" + scoreString;
    }

    /**
     * Checks for equality based on the unique match ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return id == match.id;
    }

    /**
     * Generates a hash code based on the unique match ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}