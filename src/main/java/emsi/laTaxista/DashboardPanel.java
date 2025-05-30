package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private CovoiturageApp app;
    private JLabel welcomeLabel;
    private JList<Trip> myTripsAsList;
    private DefaultListModel<Trip> myTripsModel;
    private JList<Trip> myBookingsAsList;
    private DefaultListModel<Trip> myBookingsModel;
    
    public DashboardPanel(CovoiturageApp app) {
        this.app = app;
        initializeComponents();
        layoutComponents();
        addEventListeners();
    }
    
    private void initializeComponents() {
        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        myTripsModel = new DefaultListModel<>();
        myTripsAsList = new JList<>(myTripsModel);
        myTripsAsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        myBookingsModel = new DefaultListModel<>();
        myBookingsAsList = new JList<>(myBookingsModel);
        myBookingsAsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Top panel with welcome and navigation
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        
        JButton createTripBtn = new JButton("Create Trip");
        JButton searchTripsBtn = new JButton("Search Trips");
        JButton logoutBtn = new JButton("Logout");
        
        styleButton(createTripBtn, new Color(60, 179, 113));
        styleButton(searchTripsBtn, new Color(255, 165, 0));
        styleButton(logoutBtn, new Color(220, 20, 60));
        
        buttonPanel.add(createTripBtn);
        buttonPanel.add(searchTripsBtn);
        buttonPanel.add(logoutBtn);
        
        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 10, 10));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // My trips section
        JPanel myTripsPanel = createListPanel("My Trips", myTripsAsList);
        mainContent.add(myTripsPanel);
        
        // My bookings section
        JPanel myBookingsPanel = createListPanel("My Bookings", myBookingsAsList);
        mainContent.add(myBookingsPanel);
        
        add(mainContent, BorderLayout.CENTER);
        
        // Event listeners
        createTripBtn.addActionListener(e -> app.showPanel("CREATE_TRIP"));
        searchTripsBtn.addActionListener(e -> app.showPanel("SEARCH_TRIPS"));
        logoutBtn.addActionListener(e -> app.logout());
    }
    
    private JPanel createListPanel(String title, JList<Trip> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }
    
    private void addEventListeners() {
        // Double-click to view trip details
        myTripsAsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Trip selectedTrip = myTripsAsList.getSelectedValue();
                    if (selectedTrip != null) {
                        showTripDetails(selectedTrip);
                    }
                }
            }
        });
        
        myBookingsAsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Trip selectedTrip = myBookingsAsList.getSelectedValue();
                    if (selectedTrip != null) {
                        showTripDetails(selectedTrip);
                    }
                }
            }
        });
    }
    
    private void showTripDetails(Trip trip) {
        StringBuilder details = new StringBuilder();
        details.append("Trip Details:\n\n");
        details.append("From: ").append(trip.getDeparture()).append("\n");
        details.append("To: ").append(trip.getDestination()).append("\n");
        details.append("Date: ").append(trip.getFormattedDateTime()).append("\n");
        details.append("Driver: ").append(trip.getDriver().getUsername()).append("\n");
        details.append("Available Seats: ").append(trip.getAvailableSeats()).append("\n");
        details.append("Price per Seat: €").append(trip.getPricePerSeat()).append("\n");
        details.append("Status: ").append(trip.getStatus()).append("\n");
        details.append("Description: ").append(trip.getDescription()).append("\n\n");
        
        if (!trip.getPassengers().isEmpty()) {
            details.append("Passengers:\n");
            for (User passenger : trip.getPassengers()) {
                details.append("- ").append(passenger.getUsername()).append("\n");
            }
        }
        
        JOptionPane.showMessageDialog(this, details.toString(), "Trip Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void updateUserInfo(User user) {
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        refreshMyTrips();
    }
    
    public void refreshMyTrips() {
        if (app.getCurrentUser() != null) {
            // Update my trips
            myTripsModel.clear();
            List<Trip> myTrips = app.getTripManager().getTripsByDriver(app.getCurrentUser());
            for (Trip trip : myTrips) {
                myTripsModel.addElement(trip);
            }
            
            // Update my bookings
            myBookingsModel.clear();
            List<Trip> myBookings = app.getTripManager().getTripsByPassenger(app.getCurrentUser());
            for (Trip trip : myBookings) {
                myBookingsModel.addElement(trip);
            }
        }
    }
}