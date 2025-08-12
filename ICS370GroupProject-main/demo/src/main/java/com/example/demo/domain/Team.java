package com.example.demo.domain;

import java.util.Objects;

public class Team {

    private int id;
    private String name;

    /**
     * Constructor to create a new Team object.
     *
     * @param id The unique ID of the team from the database.
     * @param name The name of the team.
     */
    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // --- Setters ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Provides a user-friendly string representation of the team.
     * In this case, just the team's name is sufficient.
     *
     * @return The name of the team.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Checks if two Team objects are equal based on their unique ID.
     *
     * @param o The object to compare against.
     * @return true if the teams have the same ID, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id == team.id;
    }

    /**
     * Generates a hash code for the Team object based on its unique ID.
     * This is important for storing objects in hash-based collections like HashSets.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}