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

public class BookingScheduler { //
    private final Map<String, List<BookingTime>> schedule = new HashMap<>(); //

    public BookingScheduler() { //
        loadBookingsFromFile(); //
    }

    private void loadBookingsFromFile() { //
        JSONParser parser = new JSONParser(); //
        try { //
            String filePath = Paths.get("demo", "bookingSheet.json").toAbsolutePath().toString(); //
            System.out.println("Loading bookings from: " + filePath); //
            try (FileReader reader = new FileReader(filePath)) { //
                JSONArray daysArray = (JSONArray) parser.parse(reader); //
                for (Object dayObj : daysArray) { //
                    JSONObject day = (JSONObject) dayObj; //
                    String dayOfWeek = (String) day.get("day"); //
                    JSONArray bookingTimesArray = (JSONArray) day.get("bookingTimes"); //

                    List<BookingTime> bookingTimesForDay = new ArrayList<>(); //
                    for (Object bookingTimeObj : bookingTimesArray) { //
                        JSONObject bookingTime = (JSONObject) bookingTimeObj; //
                        String time = (String) bookingTime.get("time"); //
                        JSONArray playersArray = (JSONArray) bookingTime.get("players"); //
                        List<String> players = new ArrayList<>(playersArray); //

                        // Extract the new fields: venueID, activityType, maxParticipants
                        String venueID = (String) bookingTime.get("venueID");
                        String activityType = (String) bookingTime.get("activityType");
                        // JSON simple parses numbers as Long by default, so cast to Long first
                        Long maxParticipantsLong = (Long) bookingTime.get("maxParticipants");
                        int maxParticipants = (maxParticipantsLong != null) ? maxParticipantsLong.intValue() : 0; // Provide a default if null

                        // Pass all required arguments to the BookingTime constructor
                        BookingTime bookingTimeInstance = new BookingTime(time, venueID, activityType, maxParticipants);
                        for (String player : players) { //
                            bookingTimeInstance.addPlayer(player); //
                        }
                        bookingTimesForDay.add(bookingTimeInstance); //
                    }
                    schedule.put(dayOfWeek, bookingTimesForDay); //
                }
            }
        } catch (IOException | ParseException e) { //
            e.printStackTrace(); //
        }
    }

    public List<String> getBookingTimesForDay(String dayOfWeek) { //
        List<String> bookingTimeStrings = new ArrayList<>(); //
        List<BookingTime> bookingTimes = schedule.get(dayOfWeek); //
        if (bookingTimes != null) { //
            for (BookingTime bookingTime : bookingTimes) { //
                // Use getMaxParticipants() from the BookingTime object
                bookingTimeStrings.add(bookingTime.getTime() + " (" + bookingTime.getPlayers().size() + "/" + bookingTime.getMaxParticipants() + " booked)");
            }
        }
        return bookingTimeStrings; //
    }

    public boolean bookTime(String dayOfWeek, String time, String playerName) { //
        List<BookingTime> bookingTimes = schedule.get(dayOfWeek); //
        if (bookingTimes != null) { //
            for (BookingTime bookingTime : bookingTimes) { //
                if (bookingTime.getTime().equals(time) && !bookingTime.isFull()) { //
                    bookingTime.addPlayer(playerName); //
                    return true; //
                }
            }
        }
        return false; //
    }

    public boolean cancelBooking(String dayOfWeek, String time, String playerName) { //
        List<BookingTime> bookingTimes = schedule.get(dayOfWeek); //
        if (bookingTimes != null) { //
            for (BookingTime bookingTime : bookingTimes) { //
                if (bookingTime.getTime().equals(time)) { //
                    return bookingTime.removePlayer(playerName); //
                }
            }
        }
        return false; //
    }
}