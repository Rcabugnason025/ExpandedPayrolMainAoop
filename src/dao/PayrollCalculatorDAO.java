package dao;

import util.DBConnection;
import model.Payroll;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PayrollCalculatorDAO {
    private static final Logger logger = Logger.getLogger(PayrollCalculatorDAO.class.getName());

    public boolean saveCalculatedPayroll(Payroll payroll) {
        String query = "INSERT INTO payroll (employee_id, period_start, period_end, monthly_rate, " +
                "days_worked, overtime_hours, gross_pay, total_deductions, net_pay, " +
                "gross_earnings, late_deduction, undertime_deduction, unpaid_leave_deduction, " +
                "overtime_pay, rice_subsidy, phone_allowance, clothing_allowance, " +
                "sss, philhealth, pagibig, tax) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

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

            int result = stmt.executeUpdate();

            if (result > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        payroll.setPayrollId(keys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error saving calculated payroll", ex);
        }

        return false;
    }

    public double calculateTotalEarnings(int employeeId, Date startDate, Date endDate) {
        String query = "SELECT COALESCE(SUM(gross_pay), 0) FROM payroll " +
                "WHERE employee_id = ? AND period_start >= ? AND period_end <= ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error calculating total earnings", ex);
        }

        return 0.0;
    }
}