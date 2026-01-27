package com.unievent.dao;

import com.unievent.config.DBConnection;
import com.unievent.model.User;
import java.sql.*;

/**
 * Advanced User DAO with Transaction Handling for Complex Registration
 * Handles the inheritance pattern: User -> Student/Staff
 */
public class UserAdvancedDAO {

    // 1. THREAD-SAFE: Transactional Registration for Students
    public boolean registerStudent(User user, String studentRegNo, int batchYear, int deptId) {
        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtStudent = null;

        try {
            conn = DBConnection.getConnection();
            // 1. DISABLE AUTO-COMMIT (Start Transaction)
            conn.setAutoCommit(false);

            // 2. Insert into Generic USER table
            String sqlUser = "INSERT INTO User (email, password, user_name, phone) VALUES (?, ?, ?, ?)";
            stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getEmail());
            stmtUser.setString(2, user.getPassword()); // TODO: Hash password with BCrypt in production!
            stmtUser.setString(3, user.getFullName());
            stmtUser.setString(4, user.getPhone());
            stmtUser.executeUpdate();

            // 3. Get the Generated User ID
            ResultSet rs = stmtUser.getGeneratedKeys();
            int newUserId = 0;
            if (rs.next()) {
                newUserId = rs.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }

            // 4. Insert into Specific STUDENT table
            String sqlStudent = "INSERT INTO Student (user_id, std_id, batch_year, dept_id) VALUES (?, ?, ?, ?)";
            stmtStudent = conn.prepareStatement(sqlStudent);
            stmtStudent.setInt(1, newUserId);
            stmtStudent.setString(2, studentRegNo);
            stmtStudent.setInt(3, batchYear);
            stmtStudent.setInt(4, deptId);
            stmtStudent.executeUpdate();

            // 5. COMMIT TRANSACTION (Success!)
            conn.commit();
            System.out.println("✅ Student registered successfully with User ID: " + newUserId);
            return true;

        } catch (SQLException e) {
            // ROLLBACK if anything failed (Data Integrity Protection)
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("⚠️ Transaction rolled back due to error.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            // Clean up resources
            try {
                if (stmtUser != null) stmtUser.close();
                if (stmtStudent != null) stmtStudent.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 2. THREAD-SAFE: Transactional Registration for Staff
    public boolean registerStaff(User user, String staffId, int deptId, String position) {
        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtStaff = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert into User table
            String sqlUser = "INSERT INTO User (email, password, user_name, phone) VALUES (?, ?, ?, ?)";
            stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getEmail());
            stmtUser.setString(2, user.getPassword());
            stmtUser.setString(3, user.getFullName());
            stmtUser.setString(4, user.getPhone());
            stmtUser.executeUpdate();

            ResultSet rs = stmtUser.getGeneratedKeys();
            int newUserId = 0;
            if (rs.next()) {
                newUserId = rs.getInt(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }

            // Insert into Staff table
            String sqlStaff = "INSERT INTO Staff (user_id, staff_id, dept_id, position) VALUES (?, ?, ?, ?)";
            stmtStaff = conn.prepareStatement(sqlStaff);
            stmtStaff.setInt(1, newUserId);
            stmtStaff.setString(2, staffId);
            stmtStaff.setInt(3, deptId);
            stmtStaff.setString(4, position);
            stmtStaff.executeUpdate();

            conn.commit();
            System.out.println("✅ Staff registered successfully with User ID: " + newUserId);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("⚠️ Transaction rolled back due to error.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmtUser != null) stmtUser.close();
                if (stmtStaff != null) stmtStaff.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 3. Check if Email Already Exists (Prevent Duplicates)
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM User WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. Get User by Email (For Login)
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM User WHERE email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("user_name"));
                user.setPhone(rs.getString("phone"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
