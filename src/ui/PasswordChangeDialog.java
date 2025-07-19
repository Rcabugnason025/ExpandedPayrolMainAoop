package ui;

import dao.CredentialsDAO;
import model.Employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PasswordChangeDialog extends JDialog {
    private Employee employee;
    private CredentialsDAO credentialsDAO;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton changeButton;
    private JButton cancelButton;
    private JCheckBox showPasswordsCheckBox;

    public PasswordChangeDialog(Frame parent, Employee employee) {
        super(parent, "Change Password", true);
        this.employee = employee;
        this.credentialsDAO = new CredentialsDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        setSize(450, 350); // Increased width for better field visibility
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initializeComponents() {
        // FIXED: Make password fields wider
        currentPasswordField = new JPasswordField(25); // Increased from 20 to 25
        newPasswordField = new JPasswordField(25);
        confirmPasswordField = new JPasswordField(25);

        // Set preferred size for better visibility
        Dimension fieldSize = new Dimension(300, 30);
        currentPasswordField.setPreferredSize(fieldSize);
        newPasswordField.setPreferredSize(fieldSize);
        confirmPasswordField.setPreferredSize(fieldSize);

        // Style the password fields
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);
        currentPasswordField.setFont(fieldFont);
        newPasswordField.setFont(fieldFont);
        confirmPasswordField.setFont(fieldFont);

        // Add show passwords checkbox
        showPasswordsCheckBox = new JCheckBox("Show passwords");
        showPasswordsCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));

        changeButton = new JButton("Change Password");
        cancelButton = new JButton("Cancel");

        // Style buttons
        changeButton.setBackground(new Color(70, 130, 180));
        changeButton.setForeground(Color.BLACK);
        changeButton.setFont(new Font("Arial", Font.BOLD, 12));
        changeButton.setPreferredSize(new Dimension(140, 35));

        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(100, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form panel with better spacing
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // Employee info
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0);
        JLabel empLabel = new JLabel("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        empLabel.setFont(new Font("Arial", Font.BOLD, 14));
        empLabel.setForeground(new Color(25, 25, 112));
        formPanel.add(empLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 15);

        // Current password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        currentLabel.setPreferredSize(new Dimension(140, 25));
        formPanel.add(currentLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(currentPasswordField, gbc);

        // New password
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(new Font("Arial", Font.BOLD, 14));
        newLabel.setPreferredSize(new Dimension(140, 25));
        formPanel.add(newLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(newPasswordField, gbc);

        // Confirm password
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmLabel.setPreferredSize(new Dimension(140, 25));
        formPanel.add(confirmLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(confirmPasswordField, gbc);

        // Show passwords checkbox
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(showPasswordsCheckBox, gbc);

        // Password requirements
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel requirementsLabel = new JLabel("<html><center><i>Password must be at least 8 characters long<br/>and different from your current password</i></center></html>");
        requirementsLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        requirementsLabel.setForeground(Color.GRAY);
        formPanel.add(requirementsLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePassword();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Show/hide passwords functionality
        showPasswordsCheckBox.addActionListener(e -> {
            char echoChar = showPasswordsCheckBox.isSelected() ? (char) 0 : '•';
            currentPasswordField.setEchoChar(echoChar);
            newPasswordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });

        // Enter key handling
        currentPasswordField.addActionListener(e -> newPasswordField.requestFocus());
        newPasswordField.addActionListener(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.addActionListener(e -> changePassword());

        // Set initial focus
        SwingUtilities.invokeLater(() -> currentPasswordField.requestFocus());
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validate inputs
        if (currentPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your current password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            currentPasswordField.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a new password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            JOptionPane.showMessageDialog(this, "New password must be at least 8 characters long.",
                    "Invalid Password", JOptionPane.WARNING_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.",
                    "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }

        if (newPassword.equals(currentPassword)) {
            JOptionPane.showMessageDialog(this, "New password must be different from current password.",
                    "Same Password", JOptionPane.WARNING_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        // Disable button during processing
        changeButton.setEnabled(false);
        changeButton.setText("Changing...");

        try {
            // Verify current password
            if (!credentialsDAO.authenticateUser(employee.getEmployeeId(), currentPassword)) {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                currentPasswordField.requestFocus();
                return;
            }

            // Update password
            if (credentialsDAO.updatePassword(employee.getEmployeeId(), newPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Password changed successfully!\n\nYour new password is now active.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error changing password: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Re-enable button and clear fields
            changeButton.setEnabled(true);
            changeButton.setText("Change Password");

            // Clear password fields for security
            currentPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        }
    }
}