package com.example.demo.domain;

import java.time.LocalDate;

public class Tournament {

    /**
     * Represents the current state of the tournament.
     */
    public enum Status {
        REGISTRATION_OPEN,
        IN_PROGRESS,
        COMPLETED
    }

    private int id;
    private String name;
    private int sportId;
    private LocalDate startDate;
    private Status status;

    /**
     * Constructor to create a new Tournament object.
     *
     * @param id The unique ID of the tournament.
     * @param name The name of the tournament.
     * @param sportId The ID of the sport this tournament is for.
     * @param startDate The date the tournament begins.
     * @param status The current status of the tournament.
     */
    public Tournament(int id, String name, int sportId, LocalDate startDate, Status status) {
        this.id = id;
        this.name = name;
        this.sportId = sportId;
        this.startDate = startDate;
        this.status = status;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSportId() {
        return sportId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Status getStatus() {
        return status;
    }

    // --- Setters ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSportId(int sportId) {
        this.sportId = sportId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Provides a user-friendly string representation of the tournament,
     * useful for displaying in UI components like a ListView.
     *
     * @return A formatted string with the tournament name and start date.
     */
    @Override
    public String toString() {
        return name + " (Starts: " + startDate + ")";
    }
}