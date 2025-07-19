package ui;

import dao.LeaveRequestDAO;
import dao.EmployeeDAO;
import model.Employee;
import model.LeaveRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LeaveManagementDialog extends JDialog {
    private Employee currentUser;
    private LeaveRequestDAO leaveRequestDAO;
    private EmployeeDAO employeeDAO;

    private JTable leaveRequestTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JButton closeButton;

    public LeaveManagementDialog(Frame parent, Employee currentUser) {
        super(parent, "Leave Request Management", true);
        this.currentUser = currentUser;
        this.leaveRequestDAO = new LeaveRequestDAO();
        this.employeeDAO = new EmployeeDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadLeaveRequests();

        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Table setup
        String[] columns = {"ID", "Employee", "Type", "Start Date", "End Date", "Days", "Status", "Requested On"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveRequestTable = new JTable(tableModel);
        leaveRequestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaveRequestTable.setRowHeight(25);

        // Status filter
        String[] statuses = {"All", "Pending", "Approved", "Rejected"};
        statusFilter = new JComboBox<>(statuses);
        statusFilter.setSelectedItem("Pending");

        // Buttons
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");

        approveButton.setBackground(new Color(34, 139, 34));
        approveButton.setForeground(Color.WHITE);
        rejectButton.setBackground(new Color(220, 20, 60));
        rejectButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Leave Request Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status Filter:"));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(closeButton);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(leaveRequestTable), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        statusFilter.addActionListener(e -> loadLeaveRequests());
        refreshButton.addActionListener(e -> loadLeaveRequests());

        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLeaveStatus(LeaveRequest.STATUS_APPROVED);
            }
        });

        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLeaveStatus(LeaveRequest.STATUS_REJECTED);
            }
        });

        closeButton.addActionListener(e -> dispose());

        // Enable/disable buttons based on selection
        leaveRequestTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = leaveRequestTable.getSelectedRow() != -1;
            approveButton.setEnabled(hasSelection);
            rejectButton.setEnabled(hasSelection);
        });
    }

    private void loadLeaveRequests() {
        tableModel.setRowCount(0);

        try {
            String selectedStatus = (String) statusFilter.getSelectedItem();
            List<LeaveRequest> requests;

            if ("All".equals(selectedStatus)) {
                // Get all leave requests (you might want to implement this in DAO)
                requests = leaveRequestDAO.getLeaveRequestsByStatus("Pending");
                requests.addAll(leaveRequestDAO.getLeaveRequestsByStatus("Approved"));
                requests.addAll(leaveRequestDAO.getLeaveRequestsByStatus("Rejected"));
            } else {
                requests = leaveRequestDAO.getLeaveRequestsByStatus(selectedStatus);
            }

            for (LeaveRequest request : requests) {
                Employee emp = employeeDAO.getEmployeeById(request.getEmployeeId());
                String employeeName = emp != null ? emp.getFullName() : "Unknown";

                Object[] row = {
                        request.getLeaveId(),
                        employeeName,
                        request.getLeaveType(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getLeaveDays(),
                        request.getStatus(),
                        "N/A" // You might want to add created_at to the model
                };
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading leave requests: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateLeaveStatus(String newStatus) {
        int selectedRow = leaveRequestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int leaveId = (Integer) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);

            if (LeaveRequest.STATUS_APPROVED.equals(currentStatus) ||
                    LeaveRequest.STATUS_REJECTED.equals(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "This leave request has already been processed.",
                        "Already Processed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String actionText = LeaveRequest.STATUS_APPROVED.equals(newStatus) ? "approve" : "reject";
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to " + actionText + " this leave request?",
                    "Confirm Action", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean updated = leaveRequestDAO.updateLeaveStatus(leaveId, newStatus);

                if (updated) {
                    JOptionPane.showMessageDialog(this,
                            "Leave request " + actionText + "d successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadLeaveRequests(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update leave request status.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error updating leave request: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}