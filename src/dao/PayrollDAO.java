package dao;

import util.DBConnection;
import model.Payroll;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Payroll operations
 * @author rejoice
 */
public class PayrollDAO {
    private static final Logger logger = Logger.getLogger(PayrollDAO.class.getName());
    
    // SQL Query constants
    private static final String SELECT_BY_EMPLOYEE_ID = 
        "SELECT payroll_id, employee_id, period_start, period_end, monthly_rate, days_worked, " +
        "overtime_hours, gross_pay, total_deductions, net_pay, gross_earnings, late_deduction, " +
        "undertime_deduction, unpaid_leave_deduction, overtime_pay, rice_subsidy, phone_allowance, " +
        "clothing_allowance, sss, philhealth, pagibig, tax FROM payroll WHERE employee_id = ? ORDER BY period_start DESC";
    
    private static final String SELECT_BY_DATE_RANGE = 
        "SELECT payroll_id, employee_id, period_start, period_end, monthly_rate, days_worked, " +
        "overtime_hours, gross_pay, total_deductions, net_pay, gross_earnings, late_deduction, " +
        "undertime_deduction, unpaid_leave_deduction, overtime_pay, rice_subsidy, phone_allowance, " +
        "clothing_allowance, sss, philhealth, pagibig, tax FROM payroll " +
        "WHERE employee_id = ? AND period_start >= ? AND period_end <= ? ORDER BY period_start DESC";
    
    private static final String INSERT_PAYROLL = 
        "INSERT INTO payroll (employee_id, period_start, period_end, monthly_rate, days_worked, " +
        "overtime_hours, gross_pay, total_deductions, net_pay, gross_earnings, late_deduction, " +
        "undertime_deduction, unpaid_leave_deduction, overtime_pay, rice_subsidy, phone_allowance, " +
        "clothing_allowance, sss, philhealth, pagibig, tax) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_PAYROLL = 
        "UPDATE payroll SET employee_id = ?, period_start = ?, period_end = ?, monthly_rate = ?, " +
        "days_worked = ?, overtime_hours = ?, gross_pay = ?, total_deductions = ?, net_pay = ?, " +
        "gross_earnings = ?, late_deduction = ?, undertime_deduction = ?, unpaid_leave_deduction = ?, " +
        "overtime_pay = ?, rice_subsidy = ?, phone_allowance = ?, clothing_allowance = ?, " +
        "sss = ?, philhealth = ?, pagibig = ?, tax = ? WHERE payroll_id = ?";
    
    private static final String DELETE_PAYROLL = "DELETE FROM payroll WHERE payroll_id = ?";
    
    private static final String SELECT_BY_ID = 
        "SELECT payroll_id, employee_id, period_start, period_end, monthly_rate, days_worked, " +
        "overtime_hours, gross_pay, total_deductions, net_pay, gross_earnings, late_deduction, " +
        "undertime_deduction, unpaid_leave_deduction, overtime_pay, rice_subsidy, phone_allowance, " +
        "clothing_allowance, sss, philhealth, pagibig, tax FROM payroll WHERE payroll_id = ?";

