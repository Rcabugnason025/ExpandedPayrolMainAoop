// AttendanceManagementDialog.java - Fixed version without compilation errors
package ui;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import model.Employee;
import model.Attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Dialog for managing employee attendance records
 * Allows HR personnel to add, edit, and delete attendance entries
 */
public class AttendanceManagementDialog extends JDialog {
    private Employee currentUser;
    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;

    // UI Components
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JComboBox<Employee> employeeComboBox;
    private JSpinner dateSpinner;
    private JSpinner logInSpinner;
    private JSpinner logOutSpinner;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton clearButton;
    private JLabel statusLabel;

    // Form state
    private boolean isEditMode = false;
    private int editingAttendanceId = -1;

    public AttendanceManagementDialog(Frame parent, Employee currentUser) {
        super(parent, "Attendance Management", true);
        this.currentUser = currentUser;
        this.attendanceDAO = new AttendanceDAO();
        this.employeeDAO = new EmployeeDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadEmployees();
        loadAttendanceRecords();
        updateButtonStates();

        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        // Table setup
        String[] columns = {"ID", "Employee", "Date", "Log In", "Log Out", "Hours", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setRowHeight(28);
        attendanceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        attendanceTable.getTableHeader().setBackground(new Color(70, 130, 180));
        attendanceTable.getTableHeader().setForeground(Color.WHITE);
        attendanceTable.setFont(new Font("Arial", Font.PLAIN, 11));

        // Set column widths
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Employee
        attendanceTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Date
        attendanceTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Log In
        attendanceTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Log Out
        attendanceTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Hours
        attendanceTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Status

        // Custom renderer for status column - Fixed
        attendanceTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        // Employee combo box - Fixed
        employeeComboBox = new JComboBox<>();
        employeeComboBox.setRenderer(new EmployeeComboBoxRenderer());
        employeeComboBox.setPreferredSize(new Dimension(200, 25));

        // Date and time spinners
        setupDateAndTimeSpinners();

        // Buttons
        setupButtons();

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.DARK_GRAY);
    }

    private void setupDateAndTimeSpinners() {
        // Date spinner
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(Date.valueOf(LocalDate.now()));
        dateSpinner.setPreferredSize(new Dimension(120, 25));

        // Log in time spinner
        logInSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor logInEditor = new JSpinner.DateEditor(logInSpinner, "HH:mm:ss");
        logInSpinner.setEditor(logInEditor);
        logInSpinner.setValue(Time.valueOf(LocalTime.of(8, 0, 0)));
        logInSpinner.setPreferredSize(new Dimension(100, 25));

        // Log out time spinner
        logOutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor logOutEditor = new JSpinner.DateEditor(logOutSpinner, "HH:mm:ss");
        logOutSpinner.setEditor(logOutEditor);
        logOutSpinner.setValue(Time.valueOf(LocalTime.of(17, 0, 0)));
        logOutSpinner.setPreferredSize(new Dimension(100, 25));

        // Add change listeners to validate times
        logInSpinner.addChangeListener(e -> validateTimes());
        logOutSpinner.addChangeListener(e -> validateTimes());
    }

