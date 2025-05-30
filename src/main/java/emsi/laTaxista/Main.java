package emsi.laTaxista;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Initialize database
            DatabaseManager.initialize();
            
            // Start application
            new CovoiturageApp();
            
            // Add shutdown hook to close database connection
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseManager.closeConnection();
            }));
        });
    }
}
