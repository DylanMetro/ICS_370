package com.example.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class BookingTime {
    private final String time;
    private final List<String> participants;
    private final String venueID;
    private final String activityType;
    private int maxParticipants;

    public BookingTime(String time, String venueID, String activityType, int maxParticipants) {
        this.time = time;
        this.participants = new ArrayList<>();
        this.venueID = venueID;
        this.activityType = activityType;
        this.maxParticipants = maxParticipants;
    }

    public String getTime() {
        return time;
    }

    public List<String> getPlayers() {
        return participants;
    }

    public String getVenueID() { return venueID; }
    public String getActivityType() { return activityType; }
    public int getMaxParticipants() { return maxParticipants; }

    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }

    public void addPlayer(String playerName) {
        if (!isFull()) {
            participants.add(playerName);
        }
    }

    public boolean removePlayer(String playerName) {
        return participants.remove(playerName);
    }
}
