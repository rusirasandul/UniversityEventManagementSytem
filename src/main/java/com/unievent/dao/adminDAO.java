package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.Event; // Assuming you have an Event model
import com.unievent.model.User;  // Assuming you have a User model
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    // 1. ADMIN LOGIN (Checks both User table and Admin table)
    public boolean validateAdmin(String email, String password) {
        // Note: In production, password should be hashed!
        String sql = "SELECT u.user_id FROM User u " +
                "JOIN Admin a ON u.user_id = a.user_id " +
                "WHERE u.email = ? AND u.password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a match is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. GET DASHBOARD STATS (Total Users, Pending Events, etc.)
    public int[] getSystemStats() {
        int[] stats = new int[3]; // [0]=Pending Events, [1]=Total Users, [2]=Total Events

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Count Pending Events
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) FROM Event WHERE status = 'PENDING'");
            if (rs1.next()) stats[0] = rs1.getInt(1);

            // Count Total Users
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM User");
            if (rs2.next()) stats[1] = rs2.getInt(1);

            // Count Total Events
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM Event");
            if (rs3.next()) stats[2] = rs3.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // 3. FETCH PENDING EVENTS (For Approval Queue)
    public List<Event> getPendingEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM Event WHERE status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event e = new Event();
                e.setEventId(rs.getInt("event_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setStart(rs.getString("start"));
                // Add other setters as needed
                events.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    // 4. THREAD-SAFE APPROVE OR REJECT EVENT (Optimistic Locking)
    // Returns: "SUCCESS", "ALREADY_HANDLED", or "ERROR"
    public String updateEventStatus(int eventId, String newStatus) {
        // Senior Trick: Only update if the status is currently 'PENDING'
        // This prevents race conditions when multiple admins click simultaneously
        String sql = "UPDATE Event SET status = ? WHERE event_id = ? AND status = 'PENDING'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newStatus); // 'APPROVED' or 'REJECTED'
            stmt.setInt(2, eventId);
            
            int rowsUpdated = stmt.executeUpdate();
            
            if (rowsUpdated > 0) {
                return "SUCCESS";
            } else {
                // If rowsUpdated is 0, it means the event was NOT pending anymore
                // (Maybe Admin B already handled it!)
                return "ALREADY_HANDLED";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
