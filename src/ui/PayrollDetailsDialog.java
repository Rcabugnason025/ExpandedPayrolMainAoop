package ui;

import model.Employee;
import model.Payroll;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;
import java.net.URL;

public class PayrollDetailsDialog extends JDialog {
    private Employee employee;
    private Payroll payroll;
    private JTextArea payslipTextArea;

    public PayrollDetailsDialog(Frame parent, Employee employee, Payroll payroll) {
        super(parent, "Payroll Details - " + employee.getFullName(), true);
        this.employee = employee;
        this.payroll = payroll;

        initializeComponents();
        setupLayout();
        generatePayslip();

        setSize(650, 750);
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(600, 650));
    }

    private void initializeComponents() {
        payslipTextArea = new JTextArea();
        payslipTextArea.setEditable(false);
        payslipTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        payslipTextArea.setBackground(Color.WHITE);
        payslipTextArea.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    }

    private void loadLogo() {
        // Method removed - no longer using logo
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Company header
        JPanel companyPanel = createCompanyHeader();

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(companyPanel, BorderLayout.NORTH);

        // Payslip text
        JScrollPane scrollPane = new JScrollPane(payslipTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        JButton printButton = new JButton("Print");
        JButton saveButton = new JButton("Save as Text");  // Updated button text
        JButton closeButton = new JButton("Close");

        printButton.setPreferredSize(new Dimension(120, 35));
        saveButton.setPreferredSize(new Dimension(120, 35));
        closeButton.setPreferredSize(new Dimension(100, 35));

        // Style buttons
        printButton.setBackground(new Color(70, 130, 180));
        printButton.setFont(new Font("Arial", Font.BOLD, 12));

        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));

        closeButton.setBackground(new Color(220, 220, 220));
        closeButton.setFont(new Font("Arial", Font.PLAIN, 12));

        printButton.addActionListener(e -> printPayslip());
        saveButton.addActionListener(e -> savePayslip());  // Updated method name
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Employee Payslip - MotorPH Payroll System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.WEST);

        return headerPanel;
    }

    private JPanel createCompanyHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Company Name
        JLabel companyName = new JLabel("MotorPH", JLabel.CENTER);
        companyName.setFont(new Font("Arial", Font.BOLD, 24));
        companyName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Address
        JLabel address = new JLabel("7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City", JLabel.CENTER);
        address.setFont(new Font("Arial", Font.PLAIN, 12));
        address.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Phone
        JLabel phone = new JLabel("Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073", JLabel.CENTER);
        phone.setFont(new Font("Arial", Font.PLAIN, 12));
        phone.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        JLabel email = new JLabel("Email: corporate@motorph.com", JLabel.CENTER);
        email.setFont(new Font("Arial", Font.PLAIN, 12));
        email.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Payslip Title
        JLabel payslipTitle = new JLabel("EMPLOYEE PAYSLIP", JLabel.CENTER);
        payslipTitle.setFont(new Font("Arial", Font.BOLD, 18));
        payslipTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(companyName);
        panel.add(Box.createVerticalStrut(5));
        panel.add(address);
        panel.add(phone);
        panel.add(email);
        panel.add(Box.createVerticalStrut(15));
        panel.add(payslipTitle);

        return panel;
    }

    private void generatePayslip() {
        StringBuilder sb = new StringBuilder();

        // Generate payslip number and dates
        String payslipNo = String.format("%d-%s",
                employee.getEmployeeId(),
                payroll.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String periodStart = payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String periodEnd = payroll.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        // Company Header (for printing/PDF)
        sb.append("\n");
        sb.append("                                MotorPH\n");
        sb.append("           7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City\n");
        sb.append("           Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073\n");
        sb.append("                         Email: corporate@motorph.com\n\n");
        sb.append("                            EMPLOYEE PAYSLIP\n");
        sb.append("\n");
        sb.append("================================================\n\n");

        // Payslip Details
        sb.append("PAYSLIP NO: ").append(payslipNo).append("\n\n");

        // Employee Information Section
        sb.append("EMPLOYEE INFORMATION:\n");
        sb.append("================================================\n");
        sb.append("Employee ID         : ").append(employee.getEmployeeId()).append("\n");
        sb.append("Name                : ").append(employee.getLastName()).append(", ").append(employee.getFirstName()).append("\n");
        sb.append("Position            : ").append(employee.getPosition()).append("\n");
        sb.append("Department          : ").append(employee.getPosition()).append("\n");
        sb.append("Employment Status   : ").append(employee.getStatus()).append("\n\n");

        // Pay Period Information
        sb.append("PAY PERIOD INFORMATION:\n");
        sb.append("================================================\n");
        sb.append("Pay Period          : ").append(periodStart).append(" to ").append(periodEnd).append("\n");
        sb.append("Days Worked         : ").append(payroll.getDaysWorked()).append("\n");
        sb.append("Monthly Rate        : ").append(formatCurrency(payroll.getMonthlyRate())).append("\n");
        sb.append("Daily Rate          : ").append(formatCurrency(payroll.getDailyRate())).append("\n\n");

        // Earnings Section
        sb.append("EARNINGS:\n");
        sb.append("================================================\n");
        sb.append("Basic Pay           : ").append(formatCurrency(payroll.getGrossEarnings())).append("\n");
        sb.append("Overtime Pay        : ").append(formatCurrency(payroll.getOvertimePay())).append("\n");
        sb.append("Rice Subsidy        : ").append(formatCurrency(payroll.getRiceSubsidy())).append("\n");
        sb.append("Phone Allowance     : ").append(formatCurrency(payroll.getPhoneAllowance())).append("\n");
        sb.append("Clothing Allowance  : ").append(formatCurrency(payroll.getClothingAllowance())).append("\n");
        sb.append("                      ").append("____________").append("\n");
        sb.append("GROSS PAY           : ").append(formatCurrency(payroll.getGrossPay())).append("\n\n");

        // Deductions Section
        sb.append("DEDUCTIONS:\n");
        sb.append("================================================\n");
        sb.append("Social Security System : ").append(formatCurrency(payroll.getSss())).append("\n");
        sb.append("Philhealth            : ").append(formatCurrency(payroll.getPhilhealth())).append("\n");
        sb.append("Pag-Ibig               : ").append(formatCurrency(payroll.getPagibig())).append("\n");
        sb.append("Withholding Tax        : ").append(formatCurrency(payroll.getTax())).append("\n");
        if (payroll.getLateDeduction() > 0) {
            sb.append("Late Deduction         : ").append(formatCurrency(payroll.getLateDeduction())).append("\n");
        }
        if (payroll.getUndertimeDeduction() > 0) {
            sb.append("Undertime Deduction    : ").append(formatCurrency(payroll.getUndertimeDeduction())).append("\n");
        }
        if (payroll.getUnpaidLeaveDeduction() > 0) {
            sb.append("Unpaid Leave           : ").append(formatCurrency(payroll.getUnpaidLeaveDeduction())).append("\n");
        }
        sb.append("                         ").append("____________").append("\n");
        sb.append("TOTAL DEDUCTIONS       : ").append(formatCurrency(payroll.getTotalDeductions())).append("\n\n");

        // Summary Section
        sb.append("SUMMARY:\n");
        sb.append("================================================\n");
        sb.append("Gross Pay              : ").append(formatCurrency(payroll.getGrossPay())).append("\n");
        sb.append("Total Deductions       : ").append(formatCurrency(payroll.getTotalDeductions())).append("\n");
        sb.append("                         ").append("____________").append("\n");
        sb.append("TAKE HOME PAY          : ").append(formatCurrency(payroll.getNetPay())).append("\n\n");

        // Footer
        sb.append("================================================\n");
        sb.append("This payslip is computer-generated and does not require signature.\n");
        sb.append("Please keep this document for your records.\n\n");
        sb.append("Generated on: ").append(java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss"))).append("\n");

        payslipTextArea.setText(sb.toString());
        payslipTextArea.setCaretPosition(0); // Scroll to top
    }

    private String formatCurrency(double amount) {
        return String.format("₱%,.2f", amount);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private void printPayslip() {
        try {
            boolean doPrint = payslipTextArea.print();
            if (doPrint) {
                JOptionPane.showMessageDialog(this,
                        "Payslip printed successfully!",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                    "Error printing payslip: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * FIXED: Enhanced savePayslip method with working file download functionality
     * This replaces the non-functional PDF save feature with a working text file save
     */
    private void savePayslip() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payslip");

        // Generate filename based on employee and period
        String filename = String.format("Payslip_%s_%s.txt",
                employee.getLastName().replaceAll("\\s+", ""),
                payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));

        fileChooser.setSelectedFile(new java.io.File(filename));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File fileToSave = fileChooser.getSelectedFile();

                // Ensure .txt extension
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".txt")) {
                    fileToSave = new java.io.File(filePath + ".txt");
                }

                // Write payslip content to file
                java.nio.file.Files.write(fileToSave.toPath(),
                        payslipTextArea.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));

                JOptionPane.showMessageDialog(this,
                        "Payslip saved successfully to:\n" + fileToSave.getAbsolutePath() +
                                "\n\nYou can open this file with any text editor.",
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // Ask user if they want to open the file
                int openFile = JOptionPane.showConfirmDialog(this,
                        "Would you like to open the saved payslip file now?",
                        "Open File?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (openFile == JOptionPane.YES_OPTION) {
                    try {
                        // Try to open the file with the system default application
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(fileToSave);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "File saved but cannot open automatically.\n" +
                                            "Please navigate to: " + fileToSave.getAbsolutePath(),
                                    "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception openError) {
                        JOptionPane.showMessageDialog(this,
                                "File saved successfully but could not open automatically.\n" +
                                        "Please navigate to: " + fileToSave.getAbsolutePath(),
                                "Information",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            } catch (java.io.IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving payslip: " + e.getMessage() +
                                "\n\nPlease check that you have write permissions to the selected location.",
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Unexpected error saving payslip: " + e.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * DEPRECATED: Old savePDF method - replaced with working savePayslip method
     * This method was showing a "not implemented" message
     */
    @Deprecated
    private void savePDF() {
        // This method has been replaced by savePayslip() which actually works
        savePayslip();
    }
}