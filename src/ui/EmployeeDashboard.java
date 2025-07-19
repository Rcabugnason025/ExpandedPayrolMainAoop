package ui;

import dao.AttendanceDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import ui.PayrollDetailsDialog;
import ui.LoginForm;
import ui.LeaveRequestDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Personal Info Tab
    private JLabel nameLabel, positionLabel, statusLabel, salaryLabel;
    private JLabel phoneLabel, addressLabel, sssLabel, philhealthLabel;

    // Attendance Tab
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JLabel totalDaysLabel, averageHoursLabel;

    // Payroll Tab
    private JTable payrollTable;
    private DefaultTableModel payrollTableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;

    // Services
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;

    public EmployeeDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Initialize DAOs and services
            this.attendanceDAO = new AttendanceDAO();
            this.payrollCalculator = new PayrollCalculator();

            // Initialize UI components
            initializeComponents();
            setupLayout();
            setupEventHandlers();

            // Load initial data
            loadData();

            // Log successful initialization
            System.out.println("✅ Employee Dashboard initialized successfully for: " + user.getFullName());

        } catch (Exception e) {
            // Log the error
            System.err.println("❌ Employee Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog to user
            JOptionPane.showMessageDialog(this,
                    "Error initializing dashboard: " + e.getMessage() +
                            "\n\nPlease try the following:\n" +
                            "1. Check database connection\n" +
                            "2. Restart the application\n" +
                            "3. Contact IT support if problem persists",
                    "Dashboard Initialization Error",
                    JOptionPane.ERROR_MESSAGE);

            // Create minimal error interface
            createErrorInterface(e);
        }

        // Set window properties (always executed)
        setTitle("MotorPH Payroll System - Employee Dashboard");
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Creates a minimal error interface when dashboard initialization fails
     */
    private void createErrorInterface(Exception error) {
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(220, 53, 69)); // Red background
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("⚠️ Dashboard Initialization Failed", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Main error panel
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        errorPanel.setBackground(Color.WHITE);

        // Error message
        String errorMessage = "<html><center>" +
                "<h2>🔧 System Error</h2>" +
                "<p><b>Dashboard initialization failed. Please contact IT support.</b></p>" +
                "<br>" +
                "<p><b>User:</b> " + (currentUser != null ? currentUser.getFullName() : "Unknown") + "</p>" +
                "<p><b>Error:</b> " + error.getClass().getSimpleName() + "</p>" +
                "<p><b>Message:</b> " + error.getMessage() + "</p>" +
                "<br>" +
                "<p><i>You can try logging out and logging back in,<br>" +
                "or contact your system administrator for assistance.</i></p>" +
                "</center></html>";

        JLabel messageLabel = new JLabel(errorMessage, JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton retryButton = new JButton("🔄 Retry");
        JButton logoutButton = new JButton("🚪 Logout");
        JButton exitButton = new JButton("❌ Exit");

        // Style buttons
        retryButton.setBackground(new Color(40, 167, 69));
        retryButton.setForeground(Color.WHITE);
        retryButton.setFont(new Font("Arial", Font.BOLD, 12));

        logoutButton.setBackground(new Color(108, 117, 125));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));

        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Button actions
        retryButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new EmployeeDashboard(currentUser).setVisible(true);
            });
        });

        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginForm().setVisible(true);
            });
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the application?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(1);
            }
        });

        buttonPanel.add(retryButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(exitButton);

        errorPanel.add(messageLabel, BorderLayout.CENTER);
        errorPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(errorPanel, BorderLayout.CENTER);
    }

    private void initializeComponents() {
        tabbedPane = new JTabbedPane();

        // Personal Info Labels
        nameLabel = new JLabel();
        positionLabel = new JLabel();
        statusLabel = new JLabel();
        salaryLabel = new JLabel();
        phoneLabel = new JLabel();
        addressLabel = new JLabel();
        sssLabel = new JLabel();
        philhealthLabel = new JLabel();

        // Attendance Table
        String[] attendanceColumns = {"Date", "Log In", "Log Out", "Work Hours", "Status"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        setupTableStyling(attendanceTable);

        totalDaysLabel = new JLabel();
        averageHoursLabel = new JLabel();

        // Payroll Table
        String[] payrollColumns = {"Period", "Days Worked", "Gross Pay", "Deductions", "Net Pay", "Actions"};
        payrollTableModel = new DefaultTableModel(payrollColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Actions column
            }
        };
        payrollTable = new JTable(payrollTableModel);
        setupTableStyling(payrollTable);

        // Month/Year selectors
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        String[] years = {"2023", "2024", "2025"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");
    }

    private void setupTableStyling(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(173, 180, 189));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(Color.LIGHT_GRAY);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabs
        tabbedPane.addTab("Personal Information", createPersonalInfoTab());
        tabbedPane.addTab("My Attendance", createAttendanceTab());
        tabbedPane.addTab("My Payroll", createPayrollTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Welcome, " + currentUser.getFullName() + " | Employee ID: " + currentUser.getEmployeeId());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("MotorPH Payroll System - Employee Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(25, 25, 112));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(25, 25, 112));
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createPersonalInfoTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create main info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Title
        JLabel titleLabel = new JLabel("Personal Information");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        infoPanel.add(titleLabel, gbc);

        // Reset grid settings
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Add fields
        addInfoField(infoPanel, gbc, "Full Name:", nameLabel, 1);
        addInfoField(infoPanel, gbc, "Position:", positionLabel, 2);
        addInfoField(infoPanel, gbc, "Employment Status:", statusLabel, 3);
        addInfoField(infoPanel, gbc, "Basic Salary:", salaryLabel, 4);
        addInfoField(infoPanel, gbc, "Phone Number:", phoneLabel, 5);
        addInfoField(infoPanel, gbc, "Address:", addressLabel, 6);
        addInfoField(infoPanel, gbc, "SSS Number:", sssLabel, 7);
        addInfoField(infoPanel, gbc, "PhilHealth Number:", philhealthLabel, 8);

        // Add allowances panel
        JPanel allowancesPanel = createAllowancesPanel();

        // Action panel
        JPanel actionPanel = createActionPanel();

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(allowancesPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addInfoField(JPanel parent, GridBagConstraints gbc, String labelText, JLabel valueLabel, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.insets = new Insets(5, 0, 5, 20);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        parent.add(label, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        parent.add(valueLabel, gbc);
    }

    private JPanel createAllowancesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Monthly Allowances",
                0, 0, new Font("Arial", Font.BOLD, 16)));

        JPanel allowanceGrid = new JPanel(new GridLayout(2, 2, 20, 10));
        allowanceGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Rice Subsidy
        JPanel ricePanel = createAllowanceCard("Rice Subsidy",
                String.format("₱%.2f", currentUser.getRiceSubsidy()),
                new Color(144, 238, 144));

        // Phone Allowance
        JPanel phonePanel = createAllowanceCard("Phone Allowance",
                String.format("₱%.2f", currentUser.getPhoneAllowance()),
                new Color(173, 216, 230));

        // Clothing Allowance
        JPanel clothingPanel = createAllowanceCard("Clothing Allowance",
                String.format("₱%.2f", currentUser.getClothingAllowance()),
                new Color(255, 182, 193));

        // Total Allowances
        double totalAllowances = currentUser.getRiceSubsidy() +
                currentUser.getPhoneAllowance() +
                currentUser.getClothingAllowance();
        JPanel totalPanel = createAllowanceCard("Total Allowances",
                String.format("₱%.2f", totalAllowances),
                new Color(255, 215, 0));

        allowanceGrid.add(ricePanel);
        allowanceGrid.add(phonePanel);
        allowanceGrid.add(clothingPanel);
        allowanceGrid.add(totalPanel);

        panel.add(allowanceGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton leaveRequestButton = new JButton("Submit Leave Request");

        leaveRequestButton.addActionListener(e -> showLeaveRequestDialog());
        leaveRequestButton.setPreferredSize(new Dimension(180, 35));
        // Style button
        leaveRequestButton.setBackground(new Color(194, 215, 238));
        leaveRequestButton.setForeground(Color.BLACK);
        leaveRequestButton.setFont(new Font("Arial", Font.BOLD, 12));

        actionPanel.add(leaveRequestButton);

        return actionPanel;
    }

    private void showLeaveRequestDialog() {
        try {
            LeaveRequestDialog dialog = new LeaveRequestDialog(this, currentUser);
            dialog.setVisible(true);
            loadAttendanceData(); // Refresh attendance data after potential leave submission
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening leave request dialog: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLatestPayslip() {
        try {
            int selectedMonth = LocalDate.now().getMonthValue();
            int selectedYear = LocalDate.now().getYear();

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            Payroll payroll = payrollCalculator.calculatePayroll(currentUser.getEmployeeId(), periodStart, periodEnd);

            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, currentUser, payroll);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating payslip: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createAllowanceCard(String title, String amount, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel amountLabel = new JLabel(amount, JLabel.CENTER);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 18));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(amountLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Attendance Summary"));
        summaryPanel.add(totalDaysLabel);
        summaryPanel.add(Box.createHorizontalStrut(30));
        summaryPanel.add(averageHoursLabel);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAttendanceData());
        summaryPanel.add(Box.createHorizontalStrut(30));
        summaryPanel.add(refreshButton);

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with period selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Period:"));
        topPanel.add(monthComboBox);
        topPanel.add(yearComboBox);

        JButton calculateButton = new JButton("Calculate Payroll");
        calculateButton.addActionListener(e -> calculatePayroll());
        topPanel.add(calculateButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        monthComboBox.addActionListener(e -> loadPayrollData());
        yearComboBox.addActionListener(e -> loadPayrollData());
    }

    private void loadData() {
        loadPersonalInfo();
        loadAttendanceData();
        loadPayrollData();
    }

    private void loadPersonalInfo() {
        nameLabel.setText(currentUser.getFullName());
        positionLabel.setText(currentUser.getPosition());
        statusLabel.setText(currentUser.getStatus());
        salaryLabel.setText(String.format("₱%.2f", currentUser.getBasicSalary()));
        phoneLabel.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "N/A");
        addressLabel.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "N/A");
        sssLabel.setText(currentUser.getSssNumber() != null ? currentUser.getSssNumber() : "N/A");
        philhealthLabel.setText(currentUser.getPhilhealthNumber() != null ? currentUser.getPhilhealthNumber() : "N/A");
    }

    private void loadAttendanceData() {
        attendanceTableModel.setRowCount(0);

        try {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeId(currentUser.getEmployeeId());

            double totalHours = 0;
            int totalDays = attendanceList.size();

            for (Attendance att : attendanceList) {
                double workHours = att.getWorkHours();
                totalHours += workHours;

                String status = "Present";
                if (att.isLate() && att.hasUndertime()) {
                    status = "Late & Undertime";
                } else if (att.isLate()) {
                    status = "Late";
                } else if (att.hasUndertime()) {
                    status = "Undertime";
                } else if (att.isFullDay()) {
                    status = "Full Day";
                }

                Object[] row = {
                        att.getDate(),
                        att.getLogIn() != null ? att.getLogIn() : "N/A",
                        att.getLogOut() != null ? att.getLogOut() : "N/A",
                        String.format("%.2f hrs", workHours),
                        status
                };
                attendanceTableModel.addRow(row);
            }

            // Update summary labels
            totalDaysLabel.setText("Total Days: " + totalDays);
            if (totalDays > 0) {
                double avgHours = totalHours / totalDays;
                averageHoursLabel.setText(String.format("Average Hours: %.2f", avgHours));
            } else {
                averageHoursLabel.setText("Average Hours: 0.00");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadPayrollData() {
        payrollTableModel.setRowCount(0);

        try {
            // Get selected period
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            Payroll payroll = payrollCalculator.calculatePayroll(currentUser.getEmployeeId(), periodStart, periodEnd);

            Object[] row = {
                    periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    payroll.getDaysWorked(),
                    String.format("₱%.2f", payroll.getGrossPay()),
                    String.format("₱%.2f", payroll.getTotalDeductions()),
                    String.format("₱%.2f", payroll.getNetPay()),
                    "View Payslip"
            };
            payrollTableModel.addRow(row);

        } catch (Exception e) {
            Object[] row = {
                    "Error calculating payroll",
                    "Error",
                    "Error",
                    "Error",
                    "Error",
                    "View Error"
            };
            payrollTableModel.addRow(row);

            JOptionPane.showMessageDialog(this, "Error loading payroll data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void calculatePayroll() {
        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            Payroll payroll = payrollCalculator.calculatePayroll(currentUser.getEmployeeId(), periodStart, periodEnd);

            // Show detailed payroll dialog
            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, currentUser, payroll);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error calculating payroll: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create a dummy user for testing
            Employee testUser = new Employee();
            testUser.setEmployeeId(10001);
            testUser.setFirstName("Test");
            testUser.setLastName("Employee");
            testUser.setPosition("Software Developer");
            testUser.setStatus("Regular");
            testUser.setBasicSalary(50000.0);
            testUser.setRiceSubsidy(1500.0);
            testUser.setPhoneAllowance(1000.0);
            testUser.setClothingAllowance(800.0);

            new EmployeeDashboard(testUser).setVisible(true);
        });
    }
}