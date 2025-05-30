package emsi.laTaxista;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Trip {
    private String id;
    private User driver;
    private String departure;
    private String destination;
    private LocalDateTime dateTime;
    private int availableSeats;
    private double pricePerSeat;
    private String description;
    private List<User> passengers;
    private TripStatus status;
    
    public enum TripStatus {
        ACTIVE, FULL, COMPLETED, CANCELLED
    }
    
    public Trip(String id, User driver, String departure, String destination, 
                LocalDateTime dateTime, int availableSeats, double pricePerSeat, String description) {
        this.id = id;
        this.driver = driver;
        this.departure = departure;
        this.destination = destination;
        this.dateTime = dateTime;
        this.availableSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
        this.description = description;
        this.passengers = new ArrayList<>();
        this.status = TripStatus.ACTIVE;
    }
    
    public boolean addPassenger(User passenger) {
        if (availableSeats > 0 && !passengers.contains(passenger) && !passenger.equals(driver)) {
            passengers.add(passenger);
            availableSeats--;
            if (availableSeats == 0) {
                status = TripStatus.FULL;
            }
            return true;
        }
        return false;
    }
    
    // Used by TripManager when loading from database
    public void addPassengerWithoutSeatUpdate(User passenger) {
        if (!passengers.contains(passenger)) {
            passengers.add(passenger);
        }
    }
    
    public boolean removePassenger(User passenger) {
        if (passengers.remove(passenger)) {
            availableSeats++;
            if (status == TripStatus.FULL) {
                status = TripStatus.ACTIVE;
            }
            return true;
        }
        return false;
    }
    
    public String getFormattedDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    // Getters and setters
    public String getId() { return id; }
    public User getDriver() { return driver; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public LocalDateTime getDateTime() { return dateTime; }
    public int getAvailableSeats() { return availableSeats; }
    public double getPricePerSeat() { return pricePerSeat; }
    public String getDescription() { return description; }
    public List<User> getPassengers() { return passengers; }
    public TripStatus getStatus() { return status; }
    
    public void setStatus(TripStatus status) { this.status = status; }
    
    @Override
    public String toString() {
        return departure + " → " + destination + " (" + getFormattedDateTime() + ")";
    }
}
