package com.example.demo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    public enum Role {
        MEMBER,
        MANAGER
    }

    private static String loggedInUsername;
    private static Integer loggedInUserId;


    public static User authenticate(String username, String password) {
        // --- MODIFICATION: Select the 'id' column as well ---
        String sql = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // --- MODIFICATION: Store both the user's ID and username ---
                    loggedInUserId = rs.getInt("id");
                    loggedInUsername = username;

                    String roleString = rs.getString("role");
                    Role role = Role.valueOf(roleString.toUpperCase());

                    return new User(username, role);
                }
            }
        } catch (SQLException e) {
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
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Role.valueOf(rs.getString("role").toUpperCase());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Role.MEMBER; // Default role
    }

    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, Role.MEMBER.name()); // Hardcode the role to MEMBER
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer getLoggedInUserId() {
        return loggedInUserId;
    }


    public static void logout() {
        // --- MODIFICATION: Clear the ID on logout as well ---
        loggedInUsername = null;
        loggedInUserId = null;
    }
}
