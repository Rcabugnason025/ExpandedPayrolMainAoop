package dao;

import util.DBConnection;
import model.Overtime;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Overtime operations
 * Handles all database operations related to overtime records
 * @author rejoice
 */
public class OvertimeDAO {
    private static final Logger logger = Logger.getLogger(OvertimeDAO.class.getName());
    
    // SQL Query constants for better maintainability
    private static final String SELECT_BY_EMPLOYEE_ID = 
        "SELECT overtime_id, employee_id, date, hours, reason, approved FROM overtime WHERE employee_id = ? ORDER BY date DESC";
    
    private static final String SELECT_BY_EMPLOYEE_ID_AND_DATE_RANGE = 
        "SELECT overtime_id, employee_id, date, hours, reason, approved FROM overtime " +
        "WHERE employee_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";
    
    private static final String INSERT_OVERTIME = 
        "INSERT INTO overtime (employee_id, date, hours, reason, approved) VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_OVERTIME = 
        "UPDATE overtime SET employee_id = ?, date = ?, hours = ?, reason = ?, approved = ? WHERE overtime_id = ?";
    
    private static final String DELETE_OVERTIME = 
        "DELETE FROM overtime WHERE overtime_id = ?";
    
    private static final String SELECT_TOTAL_HOURS = 
        "SELECT COALESCE(SUM(hours), 0) as total_hours FROM overtime " +
        "WHERE employee_id = ? AND date >= ? AND date <= ?";
    
    private static final String SELECT_BY_ID = 
        "SELECT overtime_id, employee_id, date, hours, reason, approved FROM overtime WHERE overtime_id = ?";
    
