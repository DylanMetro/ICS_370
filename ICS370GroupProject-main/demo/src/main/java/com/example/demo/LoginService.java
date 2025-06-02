package com.example.demo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class LoginService {

    public enum Role {
        MEMBER,
        MANAGER
    }

    private static String loggedInUsername;

    public static User authenticate(String username, String password) {
        JSONParser parser = new JSONParser();
        try {
            // Updated path to resolve users.json correctly
            String filePath = Paths.get("demo", "users.json").toAbsolutePath().toString();
            try (FileReader reader = new FileReader(filePath)) {
                // Parse the file as an array
                JSONArray users = (JSONArray) parser.parse(reader);

                for (Object userObj : users) {
                    JSONObject user = (JSONObject) userObj;
                    String readUsername = (String) user.get("username");
                    String readPassword = (String) user.get("password");

                    if (readUsername.equals(username) && readPassword.equals(password)) {
                        String roleString = (String) user.get("role");
                        Role role = Role.valueOf(roleString.toUpperCase());
                        loggedInUsername = readUsername;
                        return new User(username, role);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null; // Authentication failed
    }

    public static class User {
        public String username;
        public Role role;

        public User(String username, Role role) {
            this.username = username;
            this.role = role;
        }
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static Role getRole(String username) {
        String filePath = Paths.get("demo", "users.json").toAbsolutePath().toString();
        try (FileReader reader = new FileReader(filePath)) {
            JSONParser parser = new JSONParser();
            JSONArray users = (JSONArray) parser.parse(reader);

            for (Object obj : users) {
                JSONObject user = (JSONObject) obj;
                String userFromJson = (String) user.get("username");

                if (userFromJson.equals(username)) {
                    String roleObj = (String) user.get("role"); // Get the role as JSONObject
                    //String roleString = roleObj.toString();
                    return Role.valueOf(roleObj.toUpperCase());
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., show an error message)
        }

        return Role.MEMBER; // Default to MEMBER role if there's an error or user not found
    }
    public static void logout() {
        loggedInUsername = null; // Clear the current session
    }
}
