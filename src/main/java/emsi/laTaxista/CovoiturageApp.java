package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CovoiturageApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private UserManager userManager;
    private TripManager tripManager;
    private User currentUser;
    
    // Panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private CreateTripPanel createTripPanel;
    private SearchTripsPanel searchTripsPanel;
    
    public CovoiturageApp() {
        userManager = new UserManager();
        tripManager = new TripManager(userManager);
        
        initializeUI();
        setupFrame();
    }
    
    private void initializeUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize panels
        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        createTripPanel = new CreateTripPanel(this);
        searchTripsPanel = new SearchTripsPanel(this);
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(dashboardPanel, "DASHBOARD");
        mainPanel.add(createTripPanel, "CREATE_TRIP");
        mainPanel.add(searchTripsPanel, "SEARCH_TRIPS");
        
        add(mainPanel);
    }
    
    private void setupFrame() {
        setTitle("Covoiturage - Carpooling App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public void login(String username, String password) {
        User user = userManager.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            dashboardPanel.updateUserInfo(user);
            showPanel("DASHBOARD");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void register(String username, String email, String password, String phone) {
        if (userManager.registerUser(username, email, password, phone)) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists!", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void logout() {
        currentUser = null;
        showPanel("LOGIN");
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public TripManager getTripManager() {
        return tripManager;
    }
    
    public void refreshTrips() {
        searchTripsPanel.refreshTrips();
        dashboardPanel.refreshMyTrips();
    }
}