    /**
     * Retrieves all overtime records for a specific employee
     * @param empId Employee ID
     * @return List of overtime records, empty list if none found
     * @throws IllegalArgumentException if empId is invalid
     * @throws RuntimeException if database error occurs
     */
    public List<Overtime> getOvertimeByEmployeeId(int empId) {
        validateEmployeeId(empId);
        
        List<Overtime> overtimeList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {

            stmt.setInt(1, empId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Overtime overtime = mapResultSetToOvertime(rs);
                    overtimeList.add(overtime);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving overtime for employee ID: " + empId, ex);
            throw new RuntimeException("Failed to retrieve overtime records for employee: " + empId, ex);
        }

        return overtimeList;
    }

    /**
     * Retrieves overtime records for an employee within a date range
     * @param employeeId Employee ID
     * @param periodStart Start date of the period (inclusive)
     * @param periodEnd End date of the period (inclusive)
     * @return List of overtime records within the date range, empty list if none found
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database error occurs
     */
    public List<Overtime> getOvertimeByEmployeeIdAndDateRange(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        validateEmployeeId(employeeId);
        validateDateRange(periodStart, periodEnd);
        
        List<Overtime> overtimeList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID_AND_DATE_RANGE)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Overtime overtime = mapResultSetToOvertime(rs);
                    overtimeList.add(overtime);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, 
                String.format("Error retrieving overtime for employee ID: %d between dates: %s and %s", 
                    employeeId, periodStart, periodEnd), ex);
            throw new RuntimeException("Failed to retrieve overtime records for date range", ex);
        }

        return overtimeList;
    }
    
    /**
     * Retrieves a single overtime record by ID
     * @param overtimeId Overtime record ID
     * @return Overtime object if found, null otherwise
     * @throws IllegalArgumentException if overtimeId is invalid
     * @throws RuntimeException if database error occurs
     */
    public Overtime getOvertimeById(int overtimeId) {
        if (overtimeId <= 0) {
            throw new IllegalArgumentException("Overtime ID must be positive");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, overtimeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOvertime(rs);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving overtime by ID: " + overtimeId, ex);
            throw new RuntimeException("Failed to retrieve overtime record", ex);
        }
        
        return null;
    }
    
    /**
     * Inserts a new overtime record
     * @param overtime Overtime object to insert
     * @return Generated overtime ID
     * @throws IllegalArgumentException if overtime object is invalid
     * @throws RuntimeException if database error occurs
     */
    public int insertOvertime(Overtime overtime) {
        validateOvertimeForInsert(overtime);
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_OVERTIME, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, overtime.getEmployeeId());
            stmt.setDate(2, overtime.getDate());
            stmt.setDouble(3, overtime.getHours());
            stmt.setString(4, overtime.getReason());
            stmt.setBoolean(5, overtime.isApproved());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating overtime failed, no rows affected");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    overtime.setOvertimeId(generatedId);
                    logger.info("Successfully inserted overtime record with ID: " + generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating overtime failed, no ID obtained");
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting overtime record", ex);
            throw new RuntimeException("Failed to insert overtime record", ex);
        }
    }
    
    /**
     * Updates an existing overtime record
     * @param overtime Overtime object with updated information
     * @return true if update was successful, false if no record was found
     * @throws IllegalArgumentException if overtime object is invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean updateOvertime(Overtime overtime) {
        validateOvertimeForUpdate(overtime);
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_OVERTIME)) {
            
            stmt.setInt(1, overtime.getEmployeeId());
            stmt.setDate(2, overtime.getDate());
            stmt.setDouble(3, overtime.getHours());
            stmt.setString(4, overtime.getReason());
            stmt.setBoolean(5, overtime.isApproved());
            stmt.setInt(6, overtime.getOvertimeId());
            
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;
            
            if (success) {
                logger.info("Successfully updated overtime record with ID: " + overtime.getOvertimeId());
            } else {
                logger.warning("No overtime record found with ID: " + overtime.getOvertimeId());
            }
            
            return success;
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating overtime record with ID: " + overtime.getOvertimeId(), ex);
            throw new RuntimeException("Failed to update overtime record", ex);
        }
    }
    
    /**
     * Deletes an overtime record
     * @param overtimeId ID of the overtime record to delete
     * @return true if deletion was successful, false if no record was found
     * @throws IllegalArgumentException if overtimeId is invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean deleteOvertime(int overtimeId) {
        if (overtimeId <= 0) {
            throw new IllegalArgumentException("Overtime ID must be positive");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_OVERTIME)) {
            
            stmt.setInt(1, overtimeId);
            
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;
            
            if (success) {
                logger.info("Successfully deleted overtime record with ID: " + overtimeId);
            } else {
                logger.warning("No overtime record found with ID: " + overtimeId);
            }
            
            return success;
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting overtime record with ID: " + overtimeId, ex);
            throw new RuntimeException("Failed to delete overtime record", ex);
        }
    }
    
    /**
     * Calculates total overtime hours for an employee within a date range
     * @param employeeId Employee ID
     * @param periodStart Start date of the period (inclusive)
     * @param periodEnd End date of the period (inclusive)
     * @return Total overtime hours (0.0 if no records found)
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database error occurs
     */
    public double getTotalOvertimeHours(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        validateEmployeeId(employeeId);
        validateDateRange(periodStart, periodEnd);
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_TOTAL_HOURS)) {
            
            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_hours");
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, 
                String.format("Error calculating total overtime hours for employee ID: %d between %s and %s", 
                    employeeId, periodStart, periodEnd), ex);
            throw new RuntimeException("Failed to calculate total overtime hours", ex);
        }
        
        return 0.0;
    }
    
    /**
     * Gets count of overtime records for an employee within a date range
     * @param employeeId Employee ID
     * @param periodStart Start date of the period
     * @param periodEnd End date of the period
     * @return Number of overtime records
     */
    public int getOvertimeCount(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        validateEmployeeId(employeeId);
        validateDateRange(periodStart, periodEnd);
        
        String query = "SELECT COUNT(*) as count FROM overtime WHERE employee_id = ? AND date >= ? AND date <= ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error counting overtime records", ex);
            throw new RuntimeException("Failed to count overtime records", ex);
        }
        
        return 0;
    }
    
    // Private helper methods for validation
    
    private void validateEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
    }
    
    private void validateDateRange(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }
    }
    
    private void validateOvertimeForInsert(Overtime overtime) {
        if (overtime == null) {
            throw new IllegalArgumentException("Overtime cannot be null");
        }
        if (overtime.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (overtime.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (overtime.getHours() < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
    }
    
    private void validateOvertimeForUpdate(Overtime overtime) {
        validateOvertimeForInsert(overtime);
        if (overtime.getOvertimeId() <= 0) {
            throw new IllegalArgumentException("Overtime ID must be positive for updates");
        }
    }
    
    /**
     * Maps ResultSet to Overtime object
     * @param rs ResultSet from database
     * @return Overtime object
     * @throws SQLException if database access error occurs
     */
    private Overtime mapResultSetToOvertime(ResultSet rs) throws SQLException {
        Overtime overtime = new Overtime();
        overtime.setOvertimeId(rs.getInt("overtime_id"));
        overtime.setEmployeeId(rs.getInt("employee_id"));
        overtime.setDate(rs.getDate("date"));
        overtime.setHours(rs.getDouble("hours"));
        overtime.setReason(rs.getString("reason"));
        overtime.setApproved(rs.getBoolean("approved"));
        return overtime;
    }
}