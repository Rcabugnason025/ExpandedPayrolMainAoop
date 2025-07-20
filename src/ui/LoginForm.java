package ui;

import dao.EmployeeDAO;
import model.Employee;
import util.DBConnection;
import ui.HRDashboard;
import ui.EmployeeDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LoginForm extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginForm.class.getName());

    private JTextField employeeIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    private JLabel connectionStatusLabel;

    public LoginForm() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        checkDatabaseConnection();

        setTitle("MotorPH Payroll System - Login");
        setSize(550, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(500, 400));

        // Set application icon if available
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    private void initializeComponents() {
        // Create components
        employeeIdField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        cancelButton = new JButton("Exit");
        statusLabel = new JLabel(" ");
        showPasswordCheckBox = new JCheckBox("Show Password");
        connectionStatusLabel = new JLabel(" ");

        // Style components
        employeeIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.RED);
        showPasswordCheckBox.setFont(new Font("Arial", Font.PLAIN, 11));
        connectionStatusLabel.setFont(new Font("Arial", Font.ITALIC, 10));

        // Set button colors
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setOpaque(true);

        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);

        // Style text fields
        employeeIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("MotorPH Payroll System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel subtitleLabel = new JLabel("Employee Login Portal", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Employee ID panel
        JPanel empIdPanel = new JPanel(new BorderLayout(10, 0));
        empIdPanel.setBackground(Color.WHITE);
        empIdPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel empIdLabel = new JLabel("Employee ID:");
        empIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        empIdLabel.setPreferredSize(new Dimension(110, 25));

        employeeIdField.setPreferredSize(new Dimension(200, 30));
        employeeIdField.setFont(new Font("Arial", Font.PLAIN, 14));

        empIdPanel.add(empIdLabel, BorderLayout.WEST);
        empIdPanel.add(employeeIdField, BorderLayout.CENTER);

        // Password panel
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setPreferredSize(new Dimension(110, 25));

        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);

        // Show password panel
        JPanel showPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPasswordPanel.setBackground(Color.WHITE);
        showPasswordPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordPanel.add(Box.createHorizontalStrut(110));
        showPasswordPanel.add(showPasswordCheckBox);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusPanel.add(statusLabel);

        // Add components with spacing
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(empIdPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(passwordPanel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(showPasswordPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(statusPanel);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);

        // Make buttons same size
        Dimension buttonSize = new Dimension(120, 35);
        loginButton.setPreferredSize(buttonSize);
        cancelButton.setPreferredSize(buttonSize);

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Connection status panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectionPanel.setBackground(Color.WHITE);
        connectionPanel.add(connectionStatusLabel);

        // PROFESSIONAL: Logo and company info panel (NO CREDENTIALS SHOWN)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(248, 248, 255));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(new Color(248, 248, 255));
        
        // Create motorcycle logo using text/symbols (you can replace with actual image later)
        JLabel logoLabel = new JLabel("<html><center>" +
                "<div style='font-size: 24px; color: #D32F2F;'>🏍️</div>" +
                "<div style='font-size: 16px; font-weight: bold; color: #1976D2;'>MotorPH</div>" +
                "<div style='font-size: 12px; color: #666;'>Motorcycle Parts & Services</div>" +
                "</center></html>");
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logoLabel);

        // Security notice panel
        JPanel securityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        securityPanel.setBackground(new Color(248, 248, 255));
        
        JLabel securityLabel = new JLabel("<html><center>" +
                "<small style='color: #666;'>🔒 Secure Employee Access Portal</small><br/>" +
                "<small style='color: #666;'>Contact IT Support for login assistance</small>" +
                "</center></html>");
        securityLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        securityPanel.add(securityLabel);

        infoPanel.add(logoPanel, BorderLayout.CENTER);
        infoPanel.add(securityPanel, BorderLayout.SOUTH);

        // Assemble main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Bottom panel with connection status and info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(connectionPanel, BorderLayout.NORTH);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        LoginForm.this,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        // Show/hide password functionality
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0); // Show password
            } else {
                passwordField.setEchoChar('•'); // Hide password
            }
        });

        // Enter key support
        employeeIdField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> performLogin());

        // Set initial focus
        SwingUtilities.invokeLater(() -> employeeIdField.requestFocus());
    }

    private void checkDatabaseConnection() {
        SwingWorker<Boolean, Void> connectionChecker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        connectionStatusLabel.setText("🟢 Database Connected");
                        connectionStatusLabel.setForeground(new Color(0, 128, 0));
                        LOGGER.info("Database connection verified successfully");
                    } else {
                        connectionStatusLabel.setText("🔴 Database Connection Failed");
                        connectionStatusLabel.setForeground(Color.RED);
                        LOGGER.warning("Database connection failed during startup");
                    }
                } catch (Exception e) {
                    connectionStatusLabel.setText("🔴 Database Error");
                    connectionStatusLabel.setForeground(Color.RED);
                    LOGGER.log(Level.SEVERE, "Database connection check failed", e);
                }
            }
        };
        connectionChecker.execute();
    }

    private void performLogin() {
        String employeeIdStr = employeeIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Clear previous status
        statusLabel.setText(" ");

        // Validate input
        if (employeeIdStr.isEmpty()) {
            showStatus("⚠️ Please enter your Employee ID", Color.RED);
            employeeIdField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showStatus("⚠️ Please enter your password", Color.RED);
            passwordField.requestFocus();
            return;
        }

        // Parse employee ID
        int employeeId;
        try {
            employeeId = Integer.parseInt(employeeIdStr);
        } catch (NumberFormatException e) {
            showStatus("⚠️ Employee ID must be a number", Color.RED);
            employeeIdField.selectAll();
            employeeIdField.requestFocus();
            return;
        }

        // DYNAMIC VALIDATION: Check if employee exists in database
        if (!isValidEmployeeId(employeeId)) {
            showStatus("⚠️ Employee ID not found in system", Color.RED);
            employeeIdField.selectAll();
            employeeIdField.requestFocus();
            return;
        }

        // Disable login button during authentication
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Perform authentication in background thread
        SwingWorker<LoginResult, Void> authWorker = new SwingWorker<LoginResult, Void>() {
            @Override
            protected LoginResult doInBackground() throws Exception {
                return performAuthentication(employeeId, password);
            }

            @Override
            protected void done() {
                try {
                    LoginResult result = get();

                    // Reset UI state
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    setCursor(Cursor.getDefaultCursor());

                    if (result.isSuccess()) {
                        handleSuccessfulLogin(result.getEmployee());
                    } else {
                        handleFailedLogin(result.getErrorMessage());
                    }
                } catch (Exception e) {
                    // Reset UI state
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    setCursor(Cursor.getDefaultCursor());

                    showStatus("❌ Login error: " + e.getMessage(), Color.RED);
                    LOGGER.log(Level.SEVERE, "Login process failed", e);
                }
            }
        };

        authWorker.execute();
    }

    /**
     * DYNAMIC VALIDATION: Check if employee ID exists in database
     * This replaces hardcoded range validation and works for any number of employees
     */
    private boolean isValidEmployeeId(int employeeId) {
        String query = "SELECT COUNT(*) FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    LOGGER.info(String.format("Employee ID validation: %d -> %s", 
                            employeeId, exists ? "EXISTS" : "NOT FOUND"));
                    return exists;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error validating employee ID: " + employeeId, e);
        }
        return false;
    }

    private LoginResult performAuthentication(int employeeId, String password) {
        try {
            // Test database connection first
            if (!DBConnection.testConnection()) {
                return new LoginResult(false, null, "Database connection failed. Please check your connection.");
            }

            // Authenticate user
            if (!authenticateUser(employeeId, password)) {
                return new LoginResult(false, null, "Invalid Employee ID or Password");
            }

            // Get employee details
            EmployeeDAO employeeDAO = new EmployeeDAO();
            Employee employee = employeeDAO.getEmployeeById(employeeId);

            if (employee == null) {
                return new LoginResult(false, null, "Employee record not found");
            }

            return new LoginResult(true, employee, null);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Authentication failed", e);
            return new LoginResult(false, null, "Authentication failed: " + e.getMessage());
        }
    }

    // FIXED: Authentication method with correct column name 'password_hash'
    private boolean authenticateUser(int employeeId, String password) {
        // CORRECTED: Using 'password_hash' column instead of 'password'
        String query = "SELECT e.employee_id, e.first_name, e.last_name, e.position FROM employees e " +
                "JOIN credentials c ON e.employee_id = c.employee_id " +
                "WHERE e.employee_id = ? AND c.password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean authenticated = rs.next();
                
                if (authenticated) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String position = rs.getString("position");
                    LOGGER.info(String.format("✅ Authentication SUCCESS for %s %s (ID: %d, Position: %s)",
                            firstName, lastName, employeeId, position));
                } else {
                    LOGGER.warning(String.format("❌ Authentication FAILED for employee ID: %d", employeeId));
                    
                    // Enhanced debugging with correct column names
                    String debugQuery = "SELECT c.employee_id, c.username, c.password_hash, " +
                            "e.first_name, e.last_name " +
                            "FROM credentials c " +
                            "JOIN employees e ON c.employee_id = e.employee_id " +
                            "WHERE c.employee_id = ?";
                    
                    try (PreparedStatement debugStmt = conn.prepareStatement(debugQuery)) {
                        debugStmt.setInt(1, employeeId);
                        try (ResultSet debugRs = debugStmt.executeQuery()) {
                            if (debugRs.next()) {
                                String username = debugRs.getString("username");
                                String actualPasswordHash = debugRs.getString("password_hash");
                                String firstName = debugRs.getString("first_name");
                                String lastName = debugRs.getString("last_name");
                                
                                LOGGER.info(String.format("🔍 Debug Info for Employee %d:", employeeId));
                                LOGGER.info(String.format("   Name: %s %s", firstName, lastName));
                                LOGGER.info(String.format("   Username: %s", username));
                                LOGGER.info(String.format("   Stored Password Hash: %s", actualPasswordHash));
                                LOGGER.info(String.format("   Provided Password: %s", password));
                                LOGGER.info(String.format("   Password Match: %s", password.equals(actualPasswordHash)));
                            } else {
                                LOGGER.warning("❌ Employee ID does not exist in credentials table: " + employeeId);
                            }
                        }
                    }
                }
                
                return authenticated;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", e);
            return false;
        }
    }

    private void handleSuccessfulLogin(Employee employee) {
        showStatus("✅ Login successful! Welcome, " + employee.getFirstName(), new Color(0, 128, 0));

        // Log successful login
        LOGGER.info(String.format("Successful login: %s (ID: %d, Position: %s)",
                employee.getFullName(), employee.getEmployeeId(), employee.getPosition()));

        // Determine user role and open appropriate dashboard
        boolean isHR = isHRRole(employee.getPosition());

        LOGGER.info(String.format("Role determination for %s: %s -> %s Dashboard",
                employee.getFullName(), employee.getPosition(), isHR ? "HR" : "Employee"));

        // Small delay to show success message
        Timer timer = new Timer(1500, ev -> {
            dispose();
            if (isHR) {
                SwingUtilities.invokeLater(() -> {
                    new HRDashboard(employee).setVisible(true);
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    new EmployeeDashboard(employee).setVisible(true);
                });
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void handleFailedLogin(String errorMessage) {
        showStatus("❌ " + errorMessage, Color.RED);
        passwordField.setText("");
        passwordField.requestFocus();

        // Add subtle shake animation
        Timer shakeTimer = new Timer(50, null);
        final int[] shakeCount = {0};
        final Point originalLocation = getLocation();

        shakeTimer.addActionListener(e -> {
            if (shakeCount[0] < 6) {
                setLocation(originalLocation.x + (shakeCount[0] % 2 == 0 ? 5 : -5), originalLocation.y);
                shakeCount[0]++;
            } else {
                setLocation(originalLocation);
                shakeTimer.stop();
            }
        });
        shakeTimer.start();
    }

    /**
     * Enhanced role detection with better logic and null safety
     * Determines if a user should have access to HR Dashboard based on their position
     */
    private boolean isHRRole(String position) {
        if (position == null) {
            LOGGER.warning("Position is null - defaulting to employee role");
            return false;
        }

        String pos = position.toLowerCase().trim();

        // Define HR/Management roles with detailed logging
        boolean isHR = pos.contains("hr") ||
                pos.contains("human resource") ||
                pos.contains("payroll") ||
                pos.contains("chief executive officer") ||
                pos.contains("chief operating officer") ||
                pos.contains("chief finance officer") ||
                pos.contains("chief marketing officer") ||
                pos.contains("manager") ||
                pos.contains("head") ||
                pos.contains("director") ||
                pos.contains("supervisor") ||
                pos.contains("team leader");

        LOGGER.info(String.format("Role check: '%s' -> %s role", position, isHR ? "HR/Management" : "Employee"));

        return isHR;
    }

    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);

        // Auto-clear status after 10 seconds for non-error messages
        if (!color.equals(Color.RED)) {
            Timer clearTimer = new Timer(10000, e -> statusLabel.setText(" "));
            clearTimer.setRepeats(false);
            clearTimer.start();
        }
    }

    /**
     * Inner class to hold login result data
     */
    private static class LoginResult {
        private final boolean success;
        private final Employee employee;
        private final String errorMessage;

        public LoginResult(boolean success, Employee employee, String errorMessage) {
            this.success = success;
            this.employee = employee;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public Employee getEmployee() { return employee; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
