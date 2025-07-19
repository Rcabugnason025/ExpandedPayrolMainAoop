package ui;

import util.DBConnection;
import ui.LoginForm;
import javax.swing.*;
import java.awt.*;

public class MainApplication {

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            // Try to set system look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Use default if system L&F fails
            System.out.println("Could not set system look and feel, using default");
        }

        // Test database connection first
        SwingUtilities.invokeLater(() -> {
            showSplashScreen();
        });
    }

    private static void showSplashScreen() {
        JFrame splash = new JFrame();
        splash.setUndecorated(true);
        splash.setSize(400, 300);
        splash.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        panel.setBackground(new Color(240, 248, 255));

        // Logo/Title
        JLabel titleLabel = new JLabel("MotorPH Payroll System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        // Loading message
        JLabel loadingLabel = new JLabel("Initializing system...", JLabel.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(10, 40, 40, 40));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        splash.add(panel);
        splash.setVisible(true);

        // Test database connection in background
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Connecting to database...");
                Thread.sleep(1000);

                boolean connected = DBConnection.testConnection();

                if (connected) {
                    publish("Database connected successfully!");
                    Thread.sleep(500);
                    publish("Loading application...");
                    Thread.sleep(1000);
                    return true;
                } else {
                    publish("Database connection failed!");
                    Thread.sleep(2000);
                    return false;
                }
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                loadingLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    splash.dispose();

                    if (success) {
                        // Show login form
                        new LoginForm().setVisible(true);
                    } else {
                        // Show error dialog
                        JOptionPane.showMessageDialog(null,
                                "Failed to connect to database.\nPlease check your database connection and try again.",
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } catch (Exception e) {
                    splash.dispose();
                    JOptionPane.showMessageDialog(null,
                            "An error occurred: " + e.getMessage(),
                            "Application Error",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        };

        worker.execute();
    }
}