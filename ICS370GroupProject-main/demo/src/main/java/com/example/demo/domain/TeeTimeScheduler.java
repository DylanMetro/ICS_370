package com.example.demo.domain;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeeTimeScheduler {
    private final Map<String, List<TeeTime>> schedule = new HashMap<>();

    public TeeTimeScheduler() {
        loadTeeTimesFromFile();
    }

    private void loadTeeTimesFromFile() {
        JSONParser parser = new JSONParser();
        try {
            String filePath = Paths.get("demo", "teeSheet.json").toAbsolutePath().toString();
            System.out.println("Loading tee times from: " + filePath);
            try (FileReader reader = new FileReader(filePath)) {
                JSONArray daysArray = (JSONArray) parser.parse(reader);
                for (Object dayObj : daysArray) {
                    JSONObject day = (JSONObject) dayObj;
                    String dayOfWeek = (String) day.get("day");
                    JSONArray teeTimesArray = (JSONArray) day.get("teeTimes");

                    List<TeeTime> teeTimesForDay = new ArrayList<>();
                    for (Object teeTimeObj : teeTimesArray) {
                        JSONObject teeTime = (JSONObject) teeTimeObj;
                        String time = (String) teeTime.get("time");
                        JSONArray playersArray = (JSONArray) teeTime.get("players");
                        List<String> players = new ArrayList<>(playersArray);

                        TeeTime teeTimeInstance = new TeeTime(time);
                        for (String player : players) {
                            teeTimeInstance.addPlayer(player);
                        }
                        teeTimesForDay.add(teeTimeInstance);
                    }
                    schedule.put(dayOfWeek, teeTimesForDay);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTeeTimesForDay(String dayOfWeek) {
        List<String> teeTimeStrings = new ArrayList<>();
        List<TeeTime> teeTimes = schedule.get(dayOfWeek);
        if (teeTimes != null) {
            for (TeeTime teeTime : teeTimes) {
                teeTimeStrings.add(teeTime.getTime() + " (" + teeTime.getPlayers().size() + "/4 booked)");
            }
        }
        return teeTimeStrings;
    }

    public boolean bookTeeTime(String dayOfWeek, String time, String playerName) {
        List<TeeTime> teeTimes = schedule.get(dayOfWeek);
        if (teeTimes != null) {
            for (TeeTime teeTime : teeTimes) {
                if (teeTime.getTime().equals(time) && !teeTime.isFull()) {
                    teeTime.addPlayer(playerName);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean cancelBooking(String dayOfWeek, String time, String playerName) {
        List<TeeTime> teeTimes = schedule.get(dayOfWeek);
        if (teeTimes != null) {
            for (TeeTime teeTime : teeTimes) {
                if (teeTime.getTime().equals(time)) {
                    return teeTime.removePlayer(playerName);
                }
            }
        }
        return false;
    }
}
