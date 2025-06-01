package emsi.laTaxista;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
                "INSERT INTO users (username, email, password, phone, role) VALUES (?, ?, ?, ?, ?)"
            );
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, password);
            insertStmt.setString(4, phone);
            insertStmt.setString(5, User.UserRole.USER.toString());
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
                
                // Handle role column which might not exist in older databases
                try {
                    String roleStr = rs.getString("role");
                    if (roleStr != null && !roleStr.isEmpty()) {
                        user.setRole(User.UserRole.valueOf(roleStr));
                    } else {
                        // Default role if column exists but value is null
                        user.setRole(User.UserRole.USER);
                    }
                } catch (SQLException e) {
                    // Role column doesn't exist, set default role
                    user.setRole(User.UserRole.USER);
                    
                    // Special case for admin user
                    if (username.equals("admin")) {
                        user.setRole(User.UserRole.ADMIN);
                    }
                }
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
                
                // Handle role column which might not exist in older databases
                try {
                    String roleStr = rs.getString("role");
                    if (roleStr != null && !roleStr.isEmpty()) {
                        user.setRole(User.UserRole.valueOf(roleStr));
                    } else {
                        // Default role if column exists but value is null
                        user.setRole(User.UserRole.USER);
                    }
                } catch (SQLException e) {
                    // Role column doesn't exist, set default role
                    user.setRole(User.UserRole.USER);
                    
                    // Special case for admin user
                    if (username.equals("admin")) {
                        user.setRole(User.UserRole.ADMIN);
                    }
                }
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

    // Get all users (admin only)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone")
                );
                user.setRating(rs.getDouble("rating"));
                user.setTotalTrips(rs.getInt("total_trips"));
                
                // Handle role column which might not exist in older databases
                try {
                    String roleStr = rs.getString("role");
                    if (roleStr != null && !roleStr.isEmpty()) {
                        user.setRole(User.UserRole.valueOf(roleStr));
                    } else {
                        // Default role if column exists but value is null
                        user.setRole(User.UserRole.USER);
                    }
                } catch (SQLException e) {
                    // Role column doesn't exist, set default role
                    user.setRole(User.UserRole.USER);
                    
                    // Special case for admin user
                    if (user.getUsername().equals("admin")) {
                        user.setRole(User.UserRole.ADMIN);
                    }
                }
                
                users.add(user);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // Update user (admin only)
    public boolean updateUser(String originalUsername, User updatedUser) {
        try {
            // Check if role column exists
            boolean roleColumnExists = true;
            try {
                Statement checkStmt = connection.createStatement();
                checkStmt.executeQuery("SELECT role FROM users LIMIT 1");
                checkStmt.close();
            } catch (SQLException e) {
                roleColumnExists = false;
            }
            
            String sql;
            if (roleColumnExists) {
                sql = "UPDATE users SET username = ?, email = ?, password = ?, phone = ?, " +
                      "rating = ?, total_trips = ?, role = ? WHERE username = ?";
            } else {
                sql = "UPDATE users SET username = ?, email = ?, password = ?, phone = ?, " +
                      "rating = ?, total_trips = ? WHERE username = ?";
            }
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, updatedUser.getUsername());
            stmt.setString(2, updatedUser.getEmail());
            stmt.setString(3, updatedUser.getPassword());
            stmt.setString(4, updatedUser.getPhone());
            stmt.setDouble(5, updatedUser.getRating());
            stmt.setInt(6, updatedUser.getTotalTrips());
            
            if (roleColumnExists) {
                stmt.setString(7, updatedUser.getRole().toString());
                stmt.setString(8, originalUsername);
            } else {
                stmt.setString(7, originalUsername);
            }
            
            int rowsAffected = stmt.executeUpdate();
            stmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete user (admin only)
    public boolean deleteUser(String username) {
        try {
            connection.setAutoCommit(false);
            
            // First delete from trip_passengers
            PreparedStatement deletePassengersStmt = connection.prepareStatement(
                "DELETE FROM trip_passengers WHERE passenger_username = ?"
            );
            deletePassengersStmt.setString(1, username);
            deletePassengersStmt.executeUpdate();
            deletePassengersStmt.close();
            
            // Delete trips where user is driver
            PreparedStatement deleteTripsStmt = connection.prepareStatement(
                "DELETE FROM trips WHERE driver_username = ?"
            );
            deleteTripsStmt.setString(1, username);
            deleteTripsStmt.executeUpdate();
            deleteTripsStmt.close();
            
            // Finally delete the user
            PreparedStatement deleteUserStmt = connection.prepareStatement(
                "DELETE FROM users WHERE username = ?"
            );
            deleteUserStmt.setString(1, username);
            int rowsAffected = deleteUserStmt.executeUpdate();
            deleteUserStmt.close();
            
            connection.commit();
            connection.setAutoCommit(true);
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
