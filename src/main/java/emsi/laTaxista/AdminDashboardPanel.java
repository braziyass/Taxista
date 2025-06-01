package emsi.laTaxista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private CovoiturageApp app;
    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;
    
    // Users management
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton addUserButton, editUserButton, deleteUserButton, refreshUsersButton;
    
    // Trips management
    private JTable tripsTable;
    private DefaultTableModel tripsTableModel;
    private JButton addTripButton, editTripButton, deleteTripButton, refreshTripsButton;
    
    public AdminDashboardPanel(CovoiturageApp app) {
        this.app = app;
        initializeComponents();
        layoutComponents();
        addEventListeners();
    }
    
    private void initializeComponents() {
        welcomeLabel = new JLabel("Admin Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        tabbedPane = new JTabbedPane();
        
        // Users table
        String[] userColumns = {"Username", "Email", "Phone", "Rating", "Total Trips", "Role"};
        usersTableModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Trips table
        String[] tripColumns = {"ID", "Driver", "From", "To", "Date", "Seats", "Price", "Status"};
        tripsTableModel = new DefaultTableModel(tripColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tripsTable = new JTable(tripsTableModel);
        tripsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Buttons
        addUserButton = new JButton("Add User");
        editUserButton = new JButton("Edit User");
        deleteUserButton = new JButton("Delete User");
        refreshUsersButton = new JButton("Refresh");
        
        addTripButton = new JButton("Add Trip");
        editTripButton = new JButton("Edit Trip");
        deleteTripButton = new JButton("Delete Trip");
        refreshTripsButton = new JButton("Refresh");
        
        styleButton(addUserButton, Color.white);
        styleButton(editUserButton, Color.white);
        styleButton(deleteUserButton, Color.white);
        styleButton(refreshUsersButton, Color.white);
        
        styleButton(addTripButton, Color.white);
        styleButton(editTripButton, Color.white);
        styleButton(deleteTripButton, Color.white);
        styleButton(refreshTripsButton, Color.white);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Top panel with welcome and logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, new Color(220, 20, 60));
        logoutButton.addActionListener(e -> app.logout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Users management panel
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users Management", usersPanel);
        
        // Trips management panel
        JPanel tripsPanel = createTripsPanel();
        tabbedPane.addTab("Trips Management", tripsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(addUserButton);
        buttonsPanel.add(editUserButton);
        buttonsPanel.add(deleteUserButton);
        buttonsPanel.add(refreshUsersButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTripsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table
        JScrollPane scrollPane = new JScrollPane(tripsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(addTripButton);
        buttonsPanel.add(editTripButton);
        buttonsPanel.add(deleteTripButton);
        buttonsPanel.add(refreshTripsButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }
    
    private void addEventListeners() {
        // Users management
        addUserButton.addActionListener(this::addUser);
        editUserButton.addActionListener(this::editUser);
        deleteUserButton.addActionListener(this::deleteUser);
        refreshUsersButton.addActionListener(e -> refreshUsers());
        
        // Trips management
        addTripButton.addActionListener(this::addTrip);
        editTripButton.addActionListener(this::editTrip);
        deleteTripButton.addActionListener(this::deleteTrip);
        refreshTripsButton.addActionListener(e -> refreshTrips());
        
        // Double-click to edit
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editUser(null);
                }
            }
        });
        
        tripsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editTrip(null);
                }
            }
        });
    }
    
    // User management methods
    private void addUser(ActionEvent e) {
        UserEditDialog dialog = new UserEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            User newUser = dialog.getUser();
            if (app.getUserManager().registerUser(newUser.getUsername(), newUser.getEmail(), 
                                                newUser.getPassword(), newUser.getPhone())) {
                // Update role if not default
                if (newUser.getRole() != User.UserRole.USER) {
                    app.getUserManager().updateUser(newUser.getUsername(), newUser);
                }
                refreshUsers();
                JOptionPane.showMessageDialog(this, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) usersTableModel.getValueAt(selectedRow, 0);
        User user = app.getUserManager().getUserByUsername(username);
        
        if (user != null) {
            UserEditDialog dialog = new UserEditDialog(this, user);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                User updatedUser = dialog.getUser();
                if (app.getUserManager().updateUser(username, updatedUser)) {
                    refreshUsers();
                    JOptionPane.showMessageDialog(this, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update user!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteUser(ActionEvent e) {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String username = (String) usersTableModel.getValueAt(selectedRow, 0);
        
        if (username.equals(app.getCurrentUser().getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete user '" + username + "'?\nThis will also delete all their trips and bookings.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (app.getUserManager().deleteUser(username)) {
                refreshUsers();
                refreshTrips();
                JOptionPane.showMessageDialog(this, "User deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Trip management methods
    private void addTrip(ActionEvent e) {
        TripEditDialog dialog = new TripEditDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Trip newTrip = dialog.getTrip();
            String tripId = app.getTripManager().createTrip(
                newTrip.getDriver(), newTrip.getDeparture(), newTrip.getDestination(),
                newTrip.getDateTime(), newTrip.getAvailableSeats(), newTrip.getPricePerSeat(),
                newTrip.getDescription()
            );
            if (tripId != null) {
                refreshTrips();
                JOptionPane.showMessageDialog(this, "Trip added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add trip!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editTrip(ActionEvent e) {
        int selectedRow = tripsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trip to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tripId = (String) tripsTableModel.getValueAt(selectedRow, 0);
        Trip trip = app.getTripManager().findTripById(tripId);
        
        if (trip != null) {
            TripEditDialog dialog = new TripEditDialog(this, trip);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                Trip updatedTrip = dialog.getTrip();
                if (app.getTripManager().updateTrip(updatedTrip)) {
                    refreshTrips();
                    JOptionPane.showMessageDialog(this, "Trip updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update trip!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void deleteTrip(ActionEvent e) {
        int selectedRow = tripsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a trip to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tripId = (String) tripsTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete trip ID '" + tripId + "'?\nThis will also cancel all bookings for this trip.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (app.getTripManager().deleteTrip(tripId)) {
                refreshTrips();
                JOptionPane.showMessageDialog(this, "Trip deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete trip!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void refreshUsers() {
        usersTableModel.setRowCount(0);
        List<User> users = app.getUserManager().getAllUsers();
        for (User user : users) {
            Object[] row = {
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                String.format("%.1f", user.getRating()),
                user.getTotalTrips(),
                user.getRole().toString()
            };
            usersTableModel.addRow(row);
        }
    }
    
    public void refreshTrips() {
        tripsTableModel.setRowCount(0);
        List<Trip> trips = app.getTripManager().getAllTrips();
        for (Trip trip : trips) {
            Object[] row = {
                trip.getId(),
                trip.getDriver().getUsername(),
                trip.getDeparture(),
                trip.getDestination(),
                trip.getFormattedDateTime(),
                trip.getAvailableSeats(),
                "€" + trip.getPricePerSeat(),
                trip.getStatus().toString()
            };
            tripsTableModel.addRow(row);
        }
    }
    
    public void updateUserInfo(User user) {
        welcomeLabel.setText("Admin Dashboard - Welcome, " + user.getUsername() + "!");
        refreshUsers();
        refreshTrips();
    }
    
    public CovoiturageApp getApp() {
        return app;
    }
}

