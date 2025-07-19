package dao;

import util.DBConnection;
import model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AttendanceDAO {
    private static final Logger logger = Logger.getLogger(AttendanceDAO.class.getName());

    public List<Attendance> getAttendanceByEmployeeId(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, empId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = mapResultSetToAttendance(rs);
                    list.add(a);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee ID: " + empId, ex);
            throw new RuntimeException("Failed to retrieve attendance records", ex);
        }

        return list;
    }


    public List<Attendance> getAttendanceByEmployeeIdBetweenDates(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period start and end dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }

        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM attendance WHERE employee_id = ? AND date >= ? AND date <= ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = mapResultSetToAttendance(rs);
                    list.add(a);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee ID: " + employeeId +
                    " between dates: " + periodStart + " and " + periodEnd, ex);
            throw new RuntimeException("Failed to retrieve attendance records", ex);
        }

        return list;
    }


    public int insertAttendance(Attendance attendance) {
        // ENHANCED: Comprehensive input validation
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        if (attendance.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive, got: " + attendance.getEmployeeId());
        }
        if (attendance.getDate() == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (attendance.getLogIn() == null) {
            throw new IllegalArgumentException("Log in time cannot be null");
        }

        // ENHANCED: Validate date is not in the future
        LocalDate today = LocalDate.now();
        LocalDate attendanceDate = attendance.getDate().toLocalDate();
        if (attendanceDate.isAfter(today)) {
            throw new IllegalArgumentException("Attendance date cannot be in the future: " + attendanceDate);
        }

        // ENHANCED: Validate date is not too far in the past (optional business rule)
        LocalDate maxPastDate = today.minusDays(30); // Allow up to 30 days past
        if (attendanceDate.isBefore(maxPastDate)) {
            throw new IllegalArgumentException("Attendance date is too far in the past: " + attendanceDate +
                    ". Maximum allowed past date is: " + maxPastDate);
        }

        // ENHANCED: Validate log times
        if (attendance.getLogOut() != null) {
            LocalTime logIn = attendance.getLogIn().toLocalTime();
            LocalTime logOut = attendance.getLogOut().toLocalTime();

            if (logOut.isBefore(logIn)) {
                throw new IllegalArgumentException("Log out time (" + logOut +
                        ") cannot be before log in time (" + logIn + ")");
            }

            // Check for reasonable working hours (24 hours max)
            long workingMinutes = java.time.Duration.between(logIn, logOut).toMinutes();
            if (workingMinutes > 24 * 60) { // More than 24 hours
                throw new IllegalArgumentException("Working hours cannot exceed 24 hours. " +
                        "Log in: " + logIn + ", Log out: " + logOut);
            }
        }

        // ENHANCED: Check if employee exists
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {

            checkStmt.setInt(1, attendance.getEmployeeId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Employee ID " + attendance.getEmployeeId() +
                            " does not exist in the system. Please verify the employee ID.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error validating employee existence for ID: " + attendance.getEmployeeId(), e);
            throw new RuntimeException("Error validating employee: " + e.getMessage(), e);
        }

        // ENHANCED: Check for duplicate attendance on the same date
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement duplicateStmt = conn.prepareStatement(
                     "SELECT id FROM attendance WHERE employee_id = ? AND date = ?")) {

            duplicateStmt.setInt(1, attendance.getEmployeeId());
            duplicateStmt.setDate(2, attendance.getDate());

            try (ResultSet rs = duplicateStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Attendance record already exists for employee " +
                            attendance.getEmployeeId() + " on date " + attendance.getDate() +
                            ". Existing record ID: " + rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking for duplicate attendance", e);
            throw new RuntimeException("Error checking duplicate attendance: " + e.getMessage(), e);
        }

        // ENHANCED: Insert attendance record with detailed logging
        String query = "INSERT INTO attendance (employee_id, date, log_in, log_out) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setDate(2, attendance.getDate());
            stmt.setTime(3, attendance.getLogIn());
            stmt.setTime(4, attendance.getLogOut()); // Can be null for incomplete attendance

            logger.info(String.format("Inserting attendance record for employee %d on %s: Log in: %s, Log out: %s",
                    attendance.getEmployeeId(), attendance.getDate(),
                    attendance.getLogIn(), attendance.getLogOut()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating attendance failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    attendance.setId(generatedId);

                    logger.info(String.format("Successfully inserted attendance record with ID: %d for employee %d",
                            generatedId, attendance.getEmployeeId()));

                    return generatedId;
                } else {
                    throw new SQLException("Creating attendance failed, no ID obtained.");
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting attendance record", ex);

            // ENHANCED: Provide specific error messages based on SQL error types
            String errorMessage = "Failed to insert attendance record";

            if (ex.getMessage().contains("Duplicate entry")) {
                errorMessage = "Duplicate attendance record. An attendance record already exists for this employee on this date.";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                errorMessage = "Invalid employee ID. The specified employee does not exist in the system.";
            } else if (ex.getMessage().contains("Data too long")) {
                errorMessage = "Attendance data is invalid. Please check your input values.";
            } else if (ex.getMessage().contains("Incorrect time")) {
                errorMessage = "Invalid time format. Please check your log in and log out times.";
            } else if (ex.getMessage().contains("Incorrect date")) {
                errorMessage = "Invalid date format. Please check your attendance date.";
            } else {
                errorMessage = "Failed to insert attendance record: " + ex.getMessage();
            }

            throw new RuntimeException(errorMessage, ex);
        }
    }


    public boolean updateAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }
        if (attendance.getId() <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "UPDATE attendance SET employee_id = ?, date = ?, log_in = ?, log_out = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendance.getEmployeeId());
            stmt.setDate(2, attendance.getDate());
            stmt.setTime(3, attendance.getLogIn());
            stmt.setTime(4, attendance.getLogOut());
            stmt.setInt(5, attendance.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating attendance record", ex);
            throw new RuntimeException("Failed to update attendance record", ex);
        }
    }

    public boolean deleteAttendance(int attendanceId) {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "DELETE FROM attendance WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendanceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting attendance record", ex);
            throw new RuntimeException("Failed to delete attendance record", ex);
        }
    }

    public Attendance getAttendanceById(int attendanceId) {
        if (attendanceId <= 0) {
            throw new IllegalArgumentException("Attendance ID must be positive");
        }

        String query = "SELECT * FROM attendance WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, attendanceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance by ID: " + attendanceId, ex);
            throw new RuntimeException("Failed to retrieve attendance record", ex);
        }

        return null;
    }

    public boolean attendanceExistsForDate(int employeeId, LocalDate attendanceDate) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (attendanceDate == null) {
            throw new IllegalArgumentException("Attendance date cannot be null");
        }

        String query = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(attendanceDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking attendance existence for employee " + employeeId + " on " + attendanceDate, ex);
            throw new RuntimeException("Failed to check attendance existence", ex);
        }

        return false;
    }


    public int countAttendanceDays(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period dates cannot be null");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after end date");
        }

        String query = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= ? AND date <= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error counting attendance days for employee " + employeeId +
                    " between " + periodStart + " and " + periodEnd, ex);
            throw new RuntimeException("Failed to count attendance days", ex);
        }

        return 0;
    }


    public Attendance getAttendanceByEmployeeAndDate(int employeeId, LocalDate attendanceDate) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (attendanceDate == null) {
            throw new IllegalArgumentException("Attendance date cannot be null");
        }

        String query = "SELECT * FROM attendance WHERE employee_id = ? AND date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(attendanceDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving attendance for employee " + employeeId + " on " + attendanceDate, ex);
            throw new RuntimeException("Failed to retrieve attendance record", ex);
        }

        return null;
    }


    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id")); // Using 'id' from database instead of 'attendance_id'
        attendance.setEmployeeId(rs.getInt("employee_id"));
        attendance.setDate(rs.getDate("date"));
        attendance.setLogIn(rs.getTime("log_in")); // Using 'log_in' from database instead of 'login_time'
        attendance.setLogOut(rs.getTime("log_out")); // Using 'log_out' from database instead of 'logout_time'
        return attendance;
    }
}