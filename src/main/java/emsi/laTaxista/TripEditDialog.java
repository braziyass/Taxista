package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TripEditDialog extends JDialog {
    private AdminDashboardPanel parent;
    private Trip originalTrip;
    private Trip editedTrip;
    private boolean confirmed = false;
    
    private JTextField tripIdField;
    private JComboBox<User> driverComboBox;
    private JTextField departureField;
    private JTextField destinationField;
    private JTextField dateTimeField;
    private JSpinner seatsSpinner;
    private JTextField priceField;
    private JTextArea descriptionArea;
    private JComboBox<Trip.TripStatus> statusComboBox;
    
    public TripEditDialog(AdminDashboardPanel parent, Trip trip) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), 
              trip == null ? "Add Trip" : "Edit Trip", true);
        this.parent = parent;
        this.originalTrip = trip;
        
        initializeComponents();
        layoutComponents();
        addEventListeners();
        
        if (trip != null) {
            populateFields(trip);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        tripIdField = new JTextField(20);
        tripIdField.setEditable(false);
        
        // Load all users for driver selection
        List<User> users = parent.getApp().getUserManager().getAllUsers();
        driverComboBox = new JComboBox<>(users.toArray(new User[0]));
        
        departureField = new JTextField(20);
        destinationField = new JTextField(20);
        dateTimeField = new JTextField(20);
        dateTimeField.setToolTipText("Format: dd/MM/yyyy HH:mm");
        
        seatsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        priceField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        statusComboBox = new JComboBox<>(Trip.TripStatus.values());
    }
    
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Trip ID (only for editing)
        if (originalTrip != null) {
            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Trip ID:"), gbc);
            gbc.gridx = 1;
            add(tripIdField, gbc);
        }
        
        // Driver
        gbc.gridx = 0; gbc.gridy = originalTrip != null ? 1 : 0;
        add(new JLabel("Driver:"), gbc);
        gbc.gridx = 1;
        add(driverComboBox, gbc);
        
        // Departure
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Departure:"), gbc);
        gbc.gridx = 1;
        add(departureField, gbc);
        
        // Destination
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Destination:"), gbc);
        gbc.gridx = 1;
        add(destinationField, gbc);
        
        // Date Time
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Date & Time:"), gbc);
        gbc.gridx = 1;
        add(dateTimeField, gbc);
        
        // Available Seats
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Available Seats:"), gbc);
        gbc.gridx = 1;
        add(seatsSpinner, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Price per Seat (€):"), gbc);
        gbc.gridx = 1;
        add(priceField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(descriptionArea), gbc);
        
        // Status (only for editing)
        if (originalTrip != null) {
            gbc.gridx = 0; gbc.gridy++;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            add(statusComboBox, gbc);
        }
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.black);
        saveButton.setFocusPainted(false);
        
        cancelButton.setBackground(Color.white);
        cancelButton.setForeground(Color.black);
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gbc);
        
        // Event listeners
        saveButton.addActionListener(this::saveTrip);
        cancelButton.addActionListener(e -> dispose());
    }
    
    private void addEventListeners() {
        // Set current date/time as default for new trips
        if (originalTrip == null) {
            LocalDateTime now = LocalDateTime.now().plusHours(1);
            dateTimeField.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
    
    private void populateFields(Trip trip) {
        tripIdField.setText(trip.getId());
        driverComboBox.setSelectedItem(trip.getDriver());
        departureField.setText(trip.getDeparture());
        destinationField.setText(trip.getDestination());
        dateTimeField.setText(trip.getFormattedDateTime());
        seatsSpinner.setValue(trip.getAvailableSeats());
        priceField.setText(String.valueOf(trip.getPricePerSeat()));
        descriptionArea.setText(trip.getDescription());
        statusComboBox.setSelectedItem(trip.getStatus());
    }
    
    private void saveTrip(ActionEvent e) {
        try {
            User driver = (User) driverComboBox.getSelectedItem();
            String departure = departureField.getText().trim();
            String destination = destinationField.getText().trim();
            String dateTimeStr = dateTimeField.getText().trim();
            int seats = (Integer) seatsSpinner.getValue();
            String priceStr = priceField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            // Validation
            if (driver == null || departure.isEmpty() || destination.isEmpty() || 
                dateTimeStr.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse date/time
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Parse price
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {  // Fixed the XML escape issue here
                JOptionPane.showMessageDialog(this, "Price must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create trip object
            String tripId = originalTrip != null ? originalTrip.getId() : "0"; // Will be set by database
            editedTrip = new Trip(tripId, driver, departure, destination, dateTime, seats, price, description);
            
            if (originalTrip != null) {
                editedTrip.setStatus((Trip.TripStatus) statusComboBox.getSelectedItem());
            }
            
            confirmed = true;
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use: dd/MM/yyyy HH:mm", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format!", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving trip: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Trip getTrip() {
        return editedTrip;
    }
}

