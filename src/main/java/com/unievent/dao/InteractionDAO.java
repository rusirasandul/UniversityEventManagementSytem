package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.Comment;
import com.unievent.model.Attends;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Interaction DAO for Comments and RSVPs
 * Demonstrates JOIN queries and UPSERT operations
 */
public class InteractionDAO {

    // 1. THREAD-SAFE: Fetch Comments with User Names (JOIN Query)
    public List<Comment> getCommentsForEvent(int eventId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.content, c.posted_at, " +
                     "u.user_name, u.user_id " +
                     "FROM Comment c " +
                     "JOIN User u ON c.user_id = u.user_id " +
                     "WHERE c.event_id = ? " +
                     "ORDER BY c.posted_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment c = new Comment();
                c.setCommentId(rs.getInt("comment_id"));
                c.setContent(rs.getString("content"));
                c.setTimestamp(rs.getTimestamp("posted_at"));
                c.setUserId(rs.getInt("user_id"));
                c.setAuthorName(rs.getString("user_name")); // From JOIN
                comments.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // 2. Add Comment
    public boolean addComment(int userId, int eventId, String content) {
        String sql = "INSERT INTO Comment (user_id, event_id, content, posted_at) " +
                     "VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            stmt.setString(3, content);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. THREAD-SAFE: Toggle RSVP (UPSERT - MySQL's "ON DUPLICATE KEY UPDATE")
    // This handles both "New RSVP" and "Update RSVP" in a single query!
    public boolean toggleRSVP(int userId, int eventId, String status) {
        // MySQL Superpower: If (user_id, event_id) already exists, UPDATE instead of INSERT
        String sql = "INSERT INTO Attends (user_id, event_id, rsvp_status, rsvp_date) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE rsvp_status = VALUES(rsvp_status), rsvp_date = NOW()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            stmt.setString(3, status); // 'GOING', 'NOT_GOING', 'MAYBE'
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Get User's RSVP Status for an Event
    public String getUserRSVPStatus(int userId, int eventId) {
        String sql = "SELECT rsvp_status FROM Attends WHERE user_id = ? AND event_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("rsvp_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // User hasn't RSVP'd yet
    }

    // 5. Get All Attendees for an Event
    public List<Attends> getEventAttendees(int eventId) {
        List<Attends> attendees = new ArrayList<>();
        String sql = "SELECT a.*, u.user_name, u.email " +
                     "FROM Attends a " +
                     "JOIN User u ON a.user_id = u.user_id " +
                     "WHERE a.event_id = ? AND a.rsvp_status = 'GOING' " +
                     "ORDER BY a.rsvp_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attends a = new Attends();
                a.setUserId(rs.getInt("user_id"));
                a.setEventId(rs.getInt("event_id"));
                a.setRsvpStatus(rs.getString("rsvp_status"));
                a.setRsvpDate(rs.getTimestamp("rsvp_date"));
                a.setUserName(rs.getString("user_name")); // From JOIN
                attendees.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendees;
    }

    // 6. Check if Event is Full (Capacity Check)
    public boolean isEventFull(int eventId) {
        String sql = "SELECT e.max_attendees, COUNT(a.user_id) as current_count " +
                     "FROM Event e " +
                     "LEFT JOIN Attends a ON e.event_id = a.event_id AND a.rsvp_status = 'GOING' " +
                     "WHERE e.event_id = ? " +
                     "GROUP BY e.event_id, e.max_attendees";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int maxAttendees = rs.getInt("max_attendees");
                int currentCount = rs.getInt("current_count");
                return currentCount >= maxAttendees;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
