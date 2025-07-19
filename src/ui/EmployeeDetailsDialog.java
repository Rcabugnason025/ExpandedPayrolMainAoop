package ui;

import dao.EmployeeDAO;
import dao.CredentialsDAO;
import model.Employee;
import ui.PasswordChangeDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EmployeeDetailsDialog extends JDialog {
    private Employee employee;
    private boolean isEditMode;
    private boolean isNewEmployee;
    private EmployeeDAO employeeDAO;
    private CredentialsDAO credentialsDAO;

    // Form fields
    private JTextField employeeIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JSpinner birthdaySpinner;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField sssField;
    private JTextField philhealthField;
    private JTextField tinField;
    private JTextField pagibigField;
    private JComboBox<String> statusComboBox;
    private JTextField positionField;
    private JTextField supervisorField;
    private JTextField basicSalaryField;
    private JTextField riceSubsidyField;
    private JTextField phoneAllowanceField;
    private JTextField clothingAllowanceField;

    // Buttons
    private JButton editButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton changePasswordButton;

    public EmployeeDetailsDialog(Frame parent, Employee employee, boolean isNewEmployee) {
        super(parent, isNewEmployee ? "Add New Employee" : "Employee Details", true);
        this.employee = employee != null ? employee : new Employee();
        this.isNewEmployee = isNewEmployee;
        this.isEditMode = isNewEmployee;
        this.employeeDAO = new EmployeeDAO();
        this.credentialsDAO = new CredentialsDAO();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateFields();

        setSize(600, 700);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Create form fields
        employeeIdField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);

        // Birthday spinner - FIXED: Proper date handling
        SpinnerDateModel birthdayModel = new SpinnerDateModel();
        birthdaySpinner = new JSpinner(birthdayModel);
        JSpinner.DateEditor birthdayEditor = new JSpinner.DateEditor(birthdaySpinner, "yyyy-MM-dd");
        birthdaySpinner.setEditor(birthdayEditor);

        addressField = new JTextField(30);
        phoneField = new JTextField(15);
        sssField = new JTextField(15);
        philhealthField = new JTextField(15);
        tinField = new JTextField(15);
        pagibigField = new JTextField(15);

        String[] statuses = {"Regular", "Probationary", "Contractual", "Part-time"};
        statusComboBox = new JComboBox<>(statuses);

        positionField = new JTextField(20);
        supervisorField = new JTextField(20);
        basicSalaryField = new JTextField(15);
        riceSubsidyField = new JTextField(15);
        phoneAllowanceField = new JTextField(15);
        clothingAllowanceField = new JTextField(15);

        // Buttons
        editButton = new JButton("Edit");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        changePasswordButton = new JButton("Change Password");

        // Button styling
        editButton.setBackground(new Color(107, 190, 206));
        editButton.setForeground(Color.BLACK);
        saveButton.setBackground(new Color(202, 238, 202));
        saveButton.setForeground(Color.BLACK);
        cancelButton.setBackground(new Color(220, 220, 220));
        changePasswordButton.setBackground(new Color(215, 188, 156));
        changePasswordButton.setForeground(Color.BLACK);

        // Set initial field states
        setFieldsEditable(isEditMode);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(isNewEmployee ? "Add New Employee" : "Employee Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Main form panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Personal info panel
        JPanel personalPanel = createPersonalInfoPanel();

        // Work info panel
        JPanel workPanel = createWorkInfoPanel();

        // Compensation panel
        JPanel compensationPanel = createCompensationPanel();

        // Combine panels using a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Personal Information", personalPanel);
        tabbedPane.addTab("Work Information", workPanel);
        tabbedPane.addTab("Compensation", compensationPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (!isNewEmployee) {
            buttonPanel.add(editButton);
            buttonPanel.add(changePasswordButton);
        }
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        panel.add(employeeIdField, gbc);

        // First Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        panel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        panel.add(lastNameField, gbc);

        // Birthday
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 1;
        panel.add(birthdaySpinner, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(addressField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        return panel;
    }

    private JPanel createWorkInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Position
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 1;
        panel.add(positionField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Employment Status:"), gbc);
        gbc.gridx = 1;
        panel.add(statusComboBox, gbc);

        // Supervisor
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Immediate Supervisor:"), gbc);
        gbc.gridx = 1;
        panel.add(supervisorField, gbc);

        // SSS Number
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("SSS Number:"), gbc);
        gbc.gridx = 1;
        panel.add(sssField, gbc);

        // PhilHealth Number
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("PhilHealth Number:"), gbc);
        gbc.gridx = 1;
        panel.add(philhealthField, gbc);

        // TIN Number
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("TIN Number:"), gbc);
        gbc.gridx = 1;
        panel.add(tinField, gbc);

        // Pag-IBIG Number
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Pag-IBIG Number:"), gbc);
        gbc.gridx = 1;
        panel.add(pagibigField, gbc);

        return panel;
    }

    private JPanel createCompensationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Basic Salary
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        panel.add(basicSalaryField, gbc);

        // Rice Subsidy
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Rice Subsidy:"), gbc);
        gbc.gridx = 1;
        panel.add(riceSubsidyField, gbc);

        // Phone Allowance
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Phone Allowance:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneAllowanceField, gbc);

        // Clothing Allowance
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Clothing Allowance:"), gbc);
        gbc.gridx = 1;
        panel.add(clothingAllowanceField, gbc);

        return panel;
    }

    private void setupEventHandlers() {
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditMode(true);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveEmployee();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isEditMode && !isNewEmployee) {
                    setEditMode(false);
                    populateFields(); // Restore original values
                } else {
                    dispose();
                }
            }
        });

        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PasswordChangeDialog dialog = new PasswordChangeDialog(
                        (Frame) getParent(), employee);
                dialog.setVisible(true);
            }
        });
    }

    private void populateFields() {
        if (employee.getEmployeeId() > 0) {
            employeeIdField.setText(String.valueOf(employee.getEmployeeId()));
        }
        firstNameField.setText(employee.getFirstName() != null ? employee.getFirstName() : "");
        lastNameField.setText(employee.getLastName() != null ? employee.getLastName() : "");

        // FIXED: Proper birthday handling
        if (employee.getBirthday() != null) {
            // Convert LocalDate to java.util.Date properly
            Date utilDate = Date.from(employee.getBirthday().atStartOfDay(ZoneId.systemDefault()).toInstant());
            birthdaySpinner.setValue(utilDate);
        } else {
            // Set default date (25 years ago)
            LocalDate defaultDate = LocalDate.now().minusYears(25);
            Date utilDate = Date.from(defaultDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            birthdaySpinner.setValue(utilDate);
        }

        addressField.setText(employee.getAddress() != null ? employee.getAddress() : "");
        phoneField.setText(employee.getPhoneNumber() != null ? employee.getPhoneNumber() : "");
        sssField.setText(employee.getSssNumber() != null ? employee.getSssNumber() : "");
        philhealthField.setText(employee.getPhilhealthNumber() != null ? employee.getPhilhealthNumber() : "");
        tinField.setText(employee.getTinNumber() != null ? employee.getTinNumber() : "");
        pagibigField.setText(employee.getPagibigNumber() != null ? employee.getPagibigNumber() : "");

        statusComboBox.setSelectedItem(employee.getStatus() != null ? employee.getStatus() : "Regular");
        positionField.setText(employee.getPosition() != null ? employee.getPosition() : "");
        supervisorField.setText(employee.getImmediateSupervisor() != null ? employee.getImmediateSupervisor() : "");

        basicSalaryField.setText(String.valueOf(employee.getBasicSalary()));
        riceSubsidyField.setText(String.valueOf(employee.getRiceSubsidy()));
        phoneAllowanceField.setText(String.valueOf(employee.getPhoneAllowance()));
        clothingAllowanceField.setText(String.valueOf(employee.getClothingAllowance()));
    }

    private void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        setFieldsEditable(editMode);

        editButton.setVisible(!editMode && !isNewEmployee);
        saveButton.setText(editMode ? "Save" : "Close");
        cancelButton.setText(editMode ? "Cancel" : "Close");
        changePasswordButton.setVisible(!editMode && !isNewEmployee);
    }

    private void setFieldsEditable(boolean editable) {
        employeeIdField.setEditable(editable && isNewEmployee); // Only editable for new employees
        firstNameField.setEditable(editable);
        lastNameField.setEditable(editable);
        birthdaySpinner.setEnabled(editable);
        addressField.setEditable(editable);
        phoneField.setEditable(editable);
        sssField.setEditable(editable);
        philhealthField.setEditable(editable);
        tinField.setEditable(editable);
        pagibigField.setEditable(editable);
        statusComboBox.setEnabled(editable);
        positionField.setEditable(editable);
        supervisorField.setEditable(editable);
        basicSalaryField.setEditable(editable);
        riceSubsidyField.setEditable(editable);
        phoneAllowanceField.setEditable(editable);
        clothingAllowanceField.setEditable(editable);
    }

    private void saveEmployee() {
        try {
            // Validate required fields
            if (firstNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name is required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name is required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isNewEmployee && employeeIdField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Employee ID is required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Update employee object
            if (isNewEmployee) {
                employee.setEmployeeId(Integer.parseInt(employeeIdField.getText().trim()));
            }

            employee.setFirstName(firstNameField.getText().trim());
            employee.setLastName(lastNameField.getText().trim());

            // FIXED: Proper birthday conversion
            Date utilDate = (Date) birthdaySpinner.getValue();
            if (utilDate != null) {
                LocalDate localDate = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                employee.setBirthday(localDate);
            }

            employee.setAddress(addressField.getText().trim());
            employee.setPhoneNumber(phoneField.getText().trim());
            employee.setSssNumber(sssField.getText().trim());
            employee.setPhilhealthNumber(philhealthField.getText().trim());
            employee.setTinNumber(tinField.getText().trim());
            employee.setPagibigNumber(pagibigField.getText().trim());
            employee.setStatus((String) statusComboBox.getSelectedItem());
            employee.setPosition(positionField.getText().trim());
            employee.setImmediateSupervisor(supervisorField.getText().trim());

            // Parse salary fields
            employee.setBasicSalary(Double.parseDouble(basicSalaryField.getText().trim()));
            employee.setRiceSubsidy(Double.parseDouble(riceSubsidyField.getText().trim()));
            employee.setPhoneAllowance(Double.parseDouble(phoneAllowanceField.getText().trim()));
            employee.setClothingAllowance(Double.parseDouble(clothingAllowanceField.getText().trim()));

            // Calculate derived fields
            employee.setGrossSemiMonthlyRate(employee.getBasicSalary() / 2);
            employee.setHourlyRate(employee.getBasicSalary() / 22 / 8); // 22 working days, 8 hours per day

            boolean success;
            if (isNewEmployee) {
                success = employeeDAO.insertEmployee(employee);
                if (success) {
                    // Create default credentials
                    credentialsDAO.createCredentials(employee.getEmployeeId(), "password1234");
                    JOptionPane.showMessageDialog(this,
                            "Employee added successfully!\nDefault password: password1234",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                success = employeeDAO.updateEmployee(employee);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Employee updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (success) {
                if (isNewEmployee) {
                    dispose();
                } else {
                    setEditMode(false);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to " + (isNewEmployee ? "add" : "update") + " employee.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for salary fields.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving employee: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}