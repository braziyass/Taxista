package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SearchTripsPanel extends JPanel {
    private CovoiturageApp app;
    private JTextField departureSearchField;
    private JTextField destinationSearchField;
    private JList<Trip> tripsAsList;
    private DefaultListModel<Trip> tripsModel;

    public SearchTripsPanel(CovoiturageApp app) {
        this.app = app;
        initializeComponents();
        layoutComponents();
        addEventListeners();
        refreshTrips();
    }

    private void initializeComponents() {
        departureSearchField = new JTextField(15);
        destinationSearchField = new JTextField(15);

        tripsModel = new DefaultListModel<>();
        tripsAsList = new JList<>(tripsModel);
        tripsAsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tripsAsList.setCellRenderer(new TripListCellRenderer());
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Search Trips");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setBackground(new Color(220, 220, 220));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> app.showPanel("DASHBOARD"));
        headerPanel.add(backButton, BorderLayout.EAST);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchPanel.add(new JLabel("From:"));
        searchPanel.add(departureSearchField);
        searchPanel.add(new JLabel("To:"));
        searchPanel.add(destinationSearchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(255, 165, 0));
        searchButton.setForeground(Color.black);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(this::searchTrips);
        searchPanel.add(searchButton);

        JButton showAllButton = new JButton("Show All");
        showAllButton.setBackground(new Color(70, 130, 180));
        showAllButton.setForeground(Color.black);
        showAllButton.setFocusPainted(false);
        showAllButton.addActionListener(e -> refreshTrips());
        searchPanel.add(showAllButton);

        // Combine header and search into one top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Available Trips"));

        JScrollPane scrollPane = new JScrollPane(tripsAsList);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        resultsPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout());

        JButton bookButton = new JButton("Book Trip");
        bookButton.setBackground(new Color(60, 179, 113));
        bookButton.setForeground(Color.black);
        bookButton.setFocusPainted(false);
        bookButton.addActionListener(this::bookTrip);

        JButton detailsButton = new JButton("View Details");
        detailsButton.setBackground(new Color(70, 130, 180));
        detailsButton.setForeground(Color.black);
        detailsButton.setFocusPainted(false);
        detailsButton.addActionListener(this::showTripDetails);

        actionPanel.add(bookButton);
        actionPanel.add(detailsButton);
        resultsPanel.add(actionPanel, BorderLayout.SOUTH);

        add(resultsPanel, BorderLayout.CENTER);
    }

    private void addEventListeners() {
        // Double-click to book
        tripsAsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    bookTrip(null);
                }
            }
        });

        // Enter key in search fields
        departureSearchField.addActionListener(e -> searchTrips(null));
        destinationSearchField.addActionListener(e -> searchTrips(null));
    }

    private void searchTrips(ActionEvent e) {
        String departure = departureSearchField.getText().trim();
        String destination = destinationSearchField.getText().trim();

        List<Trip> searchResults = app.getTripManager().searchTrips(departure, destination);

        tripsModel.clear();
        for (Trip trip : searchResults) {
            if (trip.getAvailableSeats() > 0) {
                tripsModel.addElement(trip);
            }
        }

        if (tripsModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trips found matching your criteria.",
                    "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void bookTrip(ActionEvent e) {
        Trip selectedTrip = tripsAsList.getSelectedValue();
        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(this, "Please select a trip to book.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedTrip.getDriver().equals(app.getCurrentUser())) {
            JOptionPane.showMessageDialog(this, "You cannot book your own trip!",
                    "Booking Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedTrip.getPassengers().contains(app.getCurrentUser())) {
            JOptionPane.showMessageDialog(this, "You have already booked this trip!",
                    "Booking Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Book this trip?\n\n" +
                        "From: " + selectedTrip.getDeparture() + "\n" +
                        "To: " + selectedTrip.getDestination() + "\n" +
                        "Date: " + selectedTrip.getFormattedDateTime() + "\n" +
                        "Price: €" + selectedTrip.getPricePerSeat(),
                "Confirm Booking", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = app.getTripManager().bookTrip(selectedTrip.getId(), app.getCurrentUser());
            if (success) {
                JOptionPane.showMessageDialog(this, "Trip booked successfully!",
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                refreshTrips();
                app.refreshTrips();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to book trip. It may be full.",
                        "Booking Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTripDetails(ActionEvent e) {
        Trip selectedTrip = tripsAsList.getSelectedValue();
        if (selectedTrip == null) {
            JOptionPane.showMessageDialog(this, "Please select a trip to view details.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Trip Details:\n\n");
        details.append("From: ").append(selectedTrip.getDeparture()).append("\n");
        details.append("To: ").append(selectedTrip.getDestination()).append("\n");
        details.append("Date: ").append(selectedTrip.getFormattedDateTime()).append("\n");
        details.append("Driver: ").append(selectedTrip.getDriver().getUsername()).append("\n");
        details.append("Driver Phone: ").append(selectedTrip.getDriver().getPhone()).append("\n");
        details.append("Available Seats: ").append(selectedTrip.getAvailableSeats()).append("\n");
        details.append("Price per Seat: €").append(selectedTrip.getPricePerSeat()).append("\n");
        details.append("Status: ").append(selectedTrip.getStatus()).append("\n");
        details.append("Description: ").append(selectedTrip.getDescription()).append("\n\n");

        if (!selectedTrip.getPassengers().isEmpty()) {
            details.append("Current Passengers:\n");
            for (User passenger : selectedTrip.getPassengers()) {
                details.append("- ").append(passenger.getUsername()).append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, details.toString(), "Trip Details", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshTrips() {
        tripsModel.clear();
        List<Trip> availableTrips = app.getTripManager().getAvailableTrips();
        for (Trip trip : availableTrips) {
            tripsModel.addElement(trip);
        }
    }

    // Custom cell renderer for better trip display
    private class TripListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Trip) {
                Trip trip = (Trip) value;
                setText("<html><b>" + trip.getDeparture() + " → " + trip.getDestination() + "</b><br/>" +
                        trip.getFormattedDateTime() + " | " + trip.getAvailableSeats() + " seats | €" +
                        trip.getPricePerSeat() + "<br/>" +
                        "Driver: " + trip.getDriver().getUsername() + "</html>");
            }

            return this;
        }
    }
}
