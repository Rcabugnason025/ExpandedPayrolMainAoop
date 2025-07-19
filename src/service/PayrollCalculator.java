package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import dao.OvertimeDAO;
import dao.DeductionDAO;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.Overtime;
import model.Payroll;
import model.Deduction;

import java.sql.Date;
import java.sql.Time;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PayrollCalculator {

    private static final Logger LOGGER = Logger.getLogger(PayrollCalculator.class.getName());

    // Constants for payroll calculations
    private static final int STANDARD_WORKING_DAYS_PER_MONTH = 22;
    private static final int STANDARD_WORKING_HOURS_PER_DAY = 8;
    private static final double OVERTIME_RATE_MULTIPLIER = 1.25;
    private static final LocalTime STANDARD_LOGIN_TIME = LocalTime.of(8, 0);
    private static final LocalTime LATE_THRESHOLD_TIME = LocalTime.of(8, 15);
    private static final LocalTime STANDARD_LOGOUT_TIME = LocalTime.of(17, 0);

    // DAO instances
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;
    private LeaveRequestDAO leaveDAO;
    private OvertimeDAO overtimeDAO;
    private DeductionDAO deductionDAO;

    public PayrollCalculator() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();

        // Initialize optional DAOs with proper error handling
        try {
            this.leaveDAO = new LeaveRequestDAO();
            LOGGER.info("LeaveRequestDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("LeaveRequestDAO not available - leave calculations will be skipped: " + e.getMessage());
            this.leaveDAO = null;
        }

        try {
            this.overtimeDAO = new OvertimeDAO();
            LOGGER.info("OvertimeDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("OvertimeDAO not available - overtime calculations will be skipped: " + e.getMessage());
            this.overtimeDAO = null;
        }

        try {
            this.deductionDAO = new DeductionDAO();
            LOGGER.info("DeductionDAO initialized successfully");
        } catch (Exception e) {
            LOGGER.warning("DeductionDAO not available - deduction records will not be saved: " + e.getMessage());
            this.deductionDAO = null;
        }
    }

    /**
     * Calculate comprehensive payroll for an employee within a specific period
     * Main fix is in the attendance-based earnings calculation
     */
    public Payroll calculatePayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {

        try {
            validateInputs(employeeId, periodStart, periodEnd);

            // Get employee information
            Employee employee = getEmployeeWithValidation(employeeId);

            // Initialize payroll object
            Payroll payroll = new Payroll(employeeId, Date.valueOf(periodStart), Date.valueOf(periodEnd));

            // Set basic salary information from employee record
            double monthlySalary = employee.getBasicSalary();
            double dailyRate = calculateDailyRate(monthlySalary);

            payroll.setMonthlyRate(monthlySalary);
            payroll.setDailyRate(dailyRate);

            // Calculate attendance-based earnings with better debugging
            calculateAttendanceBasedEarningsFixed(payroll, employeeId, periodStart, periodEnd, dailyRate);

            // Calculate overtime earnings (if overtime table exists)
            calculateOvertimeEarnings(payroll, employeeId, periodStart, periodEnd, dailyRate);

            // Calculate allowances and benefits (from employee record)
            calculateAllowancesAndBenefits(payroll, employee);

            // Calculate time-based deductions
            calculateTimeBasedDeductions(payroll, employeeId, periodStart, periodEnd, dailyRate);

            // Calculate government contributions and tax
            calculateGovernmentContributionsAndTax(payroll, monthlySalary);

            // Final calculations
            payroll.calculateGrossPay();
            payroll.calculateTotalDeductions();
            payroll.calculateNetPay();

            // Validate final payroll
            validatePayroll(payroll);

            // Log detailed calculation results
            LOGGER.info(String.format("=== PAYROLL CALCULATION SUMMARY ==="));
            LOGGER.info(String.format("Employee ID: %d", employeeId));
            LOGGER.info(String.format("Period: %s to %s", periodStart, periodEnd));
            LOGGER.info(String.format("Days Worked: %d", payroll.getDaysWorked()));
            LOGGER.info(String.format("Daily Rate: %.2f", dailyRate));
            LOGGER.info(String.format("Basic Pay: %.2f", payroll.getGrossEarnings()));
            LOGGER.info(String.format("Gross Pay: %.2f", payroll.getGrossPay()));
            LOGGER.info(String.format("Net Pay: %.2f", payroll.getNetPay()));
            LOGGER.info(String.format("====================================="));

            return payroll;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to calculate payroll for employee %d", employeeId), e);
            throw new PayrollCalculationException("Failed to calculate payroll: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate attendance-based earnings using actual attendance data
     * This method has been enhanced to properly handle the days worked calculation
     */
    private void calculateAttendanceBasedEarningsFixed(Payroll payroll, int employeeId,
                                                       LocalDate periodStart, LocalDate periodEnd, double dailyRate) {

        LOGGER.info(String.format("=== ATTENDANCE CALCULATION DEBUG ==="));
        LOGGER.info(String.format("Employee ID: %d", employeeId));
        LOGGER.info(String.format("Period: %s to %s", periodStart, periodEnd));
        LOGGER.info(String.format("Daily Rate: %.2f", dailyRate));

        try {
            // Get attendance data with enhanced logging
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                    employeeId, periodStart, periodEnd);

            LOGGER.info(String.format("Raw attendance records found: %d", attendanceList.size()));

            // Filter valid attendance records (both log in and log out present)
            int validAttendanceDays = 0;
            double totalValidHours = 0.0;

            for (Attendance attendance : attendanceList) {
                LOGGER.info(String.format("Processing attendance: Date=%s, LogIn=%s, LogOut=%s",
                        attendance.getDate(),
                        attendance.getLogIn(),
                        attendance.getLogOut()));

                // Only count days with valid log in time (log out can be optional)
                if (attendance.getLogIn() != null) {
                    validAttendanceDays++;

                    // Calculate work hours for this day
                    double workHours = attendance.getWorkHours();
                    totalValidHours += workHours;

                    LOGGER.info(String.format("Valid attendance day #%d: %.2f hours",
                            validAttendanceDays, workHours));
                } else {
                    LOGGER.warning(String.format("Invalid attendance record (no log in): %s",
                            attendance.getDate()));
                }
            }

            // Set the correct days worked
            payroll.setDaysWorked(validAttendanceDays);

            // Calculate basic pay based on actual days worked
            double basicPay = validAttendanceDays * dailyRate;
            payroll.setGrossEarnings(basicPay);

            LOGGER.info(String.format("FINAL ATTENDANCE CALCULATION:"));
            LOGGER.info(String.format("- Total attendance records: %d", attendanceList.size()));
            LOGGER.info(String.format("- Valid attendance days: %d", validAttendanceDays));
            LOGGER.info(String.format("- Total valid hours: %.2f", totalValidHours));
            LOGGER.info(String.format("- Daily rate: %.2f", dailyRate));
            LOGGER.info(String.format("- Basic pay: %.2f", basicPay));
            LOGGER.info(String.format("===================================="));

            // Handle edge cases
            if (validAttendanceDays == 0) {
                LOGGER.warning(String.format("No valid attendance found for employee %d in period %s to %s",
                        employeeId, periodStart, periodEnd));

                // Still show available attendance data for debugging
                LOGGER.info("Available attendance data for this employee:");
                List<Attendance> allAttendance = attendanceDAO.getAttendanceByEmployeeId(employeeId);
                for (Attendance att : allAttendance) {
                    LOGGER.info(String.format("- Date: %s, LogIn: %s, LogOut: %s",
                            att.getDate(), att.getLogIn(), att.getLogOut()));
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating attendance-based earnings", e);
            // Set defaults in case of error
            payroll.setDaysWorked(0);
            payroll.setGrossEarnings(0.0);

            // Re-throw to let caller handle the error
            throw new RuntimeException("Failed to calculate attendance-based earnings: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate daily rate from monthly salary
     */
    private double calculateDailyRate(double monthlySalary) {
        return monthlySalary / STANDARD_WORKING_DAYS_PER_MONTH;
    }

    /**
     * Calculate hourly rate from daily rate
     */
    private double calculateHourlyRate(double dailyRate) {
        return dailyRate / STANDARD_WORKING_HOURS_PER_DAY;
    }

    /**
     * Calculate overtime earnings with better error handling
     */
    private void calculateOvertimeEarnings(Payroll payroll, int employeeId,
                                           LocalDate periodStart, LocalDate periodEnd, double dailyRate) {

        if (overtimeDAO == null) {
            LOGGER.info("Overtime table not available, setting overtime pay to 0");
            payroll.setTotalOvertimeHours(0.0);
            payroll.setOvertimePay(0.0);
            return;
        }

        try {
            List<Overtime> overtimeList = overtimeDAO.getOvertimeByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            double totalOvertimeHours = overtimeList.stream()
                    .filter(Overtime::isApproved) // Only count approved overtime
                    .mapToDouble(Overtime::getHours)
                    .sum();

            double hourlyRate = calculateHourlyRate(dailyRate);
            double overtimePay = totalOvertimeHours * hourlyRate * OVERTIME_RATE_MULTIPLIER;

            payroll.setTotalOvertimeHours(totalOvertimeHours);
            payroll.setOvertimePay(overtimePay);

            LOGGER.info(String.format("Employee %d overtime: %.2f hours, pay: %.2f",
                    employeeId, totalOvertimeHours, overtimePay));
        } catch (Exception e) {
            LOGGER.warning("Error accessing overtime data, setting overtime pay to 0: " + e.getMessage());
            payroll.setTotalOvertimeHours(0.0);
            payroll.setOvertimePay(0.0);
        }
    }

    /**
     * Calculate allowances and benefits from employee record
     */
    private void calculateAllowancesAndBenefits(Payroll payroll, Employee employee) {
        payroll.setRiceSubsidy(employee.getRiceSubsidy());
        payroll.setPhoneAllowance(employee.getPhoneAllowance());
        payroll.setClothingAllowance(employee.getClothingAllowance());

        LOGGER.info(String.format("Employee %d allowances - Rice: %.2f, Phone: %.2f, Clothing: %.2f",
                employee.getEmployeeId(), employee.getRiceSubsidy(),
                employee.getPhoneAllowance(), employee.getClothingAllowance()));
    }

    /**
     * Calculate time-based deductions with better error handling
     */
    private void calculateTimeBasedDeductions(Payroll payroll, int employeeId,
                                              LocalDate periodStart, LocalDate periodEnd, double dailyRate) {

        // Get attendance records
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employeeId, periodStart, periodEnd);

        double lateDeduction = calculateLateDeduction(attendanceList, dailyRate);
        double undertimeDeduction = calculateUndertimeDeduction(attendanceList, dailyRate);

        payroll.setLateDeduction(lateDeduction);
        payroll.setUndertimeDeduction(undertimeDeduction);

        // Calculate unpaid leave deduction with better error handling
        double unpaidLeaveDeduction = 0.0;
        int unpaidLeaveCount = 0;

        if (leaveDAO != null) {
            try {
                List<LeaveRequest> approvedLeaves = leaveDAO.getApprovedLeavesByEmployeeIdAndDateRange(
                        employeeId, periodStart, periodEnd);

                unpaidLeaveCount = (int) approvedLeaves.stream()
                        .filter(leave -> "Unpaid".equalsIgnoreCase(leave.getLeaveType()))
                        .mapToLong(LeaveRequest::getLeaveDays)
                        .sum();

                unpaidLeaveDeduction = unpaidLeaveCount * dailyRate;

                LOGGER.info(String.format("Employee %d unpaid leave: %d days, deduction: %.2f",
                        employeeId, unpaidLeaveCount, unpaidLeaveDeduction));
            } catch (Exception e) {
                LOGGER.warning("Error accessing leave request data: " + e.getMessage());
            }
        } else {
            LOGGER.info("Leave request table not available, setting unpaid leave deduction to 0");
        }

        payroll.setUnpaidLeaveCount(unpaidLeaveCount);
        payroll.setUnpaidLeaveDeduction(unpaidLeaveDeduction);

        // Save deductions into the database with better error handling
        if (deductionDAO != null) {
            try {
                saveDeductionRecords(employeeId, periodStart, periodEnd, lateDeduction, undertimeDeduction, unpaidLeaveDeduction);
            } catch (Exception e) {
                LOGGER.warning(String.format("Could not save deduction records for employee %d: %s", employeeId, e.getMessage()));
            }
        } else {
            LOGGER.info("Deduction table not available, skipping deduction record saving");
        }

        LOGGER.info(String.format("Employee %d deductions - Late: %.2f, Undertime: %.2f, Unpaid Leave: %.2f",
                employeeId, lateDeduction, undertimeDeduction, unpaidLeaveDeduction));
    }

    /**
     * Helper method to save deduction records with better error handling
     */
    private void saveDeductionRecords(int employeeId, LocalDate periodStart, LocalDate periodEnd,
                                      double lateDeduction, double undertimeDeduction, double unpaidLeaveDeduction) {
        try {
            if (lateDeduction > 0) {
                SimpleDeduction lateDeductionRecord = new SimpleDeduction(
                        employeeId, "Late", lateDeduction,
                        "Late arrival deduction for period " + periodStart + " to " + periodEnd);
                lateDeductionRecord.setDeductionDate(Date.valueOf(periodEnd));
                try {
                    deductionDAO.addDeduction(lateDeductionRecord);
                } catch (SQLException e) {
                    LOGGER.warning("Failed to save late deduction record: " + e.getMessage());
                }
            }

            if (undertimeDeduction > 0) {
                SimpleDeduction undertimeDeductionRecord = new SimpleDeduction(
                        employeeId, "Undertime", undertimeDeduction,
                        "Undertime deduction for period " + periodStart + " to " + periodEnd);
                undertimeDeductionRecord.setDeductionDate(Date.valueOf(periodEnd));
                try {
                    deductionDAO.addDeduction(undertimeDeductionRecord);
                } catch (SQLException e) {
                    LOGGER.warning("Failed to save undertime deduction record: " + e.getMessage());
                }
            }

            if (unpaidLeaveDeduction > 0) {
                SimpleDeduction unpaidDeductionRecord = new SimpleDeduction(
                        employeeId, "UnpaidLeave", unpaidLeaveDeduction,
                        "Unpaid leave deduction for period " + periodStart + " to " + periodEnd);
                unpaidDeductionRecord.setDeductionDate(Date.valueOf(periodEnd));
                try {
                    deductionDAO.addDeduction(unpaidDeductionRecord);
                } catch (SQLException e) {
                    LOGGER.warning("Failed to save unpaid leave deduction record: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error saving deduction records: " + e.getMessage());
        }
    }

    /**
     * Simple concrete implementation of Deduction for database storage
     */
    private static class SimpleDeduction extends Deduction {
        public SimpleDeduction(int employeeId, String type, double amount, String description) {
            super(employeeId, type, amount, description);
        }

        @Override
        public void calculateDeduction() {
            // Already calculated, no additional calculation needed
        }
    }

    /**
     * Calculate government contributions and tax using standard rates
     */
    private void calculateGovernmentContributionsAndTax(Payroll payroll, double monthlySalary) {
        double sss = calculateSSSContribution(monthlySalary);
        double philhealth = calculatePhilHealthContribution(monthlySalary);
        double pagibig = calculatePagIBIGContribution(monthlySalary);
        double tax = calculateIncomeTax(monthlySalary);

        payroll.setSss(sss);
        payroll.setPhilhealth(philhealth);
        payroll.setPagibig(pagibig);
        payroll.setTax(tax);

        LOGGER.info(String.format("Employee %d contributions - SSS: %.2f, PhilHealth: %.2f, Pag-IBIG: %.2f, Tax: %.2f",
                payroll.getEmployeeId(), sss, philhealth, pagibig, tax));
    }

    /**
     * Enhanced late deduction calculation using actual log_in times
     */
    private double calculateLateDeduction(List<Attendance> attendanceList, double dailyRate) {
        double totalLateDeduction = 0.0;
        double hourlyRate = calculateHourlyRate(dailyRate);

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                LocalTime loginTime = attendance.getLogIn().toLocalTime();

                if (loginTime.isAfter(LATE_THRESHOLD_TIME)) {
                    long minutesLate = ChronoUnit.MINUTES.between(STANDARD_LOGIN_TIME, loginTime);
                    double hoursLate = minutesLate / 60.0;
                    totalLateDeduction += hoursLate * hourlyRate;
                }
            }
        }

        return totalLateDeduction;
    }

    /**
     * Enhanced undertime deduction calculation using actual log_out times
     */
    private double calculateUndertimeDeduction(List<Attendance> attendanceList, double dailyRate) {
        double totalUndertimeDeduction = 0.0;
        double hourlyRate = calculateHourlyRate(dailyRate);

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogOut() != null) {
                LocalTime logoutTime = attendance.getLogOut().toLocalTime();

                if (logoutTime.isBefore(STANDARD_LOGOUT_TIME)) {
                    long minutesShort = ChronoUnit.MINUTES.between(logoutTime, STANDARD_LOGOUT_TIME);
                    double hoursShort = minutesShort / 60.0;
                    totalUndertimeDeduction += hoursShort * hourlyRate;
                }
            }
        }

        return totalUndertimeDeduction;
    }

    // Government contribution calculation methods remain the same...
    private double calculateSSSContribution(double monthlySalary) {
        if (monthlySalary <= 4000) return 180.00;
        if (monthlySalary <= 4750) return 202.50;
        if (monthlySalary <= 5500) return 225.00;
        if (monthlySalary <= 6250) return 247.50;
        if (monthlySalary <= 7000) return 270.00;
        if (monthlySalary <= 7750) return 292.50;
        if (monthlySalary <= 8500) return 315.00;
        if (monthlySalary <= 9250) return 337.50;
        if (monthlySalary <= 10000) return 360.00;
        if (monthlySalary <= 15000) return 540.00;
        if (monthlySalary <= 20000) return 720.00;
        if (monthlySalary <= 25000) return 900.00;
        return 1125.00;
    }

    private double calculatePhilHealthContribution(double monthlySalary) {
        double rate = 0.05;
        double employeeShare = (monthlySalary * rate) / 2;
        double minContribution = 500.00;
        double maxContribution = 5000.00;

        if (employeeShare < minContribution) return minContribution;
        if (employeeShare > maxContribution) return maxContribution;
        return employeeShare;
    }

    private double calculatePagIBIGContribution(double monthlySalary) {
        if (monthlySalary <= 1500) {
            return monthlySalary * 0.01;
        } else {
            double contribution = monthlySalary * 0.02;
            return Math.min(contribution, 200.00);
        }
    }

    private double calculateIncomeTax(double monthlySalary) {
        double annualSalary = monthlySalary * 12;
        double annualTax = 0.0;

        if (annualSalary <= 250000) {
            annualTax = 0.0;
        } else if (annualSalary <= 400000) {
            annualTax = (annualSalary - 250000) * 0.15;
        } else if (annualSalary <= 800000) {
            annualTax = 22500 + (annualSalary - 400000) * 0.20;
        } else if (annualSalary <= 2000000) {
            annualTax = 102500 + (annualSalary - 800000) * 0.25;
        } else if (annualSalary <= 8000000) {
            annualTax = 402500 + (annualSalary - 2000000) * 0.30;
        } else {
            annualTax = 2202500 + (annualSalary - 8000000) * 0.35;
        }

        return annualTax / 12;
    }

    // Validation methods remain the same...
    private void validateInputs(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {
        if (employeeId <= 0) {
            throw new PayrollCalculationException("Invalid employee ID: " + employeeId);
        }
        if (periodStart == null || periodEnd == null) {
            throw new PayrollCalculationException("Period dates cannot be null");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new PayrollCalculationException("Period end cannot be before period start");
        }
    }

    private Employee getEmployeeWithValidation(int employeeId) throws PayrollCalculationException {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            throw new PayrollCalculationException("Employee not found with ID: " + employeeId);
        }

        if (employee.getBasicSalary() <= 0) {
            throw new PayrollCalculationException("Employee " + employeeId + " has invalid basic salary");
        }

        return employee;
    }

    private void validatePayroll(Payroll payroll) throws PayrollCalculationException {
        if (!payroll.isValid()) {
            throw new PayrollCalculationException("Invalid payroll calculation result");
        }
        if (payroll.getNetPay() < 0) {
            LOGGER.warning(String.format("Negative net pay detected for employee %d: %.2f",
                    payroll.getEmployeeId(), payroll.getNetPay()));
        }

        if (payroll.getGrossPay() < 0) {
            throw new PayrollCalculationException("Gross pay cannot be negative");
        }
        if (payroll.getTotalDeductions() < 0) {
            throw new PayrollCalculationException("Total deductions cannot be negative");
        }
    }

    /**
     * Custom exception for payroll calculation errors
     */
    public static class PayrollCalculationException extends Exception {
        public PayrollCalculationException(String message) {
            super(message);
        }

        public PayrollCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}