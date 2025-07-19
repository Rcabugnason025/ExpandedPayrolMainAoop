package ui;

import dao.LeaveRequestDAO;
import model.Employee;
import model.LeaveRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.time.LocalDate;

public class LeaveRequestDialog extends JDialog {
    private Employee employee;
    private LeaveRequestDAO leaveRequestDAO;

    private JComboBox<String> leaveTypeComboBox;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JTextArea reasonTextArea;
    private JButton submitButton;
    private JButton cancelButton;

    public LeaveRequestDialog(Frame parent, Employee employee) {
        super(parent, "Submit Leave Request", true);
        this.employee = employee;
        this.leaveRequestDAO = new LeaveRequestDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initializeComponents() {
        // Leave type combo box
        String[] leaveTypes = {
                LeaveRequest.ANNUAL_LEAVE,
                LeaveRequest.SICK_LEAVE,
                LeaveRequest.EMERGENCY_LEAVE,
                LeaveRequest.MATERNITY_LEAVE,
                LeaveRequest.PATERNITY_LEAVE
        };
        leaveTypeComboBox = new JComboBox<>(leaveTypes);
        leaveTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        // Date spinners
        SpinnerDateModel startDateModel = new SpinnerDateModel();
        SpinnerDateModel endDateModel = new SpinnerDateModel();

        startDateSpinner = new JSpinner(startDateModel);
        endDateSpinner = new JSpinner(endDateModel);

        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");

        startDateSpinner.setEditor(startDateEditor);
        endDateSpinner.setEditor(endDateEditor);

        // Set default dates
        startDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
        endDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));

        // Style the spinners
        startDateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        endDateSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        startDateSpinner.setPreferredSize(new Dimension(150, 30));
        endDateSpinner.setPreferredSize(new Dimension(150, 30));

        // Reason text area
        reasonTextArea = new JTextArea(5, 30);
        reasonTextArea.setLineWrap(true);
        reasonTextArea.setWrapStyleWord(true);
        reasonTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        reasonTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Buttons
        submitButton = new JButton("Submit Request");
        cancelButton = new JButton("Cancel");

        submitButton.setBackground(new Color(70, 130, 180));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setPreferredSize(new Dimension(140, 35));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setOpaque(true);

        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setOpaque(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Leave Request Form");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // Employee info
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 15, 10);
        gbc.gridwidth = 2;
        JLabel empLabel = new JLabel("Employee: " + employee.getFullName() + " (ID: " + employee.getEmployeeId() + ")");
        empLabel.setFont(new Font("Arial", Font.BOLD, 14));
        empLabel.setForeground(new Color(25, 25, 112));
        formPanel.add(empLabel, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        // Leave type
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 5, 10);
        JLabel leaveTypeLabel = new JLabel("Leave Type:");
        leaveTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(leaveTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(leaveTypeComboBox, gbc);

        // Start date
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel startDateLabel = new JLabel("Start Date:");
        startDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(startDateLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(startDateSpinner, gbc);

        // End date
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel endDateLabel = new JLabel("End Date:");
        endDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(endDateLabel, gbc);

        gbc.gridx = 1;
        formPanel.add(endDateSpinner, gbc);

        // Reason
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(15, 0, 5, 10);
        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(reasonLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane reasonScrollPane = new JScrollPane(reasonTextArea);
        reasonScrollPane.setPreferredSize(new Dimension(300, 120));
        formPanel.add(reasonScrollPane, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitLeaveRequest();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Validate end date is not before start date
        startDateSpinner.addChangeListener(e -> validateDates());
        endDateSpinner.addChangeListener(e -> validateDates());

        // Enter key handling for reason text area
        reasonTextArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "submit");
        reasonTextArea.getActionMap().put("submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitLeaveRequest();
            }
        });
    }

    private void validateDates() {
        try {
            java.util.Date utilStartDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date utilEndDate = (java.util.Date) endDateSpinner.getValue();

            if (utilEndDate.before(utilStartDate)) {
                endDateSpinner.setValue(utilStartDate);
            }
        } catch (Exception e) {
            // Handle any date conversion issues silently
        }
    }

