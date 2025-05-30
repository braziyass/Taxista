package emsi.laTaxista;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Connection connection;
    
    public UserManager() {
        this.connection = DatabaseManager.getConnection();
    }
    
    public boolean registerUser(String username, String email, String password, String phone) {
        try {
            // Check if username already exists
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE username = ?"
            );
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();
            
            if (count > 0) {
                return false; // Username already exists
            }
            
            // Insert new user
            PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO users (username, email, password, phone) VALUES (?, ?, ?, ?)"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, password);
            insertStmt.setString(4, phone);
            insertStmt.executeUpdate();
            insertStmt.close();
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public User authenticate(String username, String password) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            User user = null;
            if (rs.next()) {
                user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone")
                );
                user.setRating(rs.getDouble("rating"));
                user.setTotalTrips(rs.getInt("total_trips"));
            }
            
            rs.close();
            stmt.close();
            return user;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public User getUserByUsername(String username) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ?"
            );
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            User user = null;
            if (rs.next()) {
                user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone")
                );
                user.setRating(rs.getDouble("rating"));
                user.setTotalTrips(rs.getInt("total_trips"));
            }
            
            rs.close();
            stmt.close();
            return user;
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean updateUserRating(String username, double rating) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE users SET rating = ? WHERE username = ?"
            );
            stmt.setDouble(1, rating);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user rating: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean incrementUserTrips(String username) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE users SET total_trips = total_trips + 1 WHERE username = ?"
            );
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing user trips: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
