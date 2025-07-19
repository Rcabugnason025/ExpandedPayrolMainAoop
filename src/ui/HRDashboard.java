package ui;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import model.Employee;
import model.Attendance;
import model.Payroll;
import service.PayrollCalculator;
import ui.LoginForm;
import ui.EmployeeDetailsDialog;
import ui.PasswordChangeDialog;
import ui.LeaveManagementDialog;
import ui.AttendanceManagementDialog;
import ui.ReportsDialog;
import ui.PayrollDetailsDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Comparator;

public class HRDashboard extends JFrame {
    private Employee currentUser;
    private JTabbedPane tabbedPane;

    // Employee Management Tab
    private JTable employeeTable;
    private DefaultTableModel employeeTableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JComboBox<String> sortOrderComboBox;
    private JButton sortButton;
    private boolean isAscendingSort = true;

    // Payroll Tab
    private JTable payrollTable;
    private DefaultTableModel payrollTableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;

    // Attendance Tab
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JComboBox<Employee> employeeComboBox;

    // Services
    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;

    public HRDashboard(Employee user) {
        this.currentUser = user;

        try {
            // Initialize DAOs and services
            this.employeeDAO = new EmployeeDAO();
            this.attendanceDAO = new AttendanceDAO();
            this.payrollCalculator = new PayrollCalculator();

            // Initialize UI components
            initializeComponents();
            setupLayout();
            setupEventHandlers();

            // Load initial data
            loadData();

            System.out.println("✅ HR Dashboard initialized successfully for: " + user.getFullName());

        } catch (Exception e) {
            System.err.println("❌ HR Dashboard initialization failed: " + e.getMessage());
            e.printStackTrace();
            createErrorInterface(e);
        }

        setTitle("MotorPH Payroll System - HR Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void createErrorInterface(Exception error) {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(220, 53, 69));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("⚠️ HR Dashboard Initialization Failed", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        errorPanel.setBackground(Color.WHITE);

        String errorMessage = "<html><center>" +
                "<h2>🔧 HR System Error</h2>" +
                "<p><b>HR Dashboard initialization failed. Please contact system administrator.</b></p>" +
                "<br>" +
                "<p><b>HR User:</b> " + (currentUser != null ? currentUser.getFullName() : "Unknown") + "</p>" +
                "<p><b>Position:</b> " + (currentUser != null ? currentUser.getPosition() : "Unknown") + "</p>" +
                "<p><b>Error Type:</b> " + error.getClass().getSimpleName() + "</p>" +
                "<p><b>Error Message:</b> " + error.getMessage() + "</p>" +
                "</center></html>";

        JLabel messageLabel = new JLabel(errorMessage, JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton retryButton = new JButton("🔄 Retry Dashboard");
        JButton logoutButton = new JButton("🚪 Logout");
        JButton exitButton = new JButton("❌ Exit");

        retryButton.setBackground(new Color(40, 167, 69));
        retryButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(108, 117, 125));
        logoutButton.setForeground(Color.WHITE);
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);

        retryButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new HRDashboard(currentUser).setVisible(true));
        });

        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to exit the HR application?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION);
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

        // Initialize Employee Management components with Actions column
        String[] employeeColumns = {"ID", "Name", "Position", "Status", "Basic Salary", "Phone", "Actions"};
        employeeTableModel = new DefaultTableModel(employeeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Return Integer for ID column to enable proper numeric sorting
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        employeeTable = new JTable(employeeTableModel);

        // Initialize table sorter with custom comparator for ID column
        tableSorter = new TableRowSorter<>(employeeTableModel);
        employeeTable.setRowSorter(tableSorter);

        // Set custom comparator for ID column (column 0) to ensure proper numeric sorting
        tableSorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Integer.compare(o1, o2);
            }
        });

        searchField = new JTextField(20);

        // Enhanced: Add sort order combo box
        String[] sortOptions = {"ID: Ascending", "ID: Descending", "Name: A-Z", "Name: Z-A", "Position: A-Z"};
        sortOrderComboBox = new JComboBox<>(sortOptions);
        sortOrderComboBox.setSelectedIndex(0); // Default to ID ascending

        // Enhanced: Add dedicated sort button
        sortButton = new JButton("🔄 Sort");
        sortButton.setBackground(new Color(108, 117, 125));
        sortButton.setForeground(Color.BLACK);
        sortButton.setFont(new Font("Arial", Font.BOLD, 11));
        sortButton.setPreferredSize(new Dimension(70, 25));

        // Initialize other components (Payroll, Attendance)
        initializeOtherComponents();

        // Style tables
        setupTableStyling(employeeTable);
        setupTableStyling(payrollTable);
        setupTableStyling(attendanceTable);

        // Setup custom renderers for action buttons
        setupActionRenderers();
    }

    private void initializeOtherComponents() {
        // Payroll components
        String[] payrollColumns = {"Employee ID", "Name", "Period", "Days Worked", "Gross Pay", "Deductions", "Net Pay", "Actions"};
        payrollTableModel = new DefaultTableModel(payrollColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        payrollTable = new JTable(payrollTableModel);

        // Month/Year selectors
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        String[] years = {"2023", "2024", "2025"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");

        // Attendance components
        String[] attendanceColumns = {"Date", "Log In", "Log Out", "Work Hours", "Late", "Undertime", "Status"};
        attendanceTableModel = new DefaultTableModel(attendanceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceTableModel);
        employeeComboBox = new JComboBox<>();
    }

    private void setupActionRenderers() {
        // Employee table action column renderer
        employeeTable.getColumnModel().getColumn(6).setCellRenderer(new ActionButtonRenderer());

        // Payroll table action column renderer
        payrollTable.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
    }

    private void setupTableStyling(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(173, 180, 189));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setGridColor(Color.LIGHT_GRAY);

        // Enable sorting by clicking column headers
        table.setAutoCreateRowSorter(false); // We're using our custom sorter
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabs
        tabbedPane.addTab("Employee Management", createEmployeeManagementTab());
        tabbedPane.addTab("Payroll", createPayrollTab());
        tabbedPane.addTab("Attendance", createAttendanceTab());
        tabbedPane.addTab("Leave Management", createLeaveManagementTab());
        tabbedPane.addTab("Reports", createReportsTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Ready | Logged in as: " + currentUser.getFullName() + " (" + currentUser.getPosition() + ")");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("MotorPH Payroll System - HR Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
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

    private JPanel createEmployeeManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enhanced top panel with search, sort, and action buttons
        JPanel topPanel = new JPanel(new BorderLayout());

        // Enhanced search and sort panel
        JPanel searchSortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Search section
        searchSortPanel.add(new JLabel("🔍 Search:"));
        searchSortPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(108, 117, 125));
        searchButton.setForeground(Color.BLACK);
        searchButton.setFont(new Font("Arial", Font.BOLD, 11));
        searchButton.setPreferredSize(new Dimension(90, 25));
        searchButton.addActionListener(e -> searchEmployees());
        searchSortPanel.add(searchButton);

        // Separator
        searchSortPanel.add(new JSeparator(SwingConstants.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(2, 25);
            }
        });

        // Sort section
        searchSortPanel.add(new JLabel("📊 Sort by:"));
        searchSortPanel.add(sortOrderComboBox);
        searchSortPanel.add(sortButton);

        // Separator
        searchSortPanel.add(new JSeparator(SwingConstants.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(2, 25);
            }
        });

        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setBackground(new Color(194, 238, 204));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 11));
        refreshButton.setPreferredSize(new Dimension(120, 25));
        refreshButton.addActionListener(e -> {
            loadEmployeeData();
            showStatus("Employee data refreshed");
        });
        searchSortPanel.add(refreshButton);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton addEmployeeButton = new JButton("➕ Add Employee");
        addEmployeeButton.setBackground(new Color(194, 238, 204));
        addEmployeeButton.setForeground(Color.BLACK);
        addEmployeeButton.setFont(new Font("Arial", Font.BOLD, 12));
        addEmployeeButton.setPreferredSize(new Dimension(140, 30));
        addEmployeeButton.addActionListener(e -> showAddEmployeeDialog());

        JButton manageAttendanceButton = new JButton("📅 Manage Attendance");
        manageAttendanceButton.setBackground(new Color(107, 190, 206));
        manageAttendanceButton.setForeground(Color.BLACK);
        manageAttendanceButton.setFont(new Font("Arial", Font.BOLD, 12));
        manageAttendanceButton.setPreferredSize(new Dimension(160, 30));
        manageAttendanceButton.addActionListener(e -> showAttendanceManagement());

        actionPanel.add(addEmployeeButton);
        actionPanel.add(manageAttendanceButton);

        topPanel.add(searchSortPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // Status label for sorting feedback
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel sortStatusLabel = new JLabel("📋 Total employees will be shown here");
        sortStatusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        sortStatusLabel.setForeground(Color.GRAY);
        statusPanel.add(sortStatusLabel);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(statusPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        return panel;
    }

    // Enhanced sorting method
    private void applySorting() {
        String selectedSort = (String) sortOrderComboBox.getSelectedItem();

        try {
            switch (selectedSort) {
                case "ID: Ascending":
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
                    showStatus("Sorted by Employee ID: Ascending");
                    break;
                case "ID: Descending":
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
                    showStatus("Sorted by Employee ID: Descending");
                    break;
                case "Name: A-Z":
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
                    showStatus("Sorted by Name: A-Z");
                    break;
                case "Name: Z-A":
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(1, SortOrder.DESCENDING)));
                    showStatus("Sorted by Name: Z-A");
                    break;
                case "Position: A-Z":
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
                    showStatus("Sorted by Position: A-Z");
                    break;
                default:
                    tableSorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
                    showStatus("Default sorting applied");
                    break;
            }
        } catch (Exception e) {
            showStatus("Error applying sort: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showStatus(String message) {
        // Update status in a status bar or show a temporary message
        // For now, we'll print to console and could show in a status label
        System.out.println("Status: " + message);

        // You could add a status label to show this message to users
        // statusLabel.setText(message);
    }

    private JPanel createLeaveManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Leave Request Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Description
        JTextArea descArea = new JTextArea(
                "Manage employee leave requests including approvals, rejections, and tracking.\n\n" +
                        "Features:\n" +
                        "• View all pending leave requests\n" +
                        "• Approve or reject leave applications\n" +
                        "• Filter requests by status\n" +
                        "• Track leave balances\n" +
                        "• Generate leave reports"
        );
        descArea.setEditable(false);
        descArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        descArea.setBackground(new Color(248, 248, 255));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton openLeaveManagementButton = new JButton("🗂️ Open Leave Management");
        openLeaveManagementButton.setPreferredSize(new Dimension(220, 40));
        openLeaveManagementButton.setBackground(new Color(70, 130, 180));
        openLeaveManagementButton.setForeground(Color.WHITE);
        openLeaveManagementButton.setFont(new Font("Arial", Font.BOLD, 14));
        openLeaveManagementButton.addActionListener(e -> openLeaveManagement());

        buttonPanel.add(openLeaveManagementButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descArea, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Reports & Analytics", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Reports grid
        JPanel reportsGrid = new JPanel(new GridLayout(2, 2, 20, 20));

        // Monthly Payroll Report
        JPanel monthlyReportPanel = createReportCard(
                "📊 Monthly Payroll Report",
                "Generate comprehensive payroll reports for any month",
                new Color(70, 130, 180),
                e -> openReportsDialog("Monthly Payroll Report")
        );

        // Attendance Report
        JPanel attendanceReportPanel = createReportCard(
                "📅 Attendance Summary",
                "View detailed attendance statistics and trends",
                new Color(34, 139, 34),
                e -> openReportsDialog("Attendance Summary")
        );

        // Employee Directory
        JPanel employeeReportPanel = createReportCard(
                "👥 Employee Directory",
                "Export complete employee information and contacts",
                new Color(255, 140, 0),
                e -> openReportsDialog("Employee Directory")
        );

        // Government Contributions
        JPanel govContribPanel = createReportCard(
                "🏛️ Government Contributions",
                "Track SSS, PhilHealth, and Pag-IBIG contributions",
                new Color(138, 43, 226),
                e -> openReportsDialog("Government Contributions")
        );

        reportsGrid.add(monthlyReportPanel);
        reportsGrid.add(attendanceReportPanel);
        reportsGrid.add(employeeReportPanel);
        reportsGrid.add(govContribPanel);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(reportsGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReportCard(String title, String description, Color color, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", JLabel.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(Color.WHITE);

        JButton actionButton = new JButton("Generate");
        actionButton.setBackground(Color.WHITE);
        actionButton.setForeground(color);
        actionButton.setFont(new Font("Arial", Font.BOLD, 12));
        actionButton.addActionListener(action);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(actionButton, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with period selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Payroll Period:"));
        topPanel.add(monthComboBox);
        topPanel.add(yearComboBox);

        JButton generateButton = new JButton("📄 Generate Payroll");
        JButton calculateAllButton = new JButton("💰 Calculate All");
        JButton exportButton = new JButton("📤 Export");

        generateButton.addActionListener(e -> generateSelectedPayroll());
        calculateAllButton.addActionListener(e -> calculateAllPayrolls());
        exportButton.addActionListener(e -> exportPayrollData());

        topPanel.add(generateButton);
        topPanel.add(calculateAllButton);
        topPanel.add(exportButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(payrollTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAttendanceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with employee selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Employee:"));
        topPanel.add(employeeComboBox);

        JButton viewButton = new JButton("👁️ View Attendance");
        JButton refreshButton = new JButton("🔄 Refresh");

        viewButton.addActionListener(e -> loadAttendanceData());
        refreshButton.addActionListener(e -> {
            loadEmployeeComboBox();
            loadAttendanceData();
        });

        topPanel.add(viewButton);
        topPanel.add(refreshButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        // Search field enter key
        searchField.addActionListener(e -> searchEmployees());

        // Enhanced: Sort button and combo box handlers
        sortButton.addActionListener(e -> applySorting());
        sortOrderComboBox.addActionListener(e -> applySorting());

        // Employee combo box change
        employeeComboBox.addActionListener(e -> loadAttendanceData());

        // Month/Year combo box changes
        monthComboBox.addActionListener(e -> loadPayrollData());
        yearComboBox.addActionListener(e -> loadPayrollData());

        // Employee table mouse click handler for actions
        employeeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = employeeTable.rowAtPoint(e.getPoint());
                int col = employeeTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 6) { // Actions column
                    handleEmployeeAction(row, e);
                }
            }
        });

        // Payroll table mouse click handler
        payrollTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = payrollTable.rowAtPoint(e.getPoint());
                int col = payrollTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 7) { // Actions column
                    handlePayrollAction(row);
                }
            }
        });

        // Table header click for column sorting
        employeeTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = employeeTable.columnAtPoint(e.getPoint());
                if (column == 0) { // ID column
                    // Toggle between ascending and descending for ID column
                    isAscendingSort = !isAscendingSort;
                    String sortType = isAscendingSort ? "ID: Ascending" : "ID: Descending";
                    sortOrderComboBox.setSelectedItem(sortType);
                    applySorting();
                }
            }
        });
    }

    private void handleEmployeeAction(int row, MouseEvent e) {
        // Convert view row to model row due to sorting
        int modelRow = employeeTable.convertRowIndexToModel(row);
        int employeeId = (Integer) employeeTableModel.getValueAt(modelRow, 0);

        Employee employee = employeeDAO.getEmployeeById(employeeId);

        if (employee == null) return;

        // Show context menu with options
        JPopupMenu popup = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("👁️ View Details");
        viewItem.addActionListener(ev -> showEmployeeDetails(employee));

        JMenuItem editItem = new JMenuItem("✏️ Edit Employee");
        editItem.addActionListener(ev -> showEditEmployeeDialog(employee));

        JMenuItem passwordItem = new JMenuItem("🔑 Change Password");
        passwordItem.addActionListener(ev -> showPasswordChangeDialog(employee));

        JMenuItem deleteItem = new JMenuItem("🗑️ Delete Employee");
        deleteItem.addActionListener(ev -> deleteEmployee(employee));
        deleteItem.setForeground(Color.RED);

        popup.add(viewItem);
        popup.add(editItem);
        popup.addSeparator();
        popup.add(passwordItem);
        popup.addSeparator();
        popup.add(deleteItem);

        popup.show(employeeTable, e.getX(), e.getY());
    }

    private void handlePayrollAction(int row) {
        int employeeId = (Integer) payrollTableModel.getValueAt(row, 0);
        showPayrollDetails(employeeId);
    }

    // Action methods
    private void showAddEmployeeDialog() {
        EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, null, true);
        dialog.setVisible(true);
        loadEmployeeData(); // Refresh after potential addition
    }

    private void showEmployeeDetails(Employee employee) {
        EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, employee, false);
        dialog.setVisible(true);
    }

    private void showEditEmployeeDialog(Employee employee) {
        EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(this, employee, false);
        dialog.setVisible(true);
        loadEmployeeData(); // Refresh after potential edit
    }

    private void showPasswordChangeDialog(Employee employee) {
        PasswordChangeDialog dialog = new PasswordChangeDialog(this, employee);
        dialog.setVisible(true);
    }

    private void deleteEmployee(Employee employee) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete employee " + employee.getFullName() + "?\n" +
                        "This action cannot be undone and will also delete:\n" +
                        "• All attendance records\n" +
                        "• All payroll records\n" +
                        "• Login credentials\n" +
                        "• Leave requests",
                "Confirm Employee Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeDAO.deleteEmployee(employee.getEmployeeId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Employee " + employee.getFullName() + " deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadEmployeeData(); // Refresh table
                    loadEmployeeComboBox(); // Refresh combo box
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete employee. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting employee: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void showAttendanceManagement() {
        try {
            AttendanceManagementDialog dialog = new AttendanceManagementDialog(this, currentUser);
            dialog.setVisible(true);
            loadAttendanceData(); // Refresh after management
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening attendance management: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openLeaveManagement() {
        try {
            LeaveManagementDialog dialog = new LeaveManagementDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening leave management: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openReportsDialog(String reportType) {
        try {
            ReportsDialog dialog = new ReportsDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening reports: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Data loading methods
    private void loadData() {
        loadEmployeeData();
        loadEmployeeComboBox();
        loadPayrollData();
        loadAttendanceData();

        // Apply default sorting after loading data
        applySorting();
    }

    private void loadEmployeeData() {
        employeeTableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            for (Employee emp : employees) {
                Object[] row = {
                        emp.getEmployeeId(), // This will be treated as Integer for proper sorting
                        emp.getFullName(),
                        emp.getPosition(),
                        emp.getStatus(),
                        String.format("₱%.2f", emp.getBasicSalary()),
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "N/A",
                        "Actions" // This will be rendered as a button
                };
                employeeTableModel.addRow(row);
            }

            // Update status after loading
            showStatus("Loaded " + employees.size() + " employees");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            showStatus("Error loading employee data");
        }
    }

    private void loadEmployeeComboBox() {
        employeeComboBox.removeAllItems();

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            for (Employee emp : employees) {
                employeeComboBox.addItem(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPayrollData() {
        payrollTableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            // Get selected period
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            for (Employee emp : employees) {
                try {
                    Payroll payroll = payrollCalculator.calculatePayroll(emp.getEmployeeId(), periodStart, periodEnd);

                    Object[] row = {
                            emp.getEmployeeId(),
                            emp.getFullName(),
                            periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            payroll.getDaysWorked(),
                            String.format("₱%.2f", payroll.getGrossPay()),
                            String.format("₱%.2f", payroll.getTotalDeductions()),
                            String.format("₱%.2f", payroll.getNetPay()),
                            "View Payslip" // This will be rendered as a button
                    };
                    payrollTableModel.addRow(row);
                } catch (Exception e) {
                    // Add row with error status
                    Object[] row = {
                            emp.getEmployeeId(),
                            emp.getFullName(),
                            periodStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            "Error",
                            "Error",
                            "Error",
                            "Error",
                            "View Error"
                    };
                    payrollTableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading payroll data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadAttendanceData() {
        attendanceTableModel.setRowCount(0);

        Employee selectedEmployee = (Employee) employeeComboBox.getSelectedItem();
        if (selectedEmployee == null) return;

        try {
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeId(selectedEmployee.getEmployeeId());

            for (Attendance att : attendanceList) {
                Object[] row = {
                        att.getDate(),
                        att.getLogIn() != null ? att.getLogIn() : "N/A",
                        att.getLogOut() != null ? att.getLogOut() : "N/A",
                        String.format("%.2f hrs", att.getWorkHours()),
                        att.isLate() ? "Yes" : "No",
                        att.hasUndertime() ? "Yes" : "No",
                        att.isFullDay() ? "Full Day" : "Partial Day"
                };
                attendanceTableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading attendance data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void searchEmployees() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadEmployeeData();
            showStatus("Showing all employees");
            return;
        }

        employeeTableModel.setRowCount(0);

        try {
            List<Employee> employees = employeeDAO.searchEmployees(searchTerm);

            for (Employee emp : employees) {
                Object[] row = {
                        emp.getEmployeeId(),
                        emp.getFullName(),
                        emp.getPosition(),
                        emp.getStatus(),
                        String.format("₱%.2f", emp.getBasicSalary()),
                        emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "N/A",
                        "Actions"
                };
                employeeTableModel.addRow(row);
            }

            showStatus("Found " + employees.size() + " employees matching '" + searchTerm + "'");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching employees: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            showStatus("Error searching employees");
        }
    }

    private void generateSelectedPayroll() {
        int selectedRow = payrollTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to generate payroll for.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int employeeId = (Integer) payrollTableModel.getValueAt(selectedRow, 0);
        showPayrollDetails(employeeId);
    }

    private void calculateAllPayrolls() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will calculate payroll for all employees. Continue?",
                "Confirm Calculation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Show progress dialog
            JProgressBar progressBar = new JProgressBar(0, employeeTableModel.getRowCount());
            progressBar.setStringPainted(true);

            JDialog progressDialog = new JDialog(this, "Calculating Payrolls", true);
            progressDialog.add(new JLabel("Calculating payrolls for all employees..."), BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(400, 100);
            progressDialog.setLocationRelativeTo(this);

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    List<Employee> employees = employeeDAO.getAllEmployees();

                    for (int i = 0; i < employees.size(); i++) {
                        // Simulate calculation time
                        Thread.sleep(100);
                        publish(i + 1);
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    int latest = chunks.get(chunks.size() - 1);
                    progressBar.setValue(latest);
                    progressBar.setString(latest + " / " + employeeTableModel.getRowCount() + " completed");
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    loadPayrollData();
                    JOptionPane.showMessageDialog(HRDashboard.this,
                            "Payroll calculation completed for all employees!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    private void exportPayrollData() {
        try {
            ReportsDialog dialog = new ReportsDialog(this, currentUser);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening export dialog: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPayrollDetails(int employeeId) {
        try {
            Employee employee = employeeDAO.getEmployeeById(employeeId);

            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());

            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            Payroll payroll = payrollCalculator.calculatePayroll(employeeId, periodStart, periodEnd);

            // Create detailed payroll dialog
            PayrollDetailsDialog dialog = new PayrollDetailsDialog(this, employee, payroll);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating payroll: " + e.getMessage(),
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

     // Custom renderer for action buttons
    private class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            JButton button = new JButton("⚙️ Actions");
            button.setFont(new Font("Arial", Font.BOLD, 10));
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            if (isSelected) {
                button.setBackground(new Color(100, 160, 210));
            }

            return button;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create a dummy user for testing
            Employee testUser = new Employee();
            testUser.setEmployeeId(10001);
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setPosition("HR Manager");

            new HRDashboard(testUser).setVisible(true);
        });
    }
}