package emsi.laTaxista;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TripManager {
    private Connection connection;
    private UserManager userManager;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public TripManager(UserManager userManager) {
        this.connection = DatabaseManager.getConnection();
        this.userManager = userManager;
    }
    
    public String createTrip(User driver, String departure, String destination, 
                           LocalDateTime dateTime, int seats, double price, String description) {
        try {
            // Get next trip ID
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(CAST(id AS INTEGER)) FROM trips");
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            rs.close();
            stmt.close();
            
            String tripId = String.valueOf(nextId);
            
            // Insert new trip
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO trips (id, driver_username, departure, destination, date_time, " +
                "available_seats, price_per_seat, description, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            pstmt.setString(1, tripId);
            pstmt.setString(2, driver.getUsername());
            pstmt.setString(3, departure);
            pstmt.setString(4, destination);
            pstmt.setString(5, dateTime.format(formatter));
            pstmt.setInt(6, seats);
            pstmt.setDouble(7, price);
            pstmt.setString(8, description);
            pstmt.setString(9, Trip.TripStatus.ACTIVE.toString());
            pstmt.executeUpdate();
            pstmt.close();
            
            return tripId;
        } catch (SQLException e) {
            System.err.println("Error creating trip: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Trip> getAllTrips() {
        List<Trip> trips = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM trips");
            
            while (rs.next()) {
                Trip trip = createTripFromResultSet(rs);
                if (trip != null) {
                    trips.add(trip);
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting all trips: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trips;
    }
    
    public List<Trip> getAvailableTrips() {
        List<Trip> trips = new ArrayList<>();
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM trips WHERE status = ? AND available_seats > 0"
            );
            pstmt.setString(1, Trip.TripStatus.ACTIVE.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Trip trip = createTripFromResultSet(rs);
                if (trip != null) {
                    trips.add(trip);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting available trips: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trips;
    }
    
    public List<Trip> getTripsByDriver(User driver) {
        List<Trip> trips = new ArrayList<>();
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM trips WHERE driver_username = ?"
            );
            pstmt.setString(1, driver.getUsername());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Trip trip = createTripFromResultSet(rs);
                if (trip != null) {
                    trips.add(trip);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting trips by driver: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trips;
    }
    
    public List<Trip> getTripsByPassenger(User passenger) {
        List<Trip> trips = new ArrayList<>();
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT t.* FROM trips t " +
                "JOIN trip_passengers tp ON t.id = tp.trip_id " +
                "WHERE tp.passenger_username = ?"
            );
            pstmt.setString(1, passenger.getUsername());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Trip trip = createTripFromResultSet(rs);
                if (trip != null) {
                    trips.add(trip);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting trips by passenger: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trips;
    }
    
    public boolean bookTrip(String tripId, User passenger) {
        try {
            connection.setAutoCommit(false);
            
            // Check if trip exists and has available seats
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT available_seats FROM trips WHERE id = ? AND status = ?"
            );
            checkStmt.setString(1, tripId);
            checkStmt.setString(2, Trip.TripStatus.ACTIVE.toString());
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next() || rs.getInt("available_seats") <= 0) {
                rs.close();
                checkStmt.close();
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            rs.close();
            checkStmt.close();
            
            // Add passenger to trip
            PreparedStatement addPassengerStmt = connection.prepareStatement(
                "INSERT INTO trip_passengers (trip_id, passenger_username) VALUES (?, ?)"
            );
            addPassengerStmt.setString(1, tripId);
            addPassengerStmt.setString(2, passenger.getUsername());
            addPassengerStmt.executeUpdate();
            addPassengerStmt.close();
            
            // Update available seats
            PreparedStatement updateSeatsStmt = connection.prepareStatement(
                "UPDATE trips SET available_seats = available_seats - 1, " +
                "status = CASE WHEN available_seats - 1 <= 0 THEN ? ELSE status END " +
                "WHERE id = ?"
            );
            updateSeatsStmt.setString(1, Trip.TripStatus.FULL.toString());
            updateSeatsStmt.setString(2, tripId);
            updateSeatsStmt.executeUpdate();
            updateSeatsStmt.close();
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error booking trip: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean cancelBooking(String tripId, User passenger) {
        try {
            connection.setAutoCommit(false);
            
            // Remove passenger from trip
            PreparedStatement removePassengerStmt = connection.prepareStatement(
                "DELETE FROM trip_passengers WHERE trip_id = ? AND passenger_username = ?"
            );
            removePassengerStmt.setString(1, tripId);
            removePassengerStmt.setString(2, passenger.getUsername());
            int rowsAffected = removePassengerStmt.executeUpdate();
            removePassengerStmt.close();
            
            if (rowsAffected == 0) {
                connection.rollback();
                connection.setAutoCommit(true);
                return false;
            }
            
            // Update available seats
            PreparedStatement updateSeatsStmt = connection.prepareStatement(
                "UPDATE trips SET available_seats = available_seats + 1, " +
                "status = ? WHERE id = ? AND status = ?"
            );
            updateSeatsStmt.setString(1, Trip.TripStatus.ACTIVE.toString());
            updateSeatsStmt.setString(2, tripId);
            updateSeatsStmt.setString(3, Trip.TripStatus.FULL.toString());
            updateSeatsStmt.executeUpdate();
            updateSeatsStmt.close();
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error cancelling booking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Trip findTripById(String tripId) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM trips WHERE id = ?"
            );
            pstmt.setString(1, tripId);
            ResultSet rs = pstmt.executeQuery();
            
            Trip trip = null;
            if (rs.next()) {
                trip = createTripFromResultSet(rs);
            }
            
            rs.close();
            pstmt.close();
            return trip;
        } catch (SQLException e) {
            System.err.println("Error finding trip by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Trip> searchTrips(String departure, String destination) {
        List<Trip> trips = new ArrayList<>();
        
        try {
            StringBuilder query = new StringBuilder(
                "SELECT * FROM trips WHERE status = ? AND available_seats > 0"
            );
            
            if (departure != null && !departure.isEmpty()) {
                query.append(" AND LOWER(departure) LIKE LOWER(?)");
            }
            
            if (destination != null && !destination.isEmpty()) {
                query.append(" AND LOWER(destination) LIKE LOWER(?)");
            }
            
            PreparedStatement pstmt = connection.prepareStatement(query.toString());
            pstmt.setString(1, Trip.TripStatus.ACTIVE.toString());
            
            int paramIndex = 2;
            if (departure != null && !departure.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + departure + "%");
            }
            
            if (destination != null && !destination.isEmpty()) {
                pstmt.setString(paramIndex, "%" + destination + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Trip trip = createTripFromResultSet(rs);
                if (trip != null) {
                    trips.add(trip);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error searching trips: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trips;
    }
    
    private Trip createTripFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String driverUsername = rs.getString("driver_username");
        User driver = userManager.getUserByUsername(driverUsername);
        
        if (driver == null) {
            return null;
        }
        
        String departure = rs.getString("departure");
        String destination = rs.getString("destination");
        String dateTimeStr = rs.getString("date_time");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        int availableSeats = rs.getInt("available_seats");
        double pricePerSeat = rs.getDouble("price_per_seat");
        String description = rs.getString("description");
        String statusStr = rs.getString("status");
        Trip.TripStatus status = Trip.TripStatus.valueOf(statusStr);
        
        Trip trip = new Trip(id, driver, departure, destination, dateTime, 
                           availableSeats, pricePerSeat, description);
        trip.setStatus(status);
        
        // Load passengers
        loadTripPassengers(trip);
        
        return trip;
    }
    
    private void loadTripPassengers(Trip trip) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "SELECT passenger_username FROM trip_passengers WHERE trip_id = ?"
            );
            pstmt.setString(1, trip.getId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String username = rs.getString("passenger_username");
                User passenger = userManager.getUserByUsername(username);
                if (passenger != null) {
                    trip.addPassengerWithoutSeatUpdate(passenger);
                }
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading trip passengers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
