package emsi.laTaxista;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:covoiturage.db";
    private static Connection connection;
    
    // Initialize database
    public static void initialize() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Create tables if they don't exist
            createTables();
            
            // Migrate database if needed
            migrateDatabase();
            
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Create necessary tables
    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Users table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "username TEXT PRIMARY KEY, " +
            "email TEXT UNIQUE, " +
            "password TEXT, " +
            "phone TEXT, " +
            "rating REAL DEFAULT 5.0, " +
            "total_trips INTEGER DEFAULT 0" +
            ")"
        );
        
        // Trips table
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS trips (" +
            "id TEXT PRIMARY KEY, " +
            "driver_username TEXT, " +
            "departure TEXT, " +
            "destination TEXT, " +
            "date_time TEXT, " +
            "available_seats INTEGER, " +
            "price_per_seat REAL, " +
            "description TEXT, " +
            "status TEXT, " +
            "FOREIGN KEY (driver_username) REFERENCES users(username)" +
            ")"
        );
        
        // Trip passengers (many-to-many relationship)
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS trip_passengers (" +
            "trip_id TEXT, " +
            "passenger_username TEXT, " +
            "PRIMARY KEY (trip_id, passenger_username), " +
            "FOREIGN KEY (trip_id) REFERENCES trips(id), " +
            "FOREIGN KEY (passenger_username) REFERENCES users(username)" +
            ")"
        );
        
        stmt.close();
        
        // Add demo data if tables are empty
        addDemoDataIfNeeded();
    }
    
    // Migrate database schema if needed
    private static void migrateDatabase() {
        try {
            // Check if role column exists in users table
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "users", "role");
            boolean roleColumnExists = columns.next();
            columns.close();
            
            if (!roleColumnExists) {
                System.out.println("Migrating database: Adding 'role' column to users table");
                Statement stmt = connection.createStatement();
                
                // Add role column with default value 'USER'
                stmt.execute("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'USER'");
                
                // Set admin user role
                stmt.execute("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'");
                
                stmt.close();
                System.out.println("Database migration completed successfully");
            }
        } catch (SQLException e) {
            System.err.println("Database migration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Add demo data if tables are empty
    private static void addDemoDataIfNeeded() {
        try {
            // Check if users table is empty
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            int userCount = rs.getInt(1);
            rs.close();
            
            if (userCount == 0) {
                // Add demo users
                PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO users (username, email, password, phone, role) VALUES (?, ?, ?, ?, ?)"
                );
                
                // Admin user
                pstmt.setString(1, "admin");
                pstmt.setString(2, "admin@covoiturage.com");
                pstmt.setString(3, "admin");
                pstmt.setString(4, "123456789");
                pstmt.setString(5, "ADMIN");
                pstmt.executeUpdate();
                
                // yassine user
                pstmt.setString(1, "yassine");
                pstmt.setString(2, "yassine@email.com");
                pstmt.setString(3, "password");
                pstmt.setString(4, "987654321");
                pstmt.setString(5, "USER");
                pstmt.executeUpdate();
                
                // simo user
                pstmt.setString(1, "sino");
                pstmt.setString(2, "simo@email.com");
                pstmt.setString(3, "password");
                pstmt.setString(4, "555666777");
                pstmt.setString(5, "USER");
                pstmt.executeUpdate();
                
                pstmt.close();
                
                // Add demo trips
                addDemoTrips();
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error adding demo data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Add demo trips
    private static void addDemoTrips() throws SQLException {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO trips (id, driver_username, departure, destination, date_time, " +
            "available_seats, price_per_seat, description, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );
        
        // Trip 1
        pstmt.setString(1, "1");
        pstmt.setString(2, "admin");
        pstmt.setString(3, "Paris");
        pstmt.setString(4, "Lyon");
        pstmt.setString(5, tomorrow.format(formatter));
        pstmt.setInt(6, 3);
        pstmt.setDouble(7, 25.0);
        pstmt.setString(8, "Comfortable car, non-smoking");
        pstmt.setString(9, "ACTIVE");
        pstmt.executeUpdate();
        
        // Trip 2
        pstmt.setString(1, "2");
        pstmt.setString(2, "admin");
        pstmt.setString(3, "Marseille");
        pstmt.setString(4, "Nice");
        pstmt.setString(5, dayAfterTomorrow.format(formatter));
        pstmt.setInt(6, 2);
        pstmt.setDouble(7, 15.0);
        pstmt.setString(8, "Scenic route along the coast");
        pstmt.setString(9, "ACTIVE");
        pstmt.executeUpdate();
        
        pstmt.close();
    }
    
    // Close database connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    // Get database connection
    public static Connection getConnection() {
        return connection;
    }
}
