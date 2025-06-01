package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UserEditDialog extends JDialog {
    private AdminDashboardPanel parent;
    private User originalUser;
    private User editedUser;
    private boolean confirmed = false;
    
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JSpinner ratingSpinner;
    private JSpinner tripsSpinner;
    private JComboBox<User.UserRole> roleComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton showPasswordButton;
    private boolean passwordVisible = false;
    
    public UserEditDialog(AdminDashboardPanel parent, User user) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), 
              user == null ? "Add User" : "Edit User", true);
        this.parent = parent;
        this.originalUser = user;
        
        initializeComponents();
        layoutComponents();
        addEventListeners();
        
        if (user != null) {
            populateFields(user);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        phoneField = new JTextField(20);
        ratingSpinner = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 5.0, 0.1));
        tripsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        roleComboBox = new JComboBox<>(User.UserRole.values());
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        showPasswordButton = new JButton("👁");
        
        // Style buttons
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.black);
        saveButton.setFocusPainted(false);
        
        cancelButton.setBackground(new Color(220, 20, 60));
        cancelButton.setForeground(Color.black);
        cancelButton.setFocusPainted(false);
        
        // Style show password button
        showPasswordButton.setBackground(new Color(70, 130, 180));
        showPasswordButton.setForeground(Color.black);
        showPasswordButton.setFocusPainted(false);
        showPasswordButton.setPreferredSize(new Dimension(40, 25));
        showPasswordButton.setToolTipText("Show/Hide Password");
        showPasswordButton.setFont(new Font("Arial", Font.PLAIN, 12));
    }
    
    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(usernameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(emailField, gbc);
        
        // Password with show/hide button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        add(passwordField, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1;
        add(showPasswordButton, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(phoneField, gbc);
        
        // Rating
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(ratingSpinner, gbc);
        
        // Total Trips
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        add(new JLabel("Total Trips:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(tripsSpinner, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(roleComboBox, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, gbc);
    }
    
    private void addEventListeners() {
        // Event listeners for buttons
        saveButton.addActionListener(this::saveUser);
        cancelButton.addActionListener(e -> dispose());
        showPasswordButton.addActionListener(this::togglePasswordVisibility);
        
        // Set save button as default button (Enter key)
        getRootPane().setDefaultButton(saveButton);
    }
    
    private void togglePasswordVisibility(ActionEvent e) {
        if (passwordVisible) {
            // Hide password
            passwordField.setEchoChar('•');
            showPasswordButton.setText("👁");
            showPasswordButton.setToolTipText("Show Password");
            passwordVisible = false;
        } else {
            // Show password
            passwordField.setEchoChar((char) 0);
            showPasswordButton.setText("🙈");
            showPasswordButton.setToolTipText("Hide Password");
            passwordVisible = true;
        }
    }
    
    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        phoneField.setText(user.getPhone());
        ratingSpinner.setValue(user.getRating());
        tripsSpinner.setValue(user.getTotalTrips());
        roleComboBox.setSelectedItem(user.getRole());
    }
    
    private void saveUser(ActionEvent e) {
        try {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String phone = phoneField.getText().trim();
            double rating = (Double) ratingSpinner.getValue();
            int totalTrips = (Integer) tripsSpinner.getValue();
            User.UserRole role = (User.UserRole) roleComboBox.getSelectedItem();
            
            // Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Email validation
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Password validation
            if (password.length() < 3) {
                JOptionPane.showMessageDialog(this, "Password must be at least 3 characters long!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create user object
            editedUser = new User(username, email, password, phone);
            editedUser.setRating(rating);
            editedUser.setTotalTrips(totalTrips);
            editedUser.setRole(role);
            
            confirmed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public User getUser() {
        return editedUser;
    }
}
