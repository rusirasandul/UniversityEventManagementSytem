package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.Event;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Event DAO with Dynamic Search and Complex Joins
 * Demonstrates SQL query building based on user input
 */
public class EventAdvancedDAO {

    // 1. ADVANCED SEARCH: Filter by Category, Venue, Keyword (Dynamic SQL)
    public List<Event> searchEvents(Integer categoryId, Integer venueId, String keyword) {
        List<Event> events = new ArrayList<>();
        
        // Base Query with Joins to get names, not just IDs
        StringBuilder sql = new StringBuilder(
            "SELECT e.*, v.venue_name, c.cat_name, u.user_name as organizer_name, " +
            "COUNT(a.user_id) as attendee_count " +
            "FROM Event e " +
            "JOIN Venue v ON e.venue_id = v.venue_id " +
            "JOIN Category c ON e.cat_id = c.cat_id " +
            "JOIN User u ON e.user_id = u.user_id " +
            "LEFT JOIN Attends a ON e.event_id = a.event_id AND a.rsvp_status = 'GOING' " +
            "WHERE e.status = 'APPROVED'"
        );

        // Dynamic Query Building (Security: Using PreparedStatement, not concatenation)
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND e.cat_id = ?");
        }
        if (venueId != null && venueId > 0) {
            sql.append(" AND e.venue_id = ?");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (e.title LIKE ? OR e.description LIKE ?)");
        }
        
        sql.append(" GROUP BY e.event_id, v.venue_name, c.cat_name, u.user_name");
        sql.append(" ORDER BY e.start DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (categoryId != null && categoryId > 0) {
                stmt.setInt(paramIndex++, categoryId);
            }
            if (venueId != null && venueId > 0) {
                stmt.setInt(paramIndex++, venueId);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Event e = new Event();
                e.setEventId(rs.getInt("event_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setStart(rs.getString("start"));
                e.setEnd(rs.getString("end"));
                e.setMaxAttendees(rs.getInt("max_attendees"));
                e.setStatus(rs.getString("status"));
                e.setVenueName(rs.getString("venue_name"));
                e.setCategoryName(rs.getString("cat_name"));
                e.setOrganizerName(rs.getString("organizer_name"));
                e.setAttendeeCount(rs.getInt("attendee_count"));
                events.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    // 2. Get Event Details with All Related Information
    public Event getEventDetails(int eventId) {
        String sql = "SELECT e.*, v.venue_name, v.location, v.capacity, " +
                     "c.cat_name, u.user_name as organizer_name, u.email as organizer_email, " +
                     "COUNT(a.user_id) as attendee_count " +
                     "FROM Event e " +
                     "JOIN Venue v ON e.venue_id = v.venue_id " +
                     "JOIN Category c ON e.cat_id = c.cat_id " +
                     "JOIN User u ON e.user_id = u.user_id " +
                     "LEFT JOIN Attends a ON e.event_id = a.event_id AND a.rsvp_status = 'GOING' " +
                     "WHERE e.event_id = ? " +
                     "GROUP BY e.event_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Event e = new Event();
                e.setEventId(rs.getInt("event_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setStart(rs.getString("start"));
                e.setEnd(rs.getString("end"));
                e.setMaxAttendees(rs.getInt("max_attendees"));
                e.setStatus(rs.getString("status"));
                e.setVenueName(rs.getString("venue_name"));
                e.setCategoryName(rs.getString("cat_name"));
                e.setOrganizerName(rs.getString("organizer_name"));
                e.setAttendeeCount(rs.getInt("attendee_count"));
                return e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Get User's Created Events
    public List<Event> getEventsByOrganizer(int userId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT e.*, v.venue_name, c.cat_name, " +
                     "COUNT(a.user_id) as attendee_count " +
                     "FROM Event e " +
                     "JOIN Venue v ON e.venue_id = v.venue_id " +
                     "JOIN Category c ON e.cat_id = c.cat_id " +
                     "LEFT JOIN Attends a ON e.event_id = a.event_id AND a.rsvp_status = 'GOING' " +
                     "WHERE e.user_id = ? " +
                     "GROUP BY e.event_id " +
                     "ORDER BY e.start DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Event e = new Event();
                e.setEventId(rs.getInt("event_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setStart(rs.getString("start"));
                e.setEnd(rs.getString("end"));
                e.setStatus(rs.getString("status"));
                e.setVenueName(rs.getString("venue_name"));
                e.setCategoryName(rs.getString("cat_name"));
                e.setAttendeeCount(rs.getInt("attendee_count"));
                events.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    // 4. Create Event (Returns generated event_id)
    public int createEvent(Event event) {
        String sql = "INSERT INTO Event (user_id, venue_id, cat_id, title, description, " +
                     "start, end, max_attendees, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, event.getUserId());
            stmt.setInt(2, event.getVenueId());
            stmt.setInt(3, event.getCatId());
            stmt.setString(4, event.getTitle());
            stmt.setString(5, event.getDescription());
            stmt.setString(6, event.getStart());
            stmt.setString(7, event.getEnd());
            stmt.setInt(8, event.getMaxAttendees());
            stmt.setString(9, "PENDING"); // New events require admin approval
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