    /**
     * Retrieves all payroll records for a specific employee
     * @param empId Employee ID
     * @return List of payroll records
     */
    public List<Payroll> getPayrollByEmployeeId(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        
        List<Payroll> payrollList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {

            stmt.setInt(1, empId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrollList.add(payroll);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving payroll for employee ID: " + empId, ex);
            throw new RuntimeException("Failed to retrieve payroll records", ex);
        }

        return payrollList;
    }

    /**
     * Retrieves payroll records for an employee within a date range
     * @param employeeId Employee ID
     * @param periodStart Start date
     * @param periodEnd End date
     * @return List of payroll records
     */
    public List<Payroll> getPayrollByEmployeeIdAndDateRange(int employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Period dates cannot be null");
        }
        
        List<Payroll> payrollList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DATE_RANGE)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, java.sql.Date.valueOf(periodStart));
            stmt.setDate(3, java.sql.Date.valueOf(periodEnd));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrollList.add(payroll);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving payroll records for date range", ex);
            throw new RuntimeException("Failed to retrieve payroll records", ex);
        }

        return payrollList;
    }

    /**
     * Inserts a new payroll record
     * @param payroll Payroll object to insert
     * @return Generated payroll ID
     */
    public int insertPayroll(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PAYROLL, Statement.RETURN_GENERATED_KEYS)) {
            
            setPayrollParameters(stmt, payroll);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating payroll failed, no rows affected");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    payroll.setPayrollId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Creating payroll failed, no ID obtained");
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting payroll record", ex);
            throw new RuntimeException("Failed to insert payroll record", ex);
        }
    }

    /**
     * Updates an existing payroll record
     * @param payroll Payroll object with updated information
     * @return true if update was successful
     */
    public boolean updatePayroll(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }
        if (payroll.getPayrollId() <= 0) {
            throw new IllegalArgumentException("Payroll ID must be positive");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PAYROLL)) {
            
            setPayrollParameters(stmt, payroll);
            stmt.setInt(22, payroll.getPayrollId()); // Set payroll_id for WHERE clause
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating payroll record", ex);
            throw new RuntimeException("Failed to update payroll record", ex);
        }
    }

    /**
     * Deletes a payroll record
     * @param payrollId Payroll ID to delete
     * @return true if deletion was successful
     */
    public boolean deletePayroll(int payrollId) {
        if (payrollId <= 0) {
            throw new IllegalArgumentException("Payroll ID must be positive");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PAYROLL)) {
            
            stmt.setInt(1, payrollId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting payroll record", ex);
            throw new RuntimeException("Failed to delete payroll record", ex);
        }
    }

    /**
     * Retrieves a payroll record by ID
     * @param payrollId Payroll ID
     * @return Payroll object or null if not found
     */
    public Payroll getPayrollById(int payrollId) {
        if (payrollId <= 0) {
            throw new IllegalArgumentException("Payroll ID must be positive");
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            
            stmt.setInt(1, payrollId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayroll(rs);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving payroll by ID", ex);
            throw new RuntimeException("Failed to retrieve payroll record", ex);
        }
        
        return null;
    }

    /**
     * Sets payroll parameters for PreparedStatement
     * @param stmt PreparedStatement
     * @param payroll Payroll object
     * @throws SQLException if database error occurs
     */
    private void setPayrollParameters(PreparedStatement stmt, Payroll payroll) throws SQLException {
        stmt.setInt(1, payroll.getEmployeeId());
        stmt.setDate(2, payroll.getPeriodStart());
        stmt.setDate(3, payroll.getPeriodEnd());
        stmt.setDouble(4, payroll.getMonthlyRate());
        stmt.setInt(5, payroll.getDaysWorked());
        stmt.setDouble(6, payroll.getOvertimeHours());
        stmt.setDouble(7, payroll.getGrossPay());
        stmt.setDouble(8, payroll.getTotalDeductions());
        stmt.setDouble(9, payroll.getNetPay());
        stmt.setDouble(10, payroll.getGrossEarnings());
        stmt.setDouble(11, payroll.getLateDeduction());
        stmt.setDouble(12, payroll.getUndertimeDeduction());
        stmt.setDouble(13, payroll.getUnpaidLeaveDeduction());
        stmt.setDouble(14, payroll.getOvertimePay());
        stmt.setDouble(15, payroll.getRiceSubsidy());
        stmt.setDouble(16, payroll.getPhoneAllowance());
        stmt.setDouble(17, payroll.getClothingAllowance());
        stmt.setDouble(18, payroll.getSss());
        stmt.setDouble(19, payroll.getPhilhealth());
        stmt.setDouble(20, payroll.getPagibig());
        stmt.setDouble(21, payroll.getTax());
    }

    /**
     * Maps ResultSet to Payroll object
     * @param rs ResultSet from database
     * @return Payroll object
     * @throws SQLException if database access error occurs
     */
    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException {
        Payroll payroll = new Payroll();
        payroll.setPayrollId(rs.getInt("payroll_id"));
        payroll.setEmployeeId(rs.getInt("employee_id"));
        payroll.setPeriodStart(rs.getDate("period_start"));
        payroll.setPeriodEnd(rs.getDate("period_end"));
        payroll.setMonthlyRate(rs.getDouble("monthly_rate"));
        payroll.setDaysWorked(rs.getInt("days_worked"));
        payroll.setOvertimeHours(rs.getDouble("overtime_hours"));
        payroll.setGrossPay(rs.getDouble("gross_pay"));
        payroll.setTotalDeductions(rs.getDouble("total_deductions"));
        payroll.setNetPay(rs.getDouble("net_pay"));
        payroll.setGrossEarnings(rs.getDouble("gross_earnings"));
        payroll.setLateDeduction(rs.getDouble("late_deduction"));
        payroll.setUndertimeDeduction(rs.getDouble("undertime_deduction"));
        payroll.setUnpaidLeaveDeduction(rs.getDouble("unpaid_leave_deduction"));
        payroll.setOvertimePay(rs.getDouble("overtime_pay"));
        payroll.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        payroll.setPhoneAllowance(rs.getDouble("phone_allowance"));
        payroll.setClothingAllowance(rs.getDouble("clothing_allowance"));
        payroll.setSss(rs.getDouble("sss"));
        payroll.setPhilhealth(rs.getDouble("philhealth"));
        payroll.setPagibig(rs.getDouble("pagibig"));
        payroll.setTax(rs.getDouble("tax"));
        return payroll;
    }
}