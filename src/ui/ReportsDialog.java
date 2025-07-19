package ui;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import dao.AttendanceDAO;
import model.Employee;
import model.Payroll;
import model.Attendance;
import service.PayrollCalculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReportsDialog extends JDialog {
    private Employee currentUser;
    private EmployeeDAO employeeDAO;
    private PayrollDAO payrollDAO;
    private AttendanceDAO attendanceDAO;
    private PayrollCalculator payrollCalculator;

    private JComboBox<String> reportTypeComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> yearComboBox;
    private JComboBox<String> formatComboBox;
    private JButton generateButton;
    private JButton closeButton;
    private JTextArea previewArea;

    // Employee Directory specific controls
    private JPanel directoryOptionsPanel;
    private JCheckBox includePersonalInfoCheckBox;
    private JCheckBox includeContactInfoCheckBox;
    private JCheckBox includeJobInfoCheckBox;
    private JCheckBox includeCompensationCheckBox;

    public ReportsDialog(Frame parent, Employee currentUser) {
        super(parent, "Generate Reports", true);
        this.currentUser = currentUser;
        this.employeeDAO = new EmployeeDAO();
        this.payrollDAO = new PayrollDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.payrollCalculator = new PayrollCalculator();

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        setSize(700, 650);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Report type combo box
        String[] reportTypes = {
                "Monthly Payroll Report",
                "Employee Directory Report",
                "Attendance Summary Report",
                "Government Contributions Report"
        };
        reportTypeComboBox = new JComboBox<>(reportTypes);

        // Month combo box
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        // Year combo box
        String[] years = {"2023", "2024", "2025"};
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem("2024");

        // Format combo box
        String[] formats = {"Preview Only", "HTML Export", "CSV Export"};
        formatComboBox = new JComboBox<>(formats);
        formatComboBox.setSelectedItem("Preview Only");

        // Employee Directory Options (initially hidden)
        includePersonalInfoCheckBox = new JCheckBox("Include Personal Information (Birthday, Age)", false);
        includeContactInfoCheckBox = new JCheckBox("Include Contact Information (Phone, Address)", true);
        includeJobInfoCheckBox = new JCheckBox("Include Job Information (Position, Department, Status)", true);
        includeCompensationCheckBox = new JCheckBox("Include Compensation Information (Salary, Allowances)", false);

        directoryOptionsPanel = new JPanel();
        directoryOptionsPanel.setLayout(new BoxLayout(directoryOptionsPanel, BoxLayout.Y_AXIS));
        directoryOptionsPanel.setBorder(BorderFactory.createTitledBorder("Directory Options"));
        directoryOptionsPanel.add(includePersonalInfoCheckBox);
        directoryOptionsPanel.add(includeContactInfoCheckBox);
        directoryOptionsPanel.add(includeJobInfoCheckBox);
        directoryOptionsPanel.add(includeCompensationCheckBox);
        directoryOptionsPanel.setVisible(false);

        // Buttons
        generateButton = new JButton("Generate Report");
        closeButton = new JButton("Close");

        generateButton.setBackground(new Color(70, 130, 180));
        generateButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Preview area
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        previewArea.setBackground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Report Generator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Options panel
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Report type
        gbc.gridx = 0; gbc.gridy = 0;
        optionsPanel.add(new JLabel("Report Type:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        optionsPanel.add(reportTypeComboBox, gbc);
        gbc.gridwidth = 1;

        // Period (for applicable reports)
        gbc.gridx = 0; gbc.gridy = 1;
        optionsPanel.add(new JLabel("Month:"), gbc);
        gbc.gridx = 1;
        optionsPanel.add(monthComboBox, gbc);

        gbc.gridx = 2;
        optionsPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 3;
        optionsPanel.add(yearComboBox, gbc);

        // Format
        gbc.gridx = 0; gbc.gridy = 2;
        optionsPanel.add(new JLabel("Format:"), gbc);
        gbc.gridx = 1;
        optionsPanel.add(formatComboBox, gbc);

        // Directory options panel
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        optionsPanel.add(directoryOptionsPanel, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(generateButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        optionsPanel.add(buttonPanel, gbc);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(optionsPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        reportTypeComboBox.addActionListener(e -> {
            String selectedType = (String) reportTypeComboBox.getSelectedItem();
            boolean isEmployeeDirectory = "Employee Directory Report".equals(selectedType);

            // Show/hide directory options
            directoryOptionsPanel.setVisible(isEmployeeDirectory);

            // Show/hide month/year selectors for reports that don't need them
            monthComboBox.setEnabled(!isEmployeeDirectory);
            yearComboBox.setEnabled(!isEmployeeDirectory);

            revalidate();
            repaint();
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void generateReport() {
        try {
            generateButton.setEnabled(false);
            generateButton.setText("Generating...");

            String reportType = (String) reportTypeComboBox.getSelectedItem();
            String format = (String) formatComboBox.getSelectedItem();

            switch (reportType) {
                case "Monthly Payroll Report":
                    generatePayrollReport(format);
                    break;
                case "Employee Directory Report":
                    generateEmployeeDirectoryReport(format);
                    break;
                case "Government Contributions Report":
                    generateGovernmentContributionsReport(format);
                    break;
                case "Attendance Summary Report":
                    generateAttendanceReport(format);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Selected report type not yet implemented.",
                            "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
                    return;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            generateButton.setEnabled(true);
            generateButton.setText("Generate Report");
        }
    }

    private void generatePayrollReport(String format) {
        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());
            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            List<Employee> employees = employeeDAO.getAllEmployees();
            StringBuilder report = new StringBuilder();

            // Build payroll report
            report.append("MONTHLY PAYROLL REPORT\n");
            report.append("Period: ").append(periodStart.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
            report.append("Generated by: ").append(currentUser.getFullName()).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            report.append(String.format("%-6s %-25s %-12s %-12s %-12s %-12s%n",
                    "ID", "Name", "Basic Pay", "Allowances", "Deductions", "Net Pay"));
            report.append("-".repeat(80)).append("\n");

            double totalGross = 0, totalDeductions = 0, totalNet = 0;

            for (Employee emp : employees) {
                try {
                    Payroll payroll = payrollCalculator.calculatePayroll(emp.getEmployeeId(), periodStart, periodEnd);

                    double grossPay = payroll.getGrossPay();
                    double deductions = payroll.getTotalDeductions();
                    double netPay = payroll.getNetPay();

                    totalGross += grossPay;
                    totalDeductions += deductions;
                    totalNet += netPay;

                    String name = emp.getFullName();
                    if (name.length() > 25) name = name.substring(0, 22) + "...";

                    report.append(String.format("%-6d %-25s ₱%-11.2f ₱%-11.2f ₱%-11.2f ₱%-11.2f%n",
                            emp.getEmployeeId(), name, payroll.getGrossEarnings(),
                            payroll.getRiceSubsidy() + payroll.getPhoneAllowance() + payroll.getClothingAllowance(),
                            deductions, netPay));
                } catch (Exception e) {
                    report.append(String.format("%-6d %-25s %-12s %-12s %-12s %-12s%n",
                            emp.getEmployeeId(), emp.getFullName(), "Error", "Error", "Error", "Error"));
                }
            }

            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-31s ₱%-11.2f ₱%-11.2f ₱%-11.2f ₱%-11.2f%n",
                    "TOTALS:", totalGross - totalDeductions, 0.0, totalDeductions, totalNet));

            if ("Preview Only".equals(format)) {
                previewArea.setText(report.toString());
            } else {
                exportTextReport(report.toString(), "Payroll_Report_" + selectedMonth + "_" + selectedYear, format);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating payroll report: " + e.getMessage(), e);
        }
    }

    private void generateEmployeeDirectoryReport(String format) {
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            // Sort employees by last name, then first name
            employees.sort((e1, e2) -> {
                int lastNameCompare = e1.getLastName().compareToIgnoreCase(e2.getLastName());
                if (lastNameCompare != 0) return lastNameCompare;
                return e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
            });

            if ("Preview Only".equals(format)) {
                showEmployeeDirectoryPreview(employees);
            } else {
                exportEmployeeDirectory(employees, format);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating employee directory: " + e.getMessage(), e);
        }
    }

    private void generateGovernmentContributionsReport(String format) {
        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());
            LocalDate periodStart = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

            List<Employee> employees = employeeDAO.getAllEmployees();
            StringBuilder report = new StringBuilder();

            report.append("GOVERNMENT CONTRIBUTIONS REPORT\n");
            report.append("Period: ").append(periodStart.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            report.append(String.format("%-6s %-20s %-10s %-12s %-10s %-10s%n",
                    "ID", "Name", "SSS", "PhilHealth", "Pag-IBIG", "Tax"));
            report.append("-".repeat(80)).append("\n");

            double totalSSS = 0, totalPhilHealth = 0, totalPagIBIG = 0, totalTax = 0;

            for (Employee emp : employees) {
                try {
                    Payroll payroll = payrollCalculator.calculatePayroll(emp.getEmployeeId(), periodStart, periodEnd);

                    totalSSS += payroll.getSss();
                    totalPhilHealth += payroll.getPhilhealth();
                    totalPagIBIG += payroll.getPagibig();
                    totalTax += payroll.getTax();

                    String name = emp.getFullName();
                    if (name.length() > 20) name = name.substring(0, 17) + "...";

                    report.append(String.format("%-6d %-20s ₱%-9.2f ₱%-11.2f ₱%-9.2f ₱%-9.2f%n",
                            emp.getEmployeeId(), name, payroll.getSss(), payroll.getPhilhealth(),
                            payroll.getPagibig(), payroll.getTax()));
                } catch (Exception e) {
                    report.append(String.format("%-6d %-20s %-10s %-12s %-10s %-10s%n",
                            emp.getEmployeeId(), emp.getFullName(), "Error", "Error", "Error", "Error"));
                }
            }

            report.append("-".repeat(80)).append("\n");
            report.append(String.format("%-26s ₱%-9.2f ₱%-11.2f ₱%-9.2f ₱%-9.2f%n",
                    "TOTALS:", totalSSS, totalPhilHealth, totalPagIBIG, totalTax));

            if ("Preview Only".equals(format)) {
                previewArea.setText(report.toString());
            } else {
                exportTextReport(report.toString(), "Government_Contributions_" + selectedMonth + "_" + selectedYear, format);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating government contributions report: " + e.getMessage(), e);
        }
    }

    private void generateAttendanceReport(String format) {
        try {
            int selectedMonth = monthComboBox.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) yearComboBox.getSelectedItem());
            LocalDate startDate = LocalDate.of(selectedYear, selectedMonth, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            List<Employee> employees = employeeDAO.getAllEmployees();
            StringBuilder report = new StringBuilder();

            report.append("ATTENDANCE SUMMARY REPORT\n");
            report.append("Period: ").append(startDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))
                    .append(" - ").append(endDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            report.append(String.format("%-6s %-25s %-12s %-12s %-12s%n",
                    "ID", "Name", "Days Present", "Total Hours", "Avg Hours/Day"));
            report.append("-".repeat(80)).append("\n");

            int totalEmployees = 0;
            int totalDaysPresent = 0;
            double totalHours = 0;

            for (Employee emp : employees) {
                try {
                    List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                            emp.getEmployeeId(), startDate, endDate);

                    int daysPresent = attendanceList.size();
                    double empTotalHours = 0;

                    for (Attendance att : attendanceList) {
                        empTotalHours += att.getWorkHours();
                    }

                    double avgHours = daysPresent > 0 ? empTotalHours / daysPresent : 0;

                    totalEmployees++;
                    totalDaysPresent += daysPresent;
                    totalHours += empTotalHours;

                    String name = emp.getFullName();
                    if (name.length() > 25) name = name.substring(0, 22) + "...";

                    report.append(String.format("%-6d %-25s %-12d %-12.2f %-12.2f%n",
                            emp.getEmployeeId(), name, daysPresent, empTotalHours, avgHours));
                } catch (Exception e) {
                    report.append(String.format("%-6d %-25s %-12s %-12s %-12s%n",
                            emp.getEmployeeId(), emp.getFullName(), "Error", "Error", "Error"));
                }
            }

            report.append("-".repeat(80)).append("\n");
            report.append(String.format("TOTALS: %d employees, %d total days present, %.2f total hours%n",
                    totalEmployees, totalDaysPresent, totalHours));

            if ("Preview Only".equals(format)) {
                previewArea.setText(report.toString());
            } else {
                exportTextReport(report.toString(), "Attendance_Summary_" + selectedMonth + "_" + selectedYear, format);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error generating attendance report: " + e.getMessage(), e);
        }
    }

    private void showEmployeeDirectoryPreview(List<Employee> employees) {
        StringBuilder preview = new StringBuilder();

        preview.append("EMPLOYEE DIRECTORY REPORT\n");
        preview.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
        preview.append("Generated by: ").append(currentUser.getFullName()).append("\n");
        preview.append("Total Employees: ").append(employees.size()).append("\n");
        preview.append("=".repeat(80)).append("\n\n");

        // Header
        StringBuilder header = new StringBuilder();
        header.append(String.format("%-6s %-25s", "ID", "Name"));

        if (includeContactInfoCheckBox.isSelected()) {
            header.append(String.format(" %-15s", "Phone"));
        }
        if (includeJobInfoCheckBox.isSelected()) {
            header.append(String.format(" %-20s %-12s", "Position", "Status"));
        }
        if (includePersonalInfoCheckBox.isSelected()) {
            header.append(String.format(" %-4s", "Age"));
        }
        if (includeCompensationCheckBox.isSelected()) {
            header.append(String.format(" %-12s", "Salary"));
        }

        preview.append(header.toString()).append("\n");
        preview.append("-".repeat(80)).append("\n");

        // Employee data (show first 20)
        int count = 0;
        for (Employee emp : employees) {
            if (count >= 20) {
                preview.append("... and ").append(employees.size() - 20).append(" more employees\n");
                break;
            }

            StringBuilder row = new StringBuilder();
            String fullName = emp.getFullName();
            if (fullName.length() > 25) fullName = fullName.substring(0, 22) + "...";

            row.append(String.format("%-6d %-25s", emp.getEmployeeId(), fullName));

            if (includeContactInfoCheckBox.isSelected()) {
                String phone = emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "N/A";
                if (phone.length() > 15) phone = phone.substring(0, 12) + "...";
                row.append(String.format(" %-15s", phone));
            }

            if (includeJobInfoCheckBox.isSelected()) {
                String position = emp.getPosition() != null ? emp.getPosition() : "N/A";
                if (position.length() > 20) position = position.substring(0, 17) + "...";
                row.append(String.format(" %-20s %-12s", position, emp.getStatus()));
            }

            if (includePersonalInfoCheckBox.isSelected()) {
                row.append(String.format(" %-4d", emp.getAge()));
            }

            if (includeCompensationCheckBox.isSelected()) {
                row.append(String.format(" ₱%-11.2f", emp.getBasicSalary()));
            }

            preview.append(row.toString()).append("\n");
            count++;
        }

        // Summary
        preview.append("\n").append("-".repeat(80)).append("\n");
        preview.append("SUMMARY BY STATUS:\n");

        long regularCount = employees.stream().filter(e -> "Regular".equals(e.getStatus())).count();
        long probationaryCount = employees.stream().filter(e -> "Probationary".equals(e.getStatus())).count();

        preview.append(String.format("Regular Employees: %d%n", regularCount));
        preview.append(String.format("Probationary Employees: %d%n", probationaryCount));

        if (includeCompensationCheckBox.isSelected()) {
            double avgSalary = employees.stream().mapToDouble(Employee::getBasicSalary).average().orElse(0.0);
            preview.append(String.format("Average Salary: ₱%.2f%n", avgSalary));
        }

        previewArea.setText(preview.toString());
    }

    private void exportEmployeeDirectory(List<Employee> employees, String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Employee Directory");

        String extension = format.toLowerCase().replace(" export", "");
        String fileName = "Employee_Directory_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + "." + extension;
        fileChooser.setSelectedFile(new File(fileName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try {
                if ("csv".equals(extension)) {
                    exportEmployeeDirectoryToCSV(employees, fileToSave.getAbsolutePath());
                } else if ("html".equals(extension)) {
                    exportEmployeeDirectoryToHTML(employees, fileToSave.getAbsolutePath());
                }

                JOptionPane.showMessageDialog(this,
                        "Employee directory exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting employee directory: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void exportEmployeeDirectoryToCSV(List<Employee> employees, String filePath) throws Exception {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)))) {
            // Build CSV header
            StringBuilder header = new StringBuilder();
            header.append("Employee ID,Last Name,First Name,Full Name");

            if (includeContactInfoCheckBox.isSelected()) {
                header.append(",Phone Number,Address");
            }
            if (includeJobInfoCheckBox.isSelected()) {
                header.append(",Position,Status,Supervisor,SSS Number,PhilHealth Number,TIN Number,Pag-IBIG Number");
            }
            if (includePersonalInfoCheckBox.isSelected()) {
                header.append(",Birthday,Age");
            }
            if (includeCompensationCheckBox.isSelected()) {
                header.append(",Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Total Compensation");
            }

            writer.println(header.toString());

            // Write employee data
            for (Employee emp : employees) {
                StringBuilder row = new StringBuilder();
                row.append(emp.getEmployeeId()).append(",");
                row.append(csvEscape(emp.getLastName())).append(",");
                row.append(csvEscape(emp.getFirstName())).append(",");
                row.append(csvEscape(emp.getFullName()));

                if (includeContactInfoCheckBox.isSelected()) {
                    row.append(",").append(csvEscape(emp.getPhoneNumber()));
                    row.append(",").append(csvEscape(emp.getAddress()));
                }

                if (includeJobInfoCheckBox.isSelected()) {
                    row.append(",").append(csvEscape(emp.getPosition()));
                    row.append(",").append(csvEscape(emp.getStatus()));
                    row.append(",").append(csvEscape(emp.getImmediateSupervisor()));
                    row.append(",").append(csvEscape(emp.getSssNumber()));
                    row.append(",").append(csvEscape(emp.getPhilhealthNumber()));
                    row.append(",").append(csvEscape(emp.getTinNumber()));
                    row.append(",").append(csvEscape(emp.getPagibigNumber()));
                }

                if (includePersonalInfoCheckBox.isSelected()) {
                    row.append(",").append(emp.getBirthday() != null ? emp.getBirthday().toString() : "");
                    row.append(",").append(emp.getAge());
                }

                if (includeCompensationCheckBox.isSelected()) {
                    row.append(",").append(String.format("%.2f", emp.getBasicSalary()));
                    row.append(",").append(String.format("%.2f", emp.getRiceSubsidy()));
                    row.append(",").append(String.format("%.2f", emp.getPhoneAllowance()));
                    row.append(",").append(String.format("%.2f", emp.getClothingAllowance()));
                    row.append(",").append(String.format("%.2f", emp.getBasicSalary() + emp.getTotalAllowances()));
                }

                writer.println(row.toString());
            }

            // Summary
            writer.println();
            writer.println("SUMMARY");
            writer.println("Total Employees," + employees.size());

            long regularCount = employees.stream().filter(e -> "Regular".equals(e.getStatus())).count();
            long probationaryCount = employees.stream().filter(e -> "Probationary".equals(e.getStatus())).count();

            writer.println("Regular Employees," + regularCount);
            writer.println("Probationary Employees," + probationaryCount);

            if (includeCompensationCheckBox.isSelected()) {
                double avgSalary = employees.stream().mapToDouble(Employee::getBasicSalary).average().orElse(0.0);
                writer.println("Average Salary," + String.format("%.2f", avgSalary));
            }
        }
    }

    private void exportEmployeeDirectoryToHTML(List<Employee> employees, String filePath) throws Exception {
        StringBuilder html = new StringBuilder();

        // HTML structure
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>Employee Directory</title>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px; }");
        html.append(".header h1 { margin: 0; font-size: 2.5em; }");
        html.append(".summary { background: white; padding: 25px; border-radius: 10px; margin-bottom: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append("table { width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append("th { background: #495057; color: white; padding: 15px 12px; text-align: left; }");
        html.append("td { padding: 12px; border-bottom: 1px solid #dee2e6; }");
        html.append("tr:nth-child(even) { background-color: #f8f9fa; }");
        html.append("tr:hover { background-color: #e9ecef; }");
        html.append(".footer { margin-top: 30px; text-align: center; color: #6c757d; }");
        html.append("</style></head><body>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>Employee Directory</h1>");
        html.append("<p>Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("</p>");
        html.append("<p>Generated by: ").append(currentUser.getFullName()).append("</p>");
        html.append("</div>");

        // Summary
        html.append("<div class='summary'>");
        html.append("<h2>Summary</h2>");
        html.append("<p><strong>Total Employees:</strong> ").append(employees.size()).append("</p>");

        long regularCount = employees.stream().filter(e -> "Regular".equals(e.getStatus())).count();
        long probationaryCount = employees.stream().filter(e -> "Probationary".equals(e.getStatus())).count();

        html.append("<p><strong>Regular Employees:</strong> ").append(regularCount).append("</p>");
        html.append("<p><strong>Probationary Employees:</strong> ").append(probationaryCount).append("</p>");

        if (includeCompensationCheckBox.isSelected()) {
            double avgSalary = employees.stream().mapToDouble(Employee::getBasicSalary).average().orElse(0.0);
            html.append("<p><strong>Average Salary:</strong> ₱").append(String.format("%.2f", avgSalary)).append("</p>");
        }
        html.append("</div>");

        // Table
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Employee ID</th>");
        html.append("<th>Full Name</th>");

        if (includeContactInfoCheckBox.isSelected()) {
            html.append("<th>Phone</th>");
            html.append("<th>Address</th>");
        }

        if (includeJobInfoCheckBox.isSelected()) {
            html.append("<th>Position</th>");
            html.append("<th>Status</th>");
            html.append("<th>Supervisor</th>");
        }

        if (includePersonalInfoCheckBox.isSelected()) {
            html.append("<th>Birthday</th>");
            html.append("<th>Age</th>");
        }

        if (includeCompensationCheckBox.isSelected()) {
            html.append("<th>Basic Salary</th>");
            html.append("<th>Allowances</th>");
            html.append("<th>Total Compensation</th>");
        }

        html.append("</tr></thead><tbody>");

        // Employee rows
        for (Employee emp : employees) {
            html.append("<tr>");
            html.append("<td>").append(emp.getEmployeeId()).append("</td>");
            html.append("<td>").append(htmlEscape(emp.getFullName())).append("</td>");

            if (includeContactInfoCheckBox.isSelected()) {
                html.append("<td>").append(htmlEscape(emp.getPhoneNumber())).append("</td>");
                html.append("<td>").append(htmlEscape(emp.getAddress())).append("</td>");
            }

            if (includeJobInfoCheckBox.isSelected()) {
                html.append("<td>").append(htmlEscape(emp.getPosition())).append("</td>");
                html.append("<td>").append(htmlEscape(emp.getStatus())).append("</td>");
                html.append("<td>").append(htmlEscape(emp.getImmediateSupervisor())).append("</td>");
            }

            if (includePersonalInfoCheckBox.isSelected()) {
                html.append("<td>").append(emp.getBirthday() != null ? emp.getBirthday().toString() : "N/A").append("</td>");
                html.append("<td>").append(emp.getAge()).append("</td>");
            }

            if (includeCompensationCheckBox.isSelected()) {
                html.append("<td>₱").append(String.format("%.2f", emp.getBasicSalary())).append("</td>");
                html.append("<td>₱").append(String.format("%.2f", emp.getTotalAllowances())).append("</td>");
                html.append("<td>₱").append(String.format("%.2f", emp.getBasicSalary() + emp.getTotalAllowances())).append("</td>");
            }

            html.append("</tr>");
        }

        html.append("</tbody></table>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Generated by MotorPH Payroll System</p>");
        html.append("</div>");

        html.append("</body></html>");

        Files.write(Paths.get(filePath), html.toString().getBytes());
    }

    private void exportTextReport(String reportContent, String baseFileName, String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");

        String extension = format.toLowerCase().replace(" export", "");
        if (extension.equals("preview only")) extension = "txt";

        String fileName = baseFileName + "." + extension;
        fileChooser.setSelectedFile(new File(fileName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            try {
                if ("html".equals(extension)) {
                    // Convert text report to HTML
                    String htmlContent = convertTextToHTML(reportContent, baseFileName);
                    Files.write(fileToSave.toPath(), htmlContent.getBytes());
                } else {
                    Files.write(fileToSave.toPath(), reportContent.getBytes());
                }

                JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting report: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String convertTextToHTML(String textContent, String title) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>").append(title).append("</title>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Courier New', monospace; margin: 20px; background-color: #f5f5f5; }");
        html.append(".content { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append("pre { font-family: 'Courier New', monospace; white-space: pre-wrap; }");
        html.append("</style></head><body>");
        html.append("<div class='content'>");
        html.append("<pre>").append(htmlEscape(textContent)).append("</pre>");
        html.append("</div></body></html>");
        return html.toString();
    }

    // Helper methods
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String htmlEscape(String value) {
        if (value == null) return "N/A";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}