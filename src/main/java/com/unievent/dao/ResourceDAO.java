package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.Category;
import com.unievent.model.Venue;
import com.unievent.model.Department;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility DAO for Managing Categories, Venues, and Departments
 * These are typically managed by admins
 */
public class ResourceDAO {

    // ==================== CATEGORY OPERATIONS ====================
    
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Category ORDER BY cat_name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Category c = new Category();
                c.setCatId(rs.getInt("cat_id"));
                c.setCatName(rs.getString("cat_name"));
                categories.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public boolean addCategory(String catName) {
        String sql = "INSERT INTO Category (cat_name) VALUES (?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, catName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== VENUE OPERATIONS ====================
    
    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM Venue ORDER BY venue_name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Venue v = new Venue();
                v.setVenueId(rs.getInt("venue_id"));
                v.setVenueName(rs.getString("venue_name"));
                v.setLocation(rs.getString("location"));
                v.setCapacity(rs.getInt("capacity"));
                venues.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }

    public boolean addVenue(Venue venue) {
        String sql = "INSERT INTO Venue (venue_name, location, capacity) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, venue.getVenueName());
            stmt.setString(2, venue.getLocation());
            stmt.setInt(3, venue.getCapacity());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check Venue Availability for a Time Slot
    public boolean isVenueAvailable(int venueId, String startTime, String endTime) {
        String sql = "SELECT COUNT(*) FROM Event " +
                     "WHERE venue_id = ? " +
                     "AND status IN ('PENDING', 'APPROVED') " +
                     "AND ((start <= ? AND end >= ?) OR (start <= ? AND end >= ?))";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, venueId);
            stmt.setString(2, startTime);
            stmt.setString(3, startTime);
            stmt.setString(4, endTime);
            stmt.setString(5, endTime);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Available if count is 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== DEPARTMENT OPERATIONS ====================
    
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Department ORDER BY dept_name";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Department d = new Department();
                d.setDeptId(rs.getInt("dept_id"));
                d.setDeptName(rs.getString("dept_name"));
                departments.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public boolean addDepartment(String deptName) {
        String sql = "INSERT INTO Department (dept_name) VALUES (?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, deptName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== STATISTICS ====================
    
    // Get Event Count by Category
    public List<CategoryStats> getEventsByCategory() {
        List<CategoryStats> stats = new ArrayList<>();
        String sql = "SELECT c.cat_name, COUNT(e.event_id) as event_count " +
                     "FROM Category c " +
                     "LEFT JOIN Event e ON c.cat_id = e.cat_id " +
                     "GROUP BY c.cat_id, c.cat_name " +
                     "ORDER BY event_count DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                CategoryStats stat = new CategoryStats();
                stat.categoryName = rs.getString("cat_name");
                stat.eventCount = rs.getInt("event_count");
                stats.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Inner class for category statistics
    public static class CategoryStats {
        public String categoryName;
        public int eventCount;
    }
}