    private void submitLeaveRequest() {
        try {
            // FIXED: Proper date conversion from java.util.Date to java.sql.Date
            java.util.Date utilStartDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date utilEndDate = (java.util.Date) endDateSpinner.getValue();

            Date startDate = new Date(utilStartDate.getTime());
            Date endDate = new Date(utilEndDate.getTime());

            String leaveType = (String) leaveTypeComboBox.getSelectedItem();
            String reason = reasonTextArea.getText().trim();

            // Validation
            if (startDate.before(Date.valueOf(LocalDate.now()))) {
                JOptionPane.showMessageDialog(this,
                        "Start date cannot be in the past.",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a reason for your leave request.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                reasonTextArea.requestFocus();
                return;
            }

            if (reason.length() < 10) {
                JOptionPane.showMessageDialog(this,
                        "Please provide a more detailed reason (at least 10 characters).",
                        "Insufficient Information", JOptionPane.WARNING_MESSAGE);
                reasonTextArea.requestFocus();
                return;
            }

            // Check for overlapping leave requests
            try {
                if (leaveRequestDAO.hasOverlappingLeave(employee.getEmployeeId(),
                        startDate.toLocalDate(), endDate.toLocalDate(), null)) {
                    JOptionPane.showMessageDialog(this,
                            "You already have an approved leave request for this period.\n" +
                                    "Please choose different dates or check your existing leave requests.",
                            "Overlapping Leave", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                // If overlap check fails, log but continue with submission
                System.err.println("Warning: Could not check for overlapping leave: " + e.getMessage());
            }

            // Create and submit leave request
            LeaveRequest leaveRequest = new LeaveRequest(employee.getEmployeeId(), startDate, endDate, leaveType);
            leaveRequest.setStatus(LeaveRequest.STATUS_PENDING);

            // Disable submit button to prevent double submission
            submitButton.setEnabled(false);
            submitButton.setText("Submitting...");

            int leaveId = leaveRequestDAO.insertLeaveRequest(leaveRequest);

            if (leaveId > 0) {
                JOptionPane.showMessageDialog(this,
                        "Leave request submitted successfully!\n\n" +
                                "Request ID: " + leaveId + "\n" +
                                "Type: " + leaveType + "\n" +
                                "Period: " + startDate + " to " + endDate + "\n" +
                                "Days: " + leaveRequest.getLeaveDays() + "\n\n" +
                                "Your request is now pending approval.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit leave request. Please try again.\n" +
                                "If the problem persists, contact IT support.",
                        "Submission Error", JOptionPane.ERROR_MESSAGE);

                // Re-enable submit button
                submitButton.setEnabled(true);
                submitButton.setText("Submit Request");
            }

        } catch (Exception e) {
            // Re-enable submit button on error
            submitButton.setEnabled(true);
            submitButton.setText("Submit Request");

            JOptionPane.showMessageDialog(this,
                    "Error submitting leave request: " + e.getMessage() + "\n\n" +
                            "Please check your input and try again.\n" +
                            "If the problem persists, contact IT support.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Helper method to convert java.util.Date to java.sql.Date
     * @param utilDate the java.util.Date to convert
     * @return java.sql.Date or null if input is null
     */
    private Date convertToSqlDate(java.util.Date utilDate) {
        if (utilDate == null) return null;
        return new Date(utilDate.getTime());
    }

    /**
     * Validates the form inputs
     * @return true if all inputs are valid
     */
    private boolean validateForm() {
        // Check leave type
        if (leaveTypeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave type.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            leaveTypeComboBox.requestFocus();
            return false;
        }

        // Check dates
        try {
            java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();

            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select valid start and end dates.",
                        "Invalid Dates", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if (endDate.before(startDate)) {
                JOptionPane.showMessageDialog(this,
                        "End date cannot be before start date.",
                        "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                endDateSpinner.requestFocus();
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Please select valid dates.",
                    "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Check reason
        String reason = reasonTextArea.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please provide a reason for your leave request.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            reasonTextArea.requestFocus();
            return false;
        }

        return true;
    }
}