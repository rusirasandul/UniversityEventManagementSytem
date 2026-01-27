package com.unievent.config;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // XAMPP Default Settings
    private static final String URL = "jdbc:mysql://localhost:3306/unievent_db"; // Make sure DB name matches yours
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 1. Load the MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Establish Connection
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ Database Connected Successfully!");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver Not Found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Connection Failed! Check XAMPP and DB Name.");
            e.printStackTrace();
        }
        return connection;
    }

    // A simple main method to TEST the connection right now
    public static void main(String[] args) {
        getConnection();
    }
}
