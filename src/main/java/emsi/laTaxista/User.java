package emsi.laTaxista;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String password;
    private String phone;
    private double rating;
    private int totalTrips;
    private UserRole role;
    
    public enum UserRole {
        USER, ADMIN
    }
    
    public User(String username, String email, String password, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.rating = 5.0;
        this.totalTrips = 0;
        this.role = UserRole.USER; // Default role
    }
    
    // Check if user is admin
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
    
    // Getters and setters
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }
    
    public double getRating() { 
        return rating; 
    }
    
    public void setRating(double rating) { 
        this.rating = rating; 
    }
    
    public int getTotalTrips() { 
        return totalTrips; 
    }
    
    public void setTotalTrips(int totalTrips) { 
        this.totalTrips = totalTrips; 
    }
    
    public UserRole getRole() { 
        return role; 
    }
    
    public void setRole(UserRole role) { 
        this.role = role; 
    }
    
    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }
    
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
