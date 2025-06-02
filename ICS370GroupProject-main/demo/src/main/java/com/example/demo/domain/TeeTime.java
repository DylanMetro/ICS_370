package com.example.demo.domain;

import java.util.ArrayList;
import java.util.List;

public class TeeTime {
    private final String time;
    private final List<String> players;

    public TeeTime(String time) {
        this.time = time;
        this.players = new ArrayList<>();
    }

    public String getTime() {
        return time;
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean isFull() {
        return players.size() >= 4;
    }

    public void addPlayer(String playerName) {
        if (!isFull()) {
            players.add(playerName);
        }
    }

    public boolean removePlayer(String playerName) {
        return players.remove(playerName);
    }
}