    private void setupButtons() {
        addButton = createStyledButton("Add Record", new Color(34, 139, 34), Color.WHITE);
        updateButton = createStyledButton("Update", new Color(70, 130, 180), Color.WHITE);
        deleteButton = createStyledButton("Delete", new Color(220, 20, 60), Color.WHITE);
        refreshButton = createStyledButton("Refresh", new Color(128, 128, 128), Color.WHITE);
        clearButton = createStyledButton("Clear Form", new Color(255, 140, 0), Color.WHITE);

        // Set button sizes
        Dimension buttonSize = new Dimension(100, 30);
        addButton.setPreferredSize(buttonSize);
        updateButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);
        refreshButton.setPreferredSize(buttonSize);
        clearButton.setPreferredSize(buttonSize);
    }

    private JButton createStyledButton(String text, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = createHeaderPanel();

        // Form panel
        JPanel formPanel = createFormPanel();

        // Table panel
        JPanel tablePanel = createTablePanel();

        // Button panel
        JPanel buttonPanel = createButtonPanel();

        // Status panel
        JPanel statusPanel = createStatusPanel();

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Attendance Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("User: " + currentUser.getFullName());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(Color.LIGHT_GRAY);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Add/Edit Attendance Record",
                0, 0, new Font("Arial", Font.BOLD, 12)));
        formPanel.setBackground(new Color(248, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Employee selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Employee:"), gbc);
        gbc.gridx = 1;
        formPanel.add(employeeComboBox, gbc);

        // Date selection
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 3;
        formPanel.add(dateSpinner, gbc);

        // Log in time
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Log In Time:"), gbc);
        gbc.gridx = 1;
        formPanel.add(logInSpinner, gbc);

        // Log out time
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Log Out Time:"), gbc);
        gbc.gridx = 3;
        formPanel.add(logOutSpinner, gbc);

        return formPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Attendance Records (Last 30 Days)",
                0, 0, new Font("Arial", Font.BOLD, 12)));

        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addAttendanceRecord());
        updateButton.addActionListener(e -> updateAttendanceRecord());
        deleteButton.addActionListener(e -> deleteAttendanceRecord());
        refreshButton.addActionListener(e -> {
            loadAttendanceRecords();
            setStatus("Records refreshed");
        });
        clearButton.addActionListener(e -> clearForm());

        // Table selection listener
        attendanceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
                updateButtonStates();
            }
        });

        // Employee selection listener
        employeeComboBox.addActionListener(e -> validateForm());
    }

    private void loadEmployees() {
        employeeComboBox.removeAllItems();
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            for (Employee emp : employees) {
                employeeComboBox.addItem(emp);
            }
            setStatus("Loaded " + employees.size() + " employees");
        } catch (Exception e) {
            showError("Error loading employees: " + e.getMessage());
        }
    }

    private void loadAttendanceRecords() {
        tableModel.setRowCount(0);

        try {
            // Load recent attendance records (last 30 days)
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);

            List<Employee> employees = employeeDAO.getAllEmployees();
            int totalRecords = 0;

            for (Employee emp : employees) {
                List<Attendance> records = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                        emp.getEmployeeId(), startDate, endDate);

                for (Attendance attendance : records) {
                    String status = determineAttendanceStatus(attendance);

                    Object[] row = {
                            attendance.getId(),
                            emp.getFullName(),
                            attendance.getDate(),
                            attendance.getLogIn(),
                            attendance.getLogOut(),
                            String.format("%.2f", attendance.getWorkHours()),
                            status
                    };
                    tableModel.addRow(row);
                    totalRecords++;
                }
            }

            setStatus("Loaded " + totalRecords + " attendance records");

        } catch (Exception e) {
            showError("Error loading attendance records: " + e.getMessage());
        }
    }

    private String determineAttendanceStatus(Attendance attendance) {
        if (attendance.getLogIn() == null) {
            return "No Log In";
        }
        if (attendance.getLogOut() == null) {
            return "No Log Out";
        }

        boolean isLate = attendance.isLate();
        boolean hasUndertime = attendance.hasUndertime();

        if (isLate && hasUndertime) {
            return "Late & Undertime";
        } else if (isLate) {
            return "Late";
        } else if (hasUndertime) {
            return "Undertime";
        } else if (attendance.isFullDay()) {
            return "Full Day";
        } else {
            return "Present";
        }
    }

    private void addAttendanceRecord() {
        if (!validateForm()) return;

        try {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            Date date = new Date(((java.util.Date) dateSpinner.getValue()).getTime());
            Time logIn = new Time(((java.util.Date) logInSpinner.getValue()).getTime());
            Time logOut = new Time(((java.util.Date) logOutSpinner.getValue()).getTime());

            // Check if attendance already exists
            if (attendanceDAO.attendanceExistsForDate(selectedEmployee.getEmployeeId(), date.toLocalDate())) {
                showWarning("Attendance record already exists for " + selectedEmployee.getFullName() + " on " + date);
                return;
            }

            Attendance attendance = new Attendance(selectedEmployee.getEmployeeId(), date, logIn, logOut);
            int id = attendanceDAO.insertAttendance(attendance);

            if (id > 0) {
                showSuccess("Attendance record added successfully for " + selectedEmployee.getFullName());
                loadAttendanceRecords();
                clearForm();
            } else {
                showError("Failed to add attendance record");
            }

        } catch (Exception e) {
            showError("Error adding attendance record: " + e.getMessage());
        }
    }

    private void updateAttendanceRecord() {
        if (!isEditMode || editingAttendanceId == -1) {
            showWarning("Please select a record to update");
            return;
        }

        if (!validateForm()) return;

        try {
            Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
            Date date = new Date(((java.util.Date) dateSpinner.getValue()).getTime());
            Time logIn = new Time(((java.util.Date) logInSpinner.getValue()).getTime());
            Time logOut = new Time(((java.util.Date) logOutSpinner.getValue()).getTime());

            Attendance attendance = new Attendance(selectedEmployee.getEmployeeId(), date, logIn, logOut);
            attendance.setId(editingAttendanceId);

            boolean updated = attendanceDAO.updateAttendance(attendance);

            if (updated) {
                showSuccess("Attendance record updated successfully");
                loadAttendanceRecords();
                clearForm();
            } else {
                showError("Failed to update attendance record");
            }

        } catch (Exception e) {
            showError("Error updating attendance record: " + e.getMessage());
        }
    }

    private void deleteAttendanceRecord() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a record to delete");
            return;
        }

        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
        Date date = (Date) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the attendance record for " + employeeName + " on " + date + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int attendanceId = (Integer) tableModel.getValueAt(selectedRow, 0);
                boolean deleted = attendanceDAO.deleteAttendance(attendanceId);

                if (deleted) {
                    showSuccess("Attendance record deleted successfully");
                    loadAttendanceRecords();
                    clearForm();
                } else {
                    showError("Failed to delete attendance record");
                }

            } catch (Exception e) {
                showError("Error deleting attendance record: " + e.getMessage());
            }
        }
    }

    private void populateFormFromSelection() {
        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            isEditMode = false;
            editingAttendanceId = -1;
            return;
        }

        try {
            isEditMode = true;
            editingAttendanceId = (Integer) tableModel.getValueAt(selectedRow, 0);

            String employeeName = (String) tableModel.getValueAt(selectedRow, 1);
            Date date = (Date) tableModel.getValueAt(selectedRow, 2);
            Time logIn = (Time) tableModel.getValueAt(selectedRow, 3);
            Time logOut = (Time) tableModel.getValueAt(selectedRow, 4);

            // Find and select the employee
            for (int i = 0; i < employeeComboBox.getItemCount(); i++) {
                Employee emp = employeeComboBox.getItemAt(i);
                if (emp.getFullName().equals(employeeName)) {
                    employeeComboBox.setSelectedIndex(i);
                    break;
                }
            }

            dateSpinner.setValue(date);
            logInSpinner.setValue(logIn);
            logOutSpinner.setValue(logOut);

            setStatus("Editing record for " + employeeName);

        } catch (Exception e) {
            showError("Error populating form: " + e.getMessage());
        }
    }

    private void clearForm() {
        isEditMode = false;
        editingAttendanceId = -1;

        if (employeeComboBox.getItemCount() > 0) {
            employeeComboBox.setSelectedIndex(0);
        }
        dateSpinner.setValue(Date.valueOf(LocalDate.now()));
        logInSpinner.setValue(Time.valueOf(LocalTime.of(8, 0, 0)));
        logOutSpinner.setValue(Time.valueOf(LocalTime.of(17, 0, 0)));

        attendanceTable.clearSelection();
        updateButtonStates();
        setStatus("Form cleared");
    }

    private boolean validateForm() {
        if (employeeComboBox.getSelectedItem() == null) {
            showWarning("Please select an employee");
            return false;
        }

        return validateTimes();
    }

    private boolean validateTimes() {
        try {
            Time logIn = new Time(((java.util.Date) logInSpinner.getValue()).getTime());
            Time logOut = new Time(((java.util.Date) logOutSpinner.getValue()).getTime());

            if (logOut.before(logIn)) {
                showWarning("Log out time cannot be before log in time");
                return false;
            }

            return true;
        } catch (Exception e) {
            showWarning("Invalid time format");
            return false;
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = attendanceTable.getSelectedRow() != -1;
        updateButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);

        addButton.setText(isEditMode ? "Save Changes" : "Add Record");
    }

    // Status and message methods
    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        setStatus(message);
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        setStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        setStatus("Warning: " + message);
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    // Fixed Custom Renderers - Now as inner classes
    private class EmployeeComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Employee) {
                Employee emp = (Employee) value;
                setText(emp.getEmployeeId() + " - " + emp.getFullName());
            }

            return this;
        }
    }

    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && value instanceof String) {
                String status = (String) value;
                switch (status) {
                    case "Late":
                    case "Late & Undertime":
                        setBackground(new Color(255, 182, 193)); // Light red
                        break;
                    case "Undertime":
                        setBackground(new Color(255, 218, 185)); // Peach
                        break;
                    case "Full Day":
                        setBackground(new Color(144, 238, 144)); // Light green
                        break;
                    case "Present":
                        setBackground(new Color(173, 216, 230)); // Light blue
                        break;
                    default:
                        setBackground(Color.WHITE);
                        break;
                }
            } else if (isSelected) {
                setBackground(table.getSelectionBackground());
            }

            return this;
        }
    }
}