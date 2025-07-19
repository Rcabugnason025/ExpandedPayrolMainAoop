package dao;

import util.DBConnection;
import model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all employees", ex);
            throw new RuntimeException("Failed to fetch employees", ex);
        }

        return employees;
    }

    public Employee getEmployeeById(int employeeId) {
        String query = "SELECT * FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch employee", ex);
        }

        return null;
    }

    /**
     * Enhanced insertEmployee method with comprehensive validation
     * @param e Employee object to insert
     * @return true if insertion was successful
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if database operation fails
     */
    public boolean insertEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        // Validate required fields
        if (e.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (e.getFirstName() == null || e.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (e.getLastName() == null || e.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (e.getBasicSalary() < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }

        // Additional validation for business rules
        if (e.getFirstName().trim().length() > 50) {
            throw new IllegalArgumentException("First name cannot exceed 50 characters");
        }
        if (e.getLastName().trim().length() > 50) {
            throw new IllegalArgumentException("Last name cannot exceed 50 characters");
        }
        if (e.getPosition() != null && e.getPosition().trim().length() > 100) {
            throw new IllegalArgumentException("Position cannot exceed 100 characters");
        }
        if (e.getRiceSubsidy() < 0) {
            throw new IllegalArgumentException("Rice subsidy cannot be negative");
        }
        if (e.getPhoneAllowance() < 0) {
            throw new IllegalArgumentException("Phone allowance cannot be negative");
        }
        if (e.getClothingAllowance() < 0) {
            throw new IllegalArgumentException("Clothing allowance cannot be negative");
        }

        // Validate phone number format if provided
        if (e.getPhoneNumber() != null && !e.getPhoneNumber().trim().isEmpty()) {
            String phone = e.getPhoneNumber().trim();
            if (phone.length() > 20) {
                throw new IllegalArgumentException("Phone number cannot exceed 20 characters");
            }
            // Basic phone number validation (digits, spaces, hyphens, parentheses)
            if (!phone.matches("[0-9\\s\\-\\(\\)\\+]+")) {
                throw new IllegalArgumentException("Phone number contains invalid characters");
            }
        }

        // Validate status enum
        if (e.getStatus() != null && !e.getStatus().trim().isEmpty()) {
            String status = e.getStatus().trim();
            if (!status.equals("Regular") && !status.equals("Probationary")) {
                throw new IllegalArgumentException("Status must be either 'Regular' or 'Probationary'");
            }
        }

        // Check for duplicate employee ID
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {

            checkStmt.setInt(1, e.getEmployeeId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Employee ID " + e.getEmployeeId() + " already exists");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking for duplicate employee ID: " + e.getEmployeeId(), ex);
            throw new RuntimeException("Error checking for duplicate employee ID: " + ex.getMessage(), ex);
        }

        // Perform the insertion
        String sql = "INSERT INTO employees (employee_id, last_name, first_name, birthday, address, " +
                "phone_number, sss_number, philhealth_number, tin_number, pagibig_number, " +
                "status, position, immediate_supervisor, basic_salary, rice_subsidy, " +
                "phone_allowance, clothing_allowance, gross_semi_monthly_rate, hourly_rate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getEmployeeId());
            stmt.setString(2, e.getLastName().trim());
            stmt.setString(3, e.getFirstName().trim());
            stmt.setDate(4, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(5, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(6, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(7, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(8, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(9, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(10, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(11, e.getStatus() != null ? e.getStatus().trim() : "Regular");
            stmt.setString(12, e.getPosition() != null ? e.getPosition().trim() : null);
            stmt.setString(13, e.getImmediateSupervisor() != null ? e.getImmediateSupervisor().trim() : null);
            stmt.setDouble(14, e.getBasicSalary());
            stmt.setDouble(15, e.getRiceSubsidy());
            stmt.setDouble(16, e.getPhoneAllowance());
            stmt.setDouble(17, e.getClothingAllowance());
            stmt.setDouble(18, e.getGrossSemiMonthlyRate());
            stmt.setDouble(19, e.getHourlyRate());

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully inserted employee: " + e.getEmployeeId() + " - " + e.getFullName());
                return true;
            } else {
                LOGGER.warning("No rows affected when inserting employee: " + e.getEmployeeId());
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting employee: " + e.getEmployeeId(), ex);

            // Provide more specific error messages based on SQL error codes
            if (ex.getErrorCode() == 1062) { // Duplicate entry
                throw new RuntimeException("Employee ID " + e.getEmployeeId() + " already exists in the database", ex);
            } else if (ex.getErrorCode() == 1406) { // Data too long
                throw new RuntimeException("One or more field values exceed maximum length limits", ex);
            } else if (ex.getErrorCode() == 1048) { // Column cannot be null
                throw new RuntimeException("Required field is missing or null", ex);
            } else {
                throw new RuntimeException("Failed to insert employee: " + ex.getMessage(), ex);
            }
        }
    }

    public boolean updateEmployee(Employee e) {
        if (e == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (e.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        // Apply similar validation as insertEmployee but for updates
        if (e.getFirstName() == null || e.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (e.getLastName() == null || e.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (e.getBasicSalary() < 0) {
            throw new IllegalArgumentException("Basic salary cannot be negative");
        }

        String sql = "UPDATE employees SET last_name=?, first_name=?, birthday=?, address=?, " +
                "phone_number=?, sss_number=?, philhealth_number=?, tin_number=?, " +
                "pagibig_number=?, status=?, position=?, immediate_supervisor=?, " +
                "basic_salary=?, rice_subsidy=?, phone_allowance=?, clothing_allowance=?, " +
                "gross_semi_monthly_rate=?, hourly_rate=? WHERE employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, e.getLastName().trim());
            stmt.setString(2, e.getFirstName().trim());
            stmt.setDate(3, e.getBirthday() != null ? java.sql.Date.valueOf(e.getBirthday()) : null);
            stmt.setString(4, e.getAddress() != null ? e.getAddress().trim() : null);
            stmt.setString(5, e.getPhoneNumber() != null ? e.getPhoneNumber().trim() : null);
            stmt.setString(6, e.getSssNumber() != null ? e.getSssNumber().trim() : null);
            stmt.setString(7, e.getPhilhealthNumber() != null ? e.getPhilhealthNumber().trim() : null);
            stmt.setString(8, e.getTinNumber() != null ? e.getTinNumber().trim() : null);
            stmt.setString(9, e.getPagibigNumber() != null ? e.getPagibigNumber().trim() : null);
            stmt.setString(10, e.getStatus() != null ? e.getStatus().trim() : "Regular");
            stmt.setString(11, e.getPosition() != null ? e.getPosition().trim() : null);
            stmt.setString(12, e.getImmediateSupervisor() != null ? e.getImmediateSupervisor().trim() : null);
            stmt.setDouble(13, e.getBasicSalary());
            stmt.setDouble(14, e.getRiceSubsidy());
            stmt.setDouble(15, e.getPhoneAllowance());
            stmt.setDouble(16, e.getClothingAllowance());
            stmt.setDouble(17, e.getGrossSemiMonthlyRate());
            stmt.setDouble(18, e.getHourlyRate());
            stmt.setInt(19, e.getEmployeeId());

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully updated employee: " + e.getEmployeeId() + " - " + e.getFullName());
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + e.getEmployeeId() + " for update");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating employee with ID: " + e.getEmployeeId(), ex);
            throw new RuntimeException("Failed to update employee: " + ex.getMessage(), ex);
        }
    }

    public boolean deleteEmployee(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully deleted employee with ID: " + employeeId);
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + employeeId + " for deletion");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting employee with ID: " + employeeId, ex);

            // Check for foreign key constraint violations
            if (ex.getErrorCode() == 1451) { // Cannot delete due to foreign key constraint
                throw new RuntimeException("Cannot delete employee: related records exist (attendance, payroll, etc.)", ex);
            } else {
                throw new RuntimeException("Failed to delete employee: " + ex.getMessage(), ex);
            }
        }
    }

    public List<Employee> getEmployeesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE status = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by status: " + status, ex);
            throw new RuntimeException("Failed to fetch employees by status", ex);
        }

        return employees;
    }

    public List<Employee> getEmployeesByPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("Position cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE position = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, position.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by position: " + position, ex);
            throw new RuntimeException("Failed to fetch employees by position", ex);
        }

        return employees;
    }

    public List<Employee> getEmployeesBySupervisor(String supervisor) {
        if (supervisor == null || supervisor.trim().isEmpty()) {
            throw new IllegalArgumentException("Supervisor cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE immediate_supervisor = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, supervisor.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by supervisor: " + supervisor, ex);
            throw new RuntimeException("Failed to fetch employees by supervisor", ex);
        }

        return employees;
    }

    public List<Employee> searchEmployees(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEmployees();
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE " +
                "CONCAT(first_name, ' ', last_name) LIKE ? OR " +
                "employee_id LIKE ? OR " +
                "position LIKE ? " +
                "ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error searching employees with term: " + searchTerm, ex);
            throw new RuntimeException("Failed to search employees", ex);
        }

        return employees;
    }

    /**
     * Enhanced mapResultSetToEmployee with better null handling
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setEmployeeId(rs.getInt("employee_id"));
        e.setLastName(rs.getString("last_name"));
        e.setFirstName(rs.getString("first_name"));

        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            e.setBirthday(birthday.toLocalDate());
        }

        e.setAddress(rs.getString("address"));
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setSssNumber(rs.getString("sss_number"));
        e.setPhilhealthNumber(rs.getString("philhealth_number"));
        e.setTinNumber(rs.getString("tin_number"));
        e.setPagibigNumber(rs.getString("pagibig_number"));
        e.setStatus(rs.getString("status"));
        e.setPosition(rs.getString("position"));
        e.setImmediateSupervisor(rs.getString("immediate_supervisor"));
        e.setBasicSalary(rs.getDouble("basic_salary"));
        e.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        e.setPhoneAllowance(rs.getDouble("phone_allowance"));
        e.setClothingAllowance(rs.getDouble("clothing_allowance"));
        e.setGrossSemiMonthlyRate(rs.getDouble("gross_semi_monthly_rate"));
        e.setHourlyRate(rs.getDouble("hourly_rate"));

        return e;
    }

    /**
     * Utility method to check if an employee exists
     * @param employeeId Employee ID to check
     * @return true if employee exists, false otherwise
     */
    public boolean employeeExists(int employeeId) {
        if (employeeId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if employee exists: " + employeeId, ex);
            return false;
        }
    }

    /**
     * Get the count of employees by status
     * @param status Employment status
     * @return number of employees with the given status
     */
    public int getEmployeeCountByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String query = "SELECT COUNT(*) FROM employees WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by status: " + status, ex);
            throw new RuntimeException("Failed to get employee count", ex);
        }

        return 0;
    }
}