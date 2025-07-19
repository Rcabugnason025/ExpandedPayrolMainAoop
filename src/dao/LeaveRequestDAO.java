package dao;

import util.DBConnection;
import model.LeaveRequest;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LeaveRequestDAO {
    private static final Logger logger = Logger.getLogger(LeaveRequestDAO.class.getName());

    // SQL Queries as constants for better maintainability
    private static final String SELECT_BY_EMPLOYEE_ID =
            "SELECT * FROM leave_request WHERE employee_id = ? ORDER BY start_date DESC";

    private static final String SELECT_APPROVED_BY_EMPLOYEE_ID =
            "SELECT * FROM leave_request WHERE employee_id = ? AND status = ? ORDER BY start_date DESC";

    private static final String SELECT_APPROVED_BY_EMPLOYEE_AND_DATE_RANGE = """
        SELECT * FROM leave_request 
        WHERE employee_id = ? AND status = ? 
        AND ((start_date >= ? AND start_date <= ?) 
             OR (end_date >= ? AND end_date <= ?)
             OR (start_date <= ? AND end_date >= ?))
        ORDER BY start_date DESC
        """;

    private static final String SELECT_BY_STATUS =
            "SELECT * FROM leave_request WHERE status = ? ORDER BY start_date DESC";

    private static final String INSERT_LEAVE_REQUEST =
            "INSERT INTO leave_request (employee_id, leave_type, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_STATUS =
            "UPDATE leave_request SET status = ? WHERE leave_id = ?";

    private static final String UPDATE_LEAVE_REQUEST =
            "UPDATE leave_request SET employee_id = ?, leave_type = ?, start_date = ?, end_date = ?, status = ? WHERE leave_id = ?";

    private static final String DELETE_LEAVE_REQUEST =
            "DELETE FROM leave_request WHERE leave_id = ?";

    private static final String SELECT_BY_ID =
            "SELECT * FROM leave_request WHERE leave_id = ?";

    private static final String CHECK_OVERLAPPING_LEAVE = """
        SELECT COUNT(*) FROM leave_request 
        WHERE employee_id = ? AND status = ? 
        AND ((start_date >= ? AND start_date <= ?) 
             OR (end_date >= ? AND end_date <= ?)
             OR (start_date <= ? AND end_date >= ?))
        """;

    // Status constants
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_APPROVED = "Approved";
    private static final String STATUS_REJECTED = "Rejected";

    /**
     * Retrieves all leave requests for a specific employee
     * @param empId Employee ID
     * @return List of leave requests
     * @throws IllegalArgumentException if empId is invalid
     * @throws RuntimeException if database error occurs
     */
    public List<LeaveRequest> getLeaveRequestsByEmployeeId(int empId) {
        validateEmployeeId(empId);

        List<LeaveRequest> leaveRequests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {

            stmt.setInt(1, empId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaveRequests.add(mapResultSetToLeaveRequest(rs));
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error retrieving leave requests for employee ID: %d", empId), ex);
            throw new RuntimeException("Failed to retrieve leave requests for employee: " + empId, ex);
        }

        return leaveRequests;
    }

    /**
     * Retrieves all approved leave requests for a specific employee
     * @param empId Employee ID
     * @return List of approved leave requests
     * @throws IllegalArgumentException if empId is invalid
     * @throws RuntimeException if database error occurs
     */
    public List<LeaveRequest> getApprovedLeavesByEmployeeId(int empId) {
        validateEmployeeId(empId);

        List<LeaveRequest> approvedLeaves = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_APPROVED_BY_EMPLOYEE_ID)) {

            stmt.setInt(1, empId);
            stmt.setString(2, STATUS_APPROVED);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    approvedLeaves.add(mapResultSetToLeaveRequest(rs));
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error retrieving approved leaves for employee ID: %d", empId), ex);
            throw new RuntimeException("Failed to retrieve approved leave requests for employee: " + empId, ex);
        }

        return approvedLeaves;
    }

    /**
     * Retrieves approved leave requests for an employee within a date range
     * @param employeeId Employee ID
     * @param periodStart Start date of the period
     * @param periodEnd End date of the period
     * @return List of approved leave requests within the date range
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database error occurs
     */
    public List<LeaveRequest> getApprovedLeavesByEmployeeIdAndDateRange(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        validateEmployeeId(employeeId);
        validateDateRange(periodStart, periodEnd);

        List<LeaveRequest> approvedLeaves = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_APPROVED_BY_EMPLOYEE_AND_DATE_RANGE)) {

            // Convert LocalDate to java.sql.Date for database operations
            java.sql.Date sqlStartDate = java.sql.Date.valueOf(periodStart);
            java.sql.Date sqlEndDate = java.sql.Date.valueOf(periodEnd);

            stmt.setInt(1, employeeId);
            stmt.setString(2, STATUS_APPROVED);
            stmt.setDate(3, sqlStartDate);
            stmt.setDate(4, sqlEndDate);
            stmt.setDate(5, sqlStartDate);
            stmt.setDate(6, sqlEndDate);
            stmt.setDate(7, sqlStartDate);
            stmt.setDate(8, sqlEndDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    approvedLeaves.add(mapResultSetToLeaveRequest(rs));
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format(
                    "Error retrieving approved leaves for employee ID: %d between dates: %s and %s",
                    employeeId, periodStart, periodEnd), ex);
            throw new RuntimeException("Failed to retrieve approved leave requests for date range", ex);
        }

        return approvedLeaves;
    }

    /**
     * Retrieves leave requests by status
     * @param status Leave request status
     * @return List of leave requests with specified status
     * @throws IllegalArgumentException if status is invalid
     * @throws RuntimeException if database error occurs
     */
    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        validateStatus(status);

        List<LeaveRequest> leaveRequests = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STATUS)) {

            stmt.setString(1, status.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaveRequests.add(mapResultSetToLeaveRequest(rs));
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error retrieving leave requests by status: %s", status), ex);
            throw new RuntimeException("Failed to retrieve leave requests by status: " + status, ex);
        }

        return leaveRequests;
    }

    /**
     * ENHANCED: Inserts a new leave request with comprehensive error handling
     * @param leaveRequest Leave request to insert
     * @return Generated leave request ID
     * @throws IllegalArgumentException if leaveRequest is invalid
     * @throws RuntimeException if database error occurs
     */
    public int insertLeaveRequest(LeaveRequest leaveRequest) {
        validateLeaveRequestForInsert(leaveRequest);

        // ENHANCED: Check if leave_request table exists before attempting insert
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeQuery("SELECT 1 FROM leave_request LIMIT 1");
            logger.fine("Leave request table validation successful");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Leave request table validation failed", e);

            // Enhanced error message based on specific SQL error
            if (e.getMessage().contains("doesn't exist") ||
                    e.getMessage().contains("Table") && e.getMessage().contains("doesn't exist")) {
                throw new RuntimeException(
                        "Leave request table not found. Please run the database setup script to create required tables.\n" +
                                "Technical details: " + e.getMessage());
            } else if (e.getMessage().contains("Access denied")) {
                throw new RuntimeException(
                        "Database access denied. Please check your database credentials and permissions.\n" +
                                "Technical details: " + e.getMessage());
            } else if (e.getMessage().contains("Connection")) {
                throw new RuntimeException(
                        "Database connection failed. Please check if MySQL server is running.\n" +
                                "Technical details: " + e.getMessage());
            } else {
                throw new RuntimeException(
                        "Database validation failed. Please check your database configuration.\n" +
                                "Technical details: " + e.getMessage(), e);
            }
        }

        // ENHANCED: Insert leave request with better error handling
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LEAVE_REQUEST, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, leaveRequest.getEmployeeId());
            stmt.setString(2, leaveRequest.getLeaveType());
            stmt.setDate(3, leaveRequest.getStartDate());
            stmt.setDate(4, leaveRequest.getEndDate());
            stmt.setString(5, leaveRequest.getStatus() != null ? leaveRequest.getStatus() : STATUS_PENDING);

            logger.info(String.format("Attempting to insert leave request for employee %d: %s from %s to %s",
                    leaveRequest.getEmployeeId(), leaveRequest.getLeaveType(),
                    leaveRequest.getStartDate(), leaveRequest.getEndDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating leave request failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    leaveRequest.setLeaveId(generatedId);
                    logger.info(String.format("Successfully inserted leave request with ID: %d for employee %d",
                            generatedId, leaveRequest.getEmployeeId()));
                    return generatedId;
                } else {
                    throw new SQLException("Creating leave request failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting leave request", ex);

            // ENHANCED: Provide specific error messages based on SQL error types
            String errorMessage = "Failed to insert leave request";

            if (ex.getMessage().contains("Duplicate entry")) {
                errorMessage = "Duplicate leave request detected. Please check if this request already exists.";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                errorMessage = "Invalid employee ID. The specified employee does not exist in the system.";
            } else if (ex.getMessage().contains("Data too long")) {
                errorMessage = "Leave request data is too long. Please check your input length.";
            } else if (ex.getMessage().contains("Incorrect date")) {
                errorMessage = "Invalid date format. Please check your start and end dates.";
            } else if (ex.getMessage().contains("Out of range")) {
                errorMessage = "Date values are out of range. Please check your date inputs.";
            } else {
                errorMessage = "Failed to insert leave request: " + ex.getMessage();
            }

            throw new RuntimeException(errorMessage, ex);
        }
    }

    /**
     * Updates the status of a leave request
     * @param leaveId Leave request ID
     * @param status New status
     * @return true if update was successful
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean updateLeaveStatus(int leaveId, String status) {
        validateLeaveId(leaveId);
        validateStatus(status);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {

            stmt.setString(1, status.trim());
            stmt.setInt(2, leaveId);

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info(String.format("Successfully updated leave request %d status to %s", leaveId, status));
            } else {
                logger.warning(String.format("No leave request found with ID: %d", leaveId));
            }

            return updated;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error updating leave request status for ID: %d", leaveId), ex);
            throw new RuntimeException("Failed to update leave request status", ex);
        }
    }

    /**
     * Updates a leave request
     * @param leaveRequest Leave request with updated information
     * @return true if update was successful
     * @throws IllegalArgumentException if leaveRequest is invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean updateLeaveRequest(LeaveRequest leaveRequest) {
        validateLeaveRequestForUpdate(leaveRequest);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LEAVE_REQUEST)) {

            stmt.setInt(1, leaveRequest.getEmployeeId());
            stmt.setString(2, leaveRequest.getLeaveType());
            stmt.setDate(3, leaveRequest.getStartDate());
            stmt.setDate(4, leaveRequest.getEndDate());
            stmt.setString(5, leaveRequest.getStatus());
            stmt.setInt(6, leaveRequest.getLeaveId());

            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;

            if (updated) {
                logger.info(String.format("Successfully updated leave request with ID: %d", leaveRequest.getLeaveId()));
            } else {
                logger.warning(String.format("No leave request found with ID: %d", leaveRequest.getLeaveId()));
            }

            return updated;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error updating leave request with ID: %d", leaveRequest.getLeaveId()), ex);
            throw new RuntimeException("Failed to update leave request", ex);
        }
    }

    /**
     * Deletes a leave request
     * @param leaveId Leave request ID
     * @return true if deletion was successful
     * @throws IllegalArgumentException if leaveId is invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean deleteLeaveRequest(int leaveId) {
        validateLeaveId(leaveId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_LEAVE_REQUEST)) {

            stmt.setInt(1, leaveId);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;

            if (deleted) {
                logger.info(String.format("Successfully deleted leave request with ID: %d", leaveId));
            } else {
                logger.warning(String.format("No leave request found with ID: %d", leaveId));
            }

            return deleted;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error deleting leave request with ID: %d", leaveId), ex);
            throw new RuntimeException("Failed to delete leave request", ex);
        }
    }

    /**
     * Retrieves leave request by ID
     * @param leaveId Leave request ID
     * @return LeaveRequest object or null if not found
     * @throws IllegalArgumentException if leaveId is invalid
     * @throws RuntimeException if database error occurs
     */
    public Optional<LeaveRequest> getLeaveRequestById(int leaveId) {
        validateLeaveId(leaveId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, leaveId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLeaveRequest(rs));
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, String.format("Error retrieving leave request by ID: %d", leaveId), ex);
            throw new RuntimeException("Failed to retrieve leave request", ex);
        }

        return Optional.empty();
    }

    /**
     * Checks for overlapping leave requests
     * @param employeeId Employee ID
     * @param startDate Start date
     * @param endDate End date
     * @param excludeLeaveId Leave ID to exclude from check (for updates)
     * @return true if overlapping leave exists
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if database error occurs
     */
    public boolean hasOverlappingLeave(int employeeId, LocalDate startDate, LocalDate endDate, Integer excludeLeaveId) {
        validateEmployeeId(employeeId);
        validateDateRange(startDate, endDate);

        String query = CHECK_OVERLAPPING_LEAVE + (excludeLeaveId != null ? " AND leave_id != ?" : "");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Convert LocalDate to java.sql.Date for database operations
            java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate);
            java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate);

            int paramIndex = 1;
            stmt.setInt(paramIndex++, employeeId);
            stmt.setString(paramIndex++, STATUS_APPROVED);
            stmt.setDate(paramIndex++, sqlStartDate);
            stmt.setDate(paramIndex++, sqlEndDate);
            stmt.setDate(paramIndex++, sqlStartDate);
            stmt.setDate(paramIndex++, sqlEndDate);
            stmt.setDate(paramIndex++, sqlStartDate);
            stmt.setDate(paramIndex++, sqlEndDate);

            if (excludeLeaveId != null) {
                stmt.setInt(paramIndex, excludeLeaveId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking overlapping leave", ex);
            throw new RuntimeException("Failed to check overlapping leave", ex);
        }

        return false;
    }

    /**
     * Maps ResultSet to LeaveRequest object
     * @param rs ResultSet from database
     * @return LeaveRequest object
     * @throws SQLException if database access error occurs
     */
    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        LeaveRequest lr = new LeaveRequest();
        lr.setLeaveId(rs.getInt("leave_id"));
        lr.setEmployeeId(rs.getInt("employee_id"));
        lr.setLeaveType(rs.getString("leave_type"));
        lr.setStartDate(rs.getDate("start_date"));
        lr.setEndDate(rs.getDate("end_date"));
        lr.setStatus(rs.getString("status"));
        return lr;
    }

    // Validation helper methods
    private void validateEmployeeId(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive, got: " + empId);
        }
    }

    private void validateLeaveId(int leaveId) {
        if (leaveId <= 0) {
            throw new IllegalArgumentException("Leave ID must be positive, got: " + leaveId);
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
    }

    private void validateDateRange(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date: " + periodStart + " > " + periodEnd);
        }
    }

    private void validateLeaveRequestForInsert(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            throw new IllegalArgumentException("Leave request cannot be null");
        }
        validateEmployeeId(leaveRequest.getEmployeeId());
        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (leaveRequest.getLeaveType() == null || leaveRequest.getLeaveType().trim().isEmpty()) {
            throw new IllegalArgumentException("Leave type cannot be null or empty");
        }
    }

    private void validateLeaveRequestForUpdate(LeaveRequest leaveRequest) {
        validateLeaveRequestForInsert(leaveRequest);
        validateLeaveId(leaveRequest.getLeaveId());
    }
}