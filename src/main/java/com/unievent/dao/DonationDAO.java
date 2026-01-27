package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.Donation;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Donation DAO with Financial Calculations
 * Demonstrates SQL Aggregate Functions (SUM, AVG, COUNT)
 */
public class DonationDAO {

    // 1. THREAD-SAFE: Get Total Donations for an Event (Aggregate Function)
    public BigDecimal getTotalDonationsForEvent(int eventId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM Donation WHERE event_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    // 2. Get Donation Statistics for an Event
    public DonationStats getDonationStats(int eventId) {
        String sql = "SELECT " +
                     "COUNT(*) as donor_count, " +
                     "COALESCE(SUM(amount), 0) as total_amount, " +
                     "COALESCE(AVG(amount), 0) as avg_donation, " +
                     "COALESCE(MAX(amount), 0) as highest_donation " +
                     "FROM Donation WHERE event_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DonationStats stats = new DonationStats();
                stats.donorCount = rs.getInt("donor_count");
                stats.totalAmount = rs.getBigDecimal("total_amount");
                stats.avgDonation = rs.getBigDecimal("avg_donation");
                stats.highestDonation = rs.getBigDecimal("highest_donation");
                return stats;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new DonationStats();
    }

    // 3. Get All Donations for an Event with Donor Names
    public List<Donation> getDonationsForEvent(int eventId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT d.*, u.user_name as donor_name " +
                     "FROM Donation d " +
                     "JOIN User u ON d.user_id = u.user_id " +
                     "WHERE d.event_id = ? " +
                     "ORDER BY d.donation_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Donation d = new Donation();
                d.setDonationId(rs.getInt("donation_id"));
                d.setUserId(rs.getInt("user_id"));
                d.setEventId(rs.getInt("event_id"));
                d.setAmount(rs.getBigDecimal("amount"));
                d.setDonationDate(rs.getTimestamp("donation_date"));
                d.setDonorName(rs.getString("donor_name"));
                donations.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    // 4. Record a New Donation
    public boolean addDonation(int userId, int eventId, BigDecimal amount) {
        String sql = "INSERT INTO Donation (user_id, event_id, amount, donation_date) " +
                     "VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            stmt.setBigDecimal(3, amount);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Get User's Donation History
    public List<Donation> getUserDonations(int userId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT d.*, e.title as event_title " +
                     "FROM Donation d " +
                     "JOIN Event e ON d.event_id = e.event_id " +
                     "WHERE d.user_id = ? " +
                     "ORDER BY d.donation_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Donation d = new Donation();
                d.setDonationId(rs.getInt("donation_id"));
                d.setUserId(rs.getInt("user_id"));
                d.setEventId(rs.getInt("event_id"));
                d.setAmount(rs.getBigDecimal("amount"));
                d.setDonationDate(rs.getTimestamp("donation_date"));
                // Store event title in a transient field if needed
                donations.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    // Inner class for statistics
    public static class DonationStats {
        public int donorCount;
        public BigDecimal totalAmount = BigDecimal.ZERO;
        public BigDecimal avgDonation = BigDecimal.ZERO;
        public BigDecimal highestDonation = BigDecimal.ZERO;
    }
}
