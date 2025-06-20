package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private CovoiturageApp app;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton showPasswordButton;
    private boolean passwordVisible = false;
    
    public LoginPanel(CovoiturageApp app) {
        this.app = app;
        initializeComponents();
        layoutComponents();
        addEventListeners();
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        showPasswordButton = new JButton("👁");
        
        // Style buttons
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.black);
        loginButton.setFocusPainted(false);
        
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.black);
        registerButton.setFocusPainted(false);
        
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
        setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Covoiturage");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(70, 130, 180));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Share your journey, reduce costs");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        add(subtitleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        add(usernameField, gbc);
        
        // Password with show/hide button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        add(passwordField, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1;
        add(showPasswordButton, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 3;
        add(buttonPanel, gbc);
        
        // Demo info
        JLabel demoLabel = new JLabel("<html><center>Demo accounts:<br/>admin/admin or yassine/password</center></html>");
        demoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        demoLabel.setForeground(Color.GRAY);
        gbc.gridy = 5;
        add(demoLabel, gbc);
    }
    
    private void addEventListeners() {
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            app.login(username, password);
        });
        
        registerButton.addActionListener(e -> showRegistrationDialog());
        showPasswordButton.addActionListener(this::togglePasswordVisibility);
        
        // Enter key support
        ActionListener loginAction = e -> loginButton.doClick();
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
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
    
    private void showRegistrationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register", true);
        dialog.setLayout(new GridBagLayout());
        
        JTextField regUsernameField = new JTextField(20);
        JTextField regEmailField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JTextField regPhoneField = new JTextField(20);
        JButton regShowPasswordButton = new JButton("👁");
        
        // Style registration show password button
        regShowPasswordButton.setBackground(new Color(70, 130, 180));
        regShowPasswordButton.setForeground(Color.WHITE);
        regShowPasswordButton.setFocusPainted(false);
        regShowPasswordButton.setPreferredSize(new Dimension(40, 25));
        regShowPasswordButton.setToolTipText("Show/Hide Password");
        regShowPasswordButton.setFont(new Font("Arial", Font.PLAIN, 12));
        
        final boolean[] regPasswordVisible = {false};
        regShowPasswordButton.addActionListener(e -> {
            if (regPasswordVisible[0]) {
                // Hide password
                regPasswordField.setEchoChar('•');
                regShowPasswordButton.setText("👁");
                regShowPasswordButton.setToolTipText("Show Password");
                regPasswordVisible[0] = false;
            } else {
                // Show password
                regPasswordField.setEchoChar((char) 0);
                regShowPasswordButton.setText("🙈");
                regShowPasswordButton.setToolTipText("Hide Password");
                regPasswordVisible[0] = true;
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialog.add(regUsernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialog.add(regEmailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1;
        dialog.add(regPasswordField, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1;
        dialog.add(regShowPasswordButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialog.add(regPhoneField, gbc);
        
        JButton regButton = new JButton("Register");
        regButton.setBackground(new Color(60, 179, 113));
        regButton.setForeground(Color.WHITE);
        regButton.setFocusPainted(false);
        
        regButton.addActionListener(e -> {
            String username = regUsernameField.getText();
            String email = regEmailField.getText();
            String password = new String(regPasswordField.getPassword());
            String phone = regPhoneField.getText();
            
            if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !phone.isEmpty()) {
                app.register(username, email, password, phone);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        dialog.add(regButton, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
