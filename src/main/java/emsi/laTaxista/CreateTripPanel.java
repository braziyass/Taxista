package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CreateTripPanel extends JPanel {
    private CovoiturageApp app;
    private JTextField departureField;
    private JTextField destinationField;
    private JTextField dateTimeField;
    private JSpinner seatsSpinner;
    private JTextField priceField;
    private JTextArea descriptionArea;
    
    public CreateTripPanel(CovoiturageApp app) {
        this.app = app;
        initializeComponents();
        layoutComponents();
        addEventListeners();
    }
    
    private void initializeComponents() {
        departureField = new JTextField(20);
        destinationField = new JTextField(20);
        dateTimeField = new JTextField(20);
        dateTimeField.setToolTipText("Format: dd/MM/yyyy HH:mm");
        
        seatsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        priceField = new JTextField(20);
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Create New Trip");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.black);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setBackground(new Color(220, 220, 220));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> app.showPanel("DASHBOARD"));
        headerPanel.add(backButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Departure
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Departure City:"), gbc);
        gbc.gridx = 1;
        formPanel.add(departureField, gbc);
        
        // Destination
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Destination City:"), gbc);
        gbc.gridx = 1;
        formPanel.add(destinationField, gbc);
        
        // Date and Time
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date & Time:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateTimeField, gbc);
        
        // Available Seats
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Available Seats:"), gbc);
        gbc.gridx = 1;
        formPanel.add(seatsSpinner, gbc);
        
        // Price per Seat
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Price per Seat (€):"), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        // Create Button
        JButton createButton = new JButton("Create Trip");
        createButton.setBackground(new Color(60, 179, 113));
        createButton.setForeground(Color.black);
        createButton.setFocusPainted(false);
        createButton.setFont(new Font("Arial", Font.BOLD, 16));
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(createButton, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        createButton.addActionListener(this::createTrip);
    }
    
    private void addEventListeners() {
        // Set current date/time as placeholder
        LocalDateTime now = LocalDateTime.now().plusHours(1);
        dateTimeField.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    
    private void createTrip(ActionEvent e) {
        try {
            String departure = departureField.getText().trim();
            String destination = destinationField.getText().trim();
            String dateTimeStr = dateTimeField.getText().trim();
            int seats = (Integer) seatsSpinner.getValue();
            String priceStr = priceField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            // Validation
            if (departure.isEmpty() || destination.isEmpty() || dateTimeStr.isEmpty() || 
                priceStr.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse date/time
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Check if date is in the future
            if (dateTime.isBefore(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(this, "Trip date must be in the future!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse price
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create trip
            String tripId = app.getTripManager().createTrip(
                app.getCurrentUser(), departure, destination, dateTime, seats, price, description
            );
            
            JOptionPane.showMessageDialog(this, "Trip created successfully!\nTrip ID: " + tripId, 
                                        "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear form
            clearForm();
            
            // Refresh trips and go back to dashboard
            app.refreshTrips();
            app.showPanel("DASHBOARD");
            
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use: dd/MM/yyyy HH:mm", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating trip: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        departureField.setText("");
        destinationField.setText("");
        LocalDateTime now = LocalDateTime.now().plusHours(1);
        dateTimeField.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        seatsSpinner.setValue(1);
        priceField.setText("");
        descriptionArea.setText("");
    }
}