package model;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

/**
 * Enhanced Payroll model class aligned with DAO and PayrollCalculator usage
 * @author rejoice
 */
public class Payroll {
    private int payrollId;
    private int employeeId;
    private Date periodStart;
    private Date periodEnd;
    private double monthlyRate;
    private int daysWorked;
    private double overtimeHours;
    private double grossPay;
    private double totalDeductions;
    private double netPay;
    
    // Enhanced payroll components to match PayrollCalculator
    private double grossEarnings;
    private double dailyRate;
    private double lateDeduction;
    private double undertimeDeduction;
    private double unpaidLeaveDeduction;
    private double overtimePay;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;
    
    // Additional fields for better tracking
    private int unpaidLeaveCount;
    private double totalOvertimeHours;

    // Constructors
    public Payroll() {}

    public Payroll(int employeeId, Date periodStart, Date periodEnd) {
        this.employeeId = employeeId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // Basic Getters and Setters
    public int getPayrollId() {
        return payrollId;
    }

    public void setPayrollId(int payrollId) {
        this.payrollId = payrollId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        if (periodStart == null) {
            throw new IllegalArgumentException("Period start cannot be null");
        }
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        if (periodEnd == null) {
            throw new IllegalArgumentException("Period end cannot be null");
        }
        if (periodStart != null && periodEnd.before(periodStart)) {
            throw new IllegalArgumentException("Period end cannot be before period start");
        }
        this.periodEnd = periodEnd;
    }

    public double getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(double monthlyRate) {
        if (monthlyRate < 0) {
            throw new IllegalArgumentException("Monthly rate cannot be negative");
        }
        this.monthlyRate = monthlyRate;
        // Auto-calculate daily rate when monthly rate is set
        this.dailyRate = monthlyRate / 22.0; // 22 working days assumption
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        if (dailyRate < 0) {
            throw new IllegalArgumentException("Daily rate cannot be negative");
        }
        this.dailyRate = dailyRate;
    }

    public int getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(int daysWorked) {
        if (daysWorked < 0) {
            throw new IllegalArgumentException("Days worked cannot be negative");
        }
        this.daysWorked = daysWorked;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative");
        }
        this.overtimeHours = overtimeHours;
    }

    public double getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(double totalOvertimeHours) {
        if (totalOvertimeHours < 0) {
            throw new IllegalArgumentException("Total overtime hours cannot be negative");
        }
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public void setGrossPay(double grossPay) {
        if (grossPay < 0) {
            throw new IllegalArgumentException("Gross pay cannot be negative");
        }
        this.grossPay = grossPay;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        if (totalDeductions < 0) {
            throw new IllegalArgumentException("Total deductions cannot be negative");
        }
        this.totalDeductions = totalDeductions;
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double netPay) {
        this.netPay = netPay;
    }

    public int getUnpaidLeaveCount() {
        return unpaidLeaveCount;
    }

    public void setUnpaidLeaveCount(int unpaidLeaveCount) {
        if (unpaidLeaveCount < 0) {
            throw new IllegalArgumentException("Unpaid leave count cannot be negative");
        }
        this.unpaidLeaveCount = unpaidLeaveCount;
    }

    // Earnings component getters and setters
    public double getGrossEarnings() {
        return grossEarnings;
    }

    public void setGrossEarnings(double grossEarnings) {
        if (grossEarnings < 0) {
            throw new IllegalArgumentException("Gross earnings cannot be negative");
        }
        this.grossEarnings = grossEarnings;
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(double overtimePay) {
        if (overtimePay < 0) {
            throw new IllegalArgumentException("Overtime pay cannot be negative");
        }
        this.overtimePay = overtimePay;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public void setRiceSubsidy(double riceSubsidy) {
        if (riceSubsidy < 0) {
            throw new IllegalArgumentException("Rice subsidy cannot be negative");
        }
        this.riceSubsidy = riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        if (phoneAllowance < 0) {
            throw new IllegalArgumentException("Phone allowance cannot be negative");
        }
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) {
            throw new IllegalArgumentException("Clothing allowance cannot be negative");
        }
        this.clothingAllowance = clothingAllowance;
    }

    // Deduction component getters and setters
    public double getLateDeduction() {
        return lateDeduction;
    }

    public void setLateDeduction(double lateDeduction) {
        if (lateDeduction < 0) {
            throw new IllegalArgumentException("Late deduction cannot be negative");
        }
        this.lateDeduction = lateDeduction;
    }

    public double getUndertimeDeduction() {
        return undertimeDeduction;
    }

    public void setUndertimeDeduction(double undertimeDeduction) {
        if (undertimeDeduction < 0) {
            throw new IllegalArgumentException("Undertime deduction cannot be negative");
        }
        this.undertimeDeduction = undertimeDeduction;
    }

    public double getUnpaidLeaveDeduction() {
        return unpaidLeaveDeduction;
    }

    public void setUnpaidLeaveDeduction(double unpaidLeaveDeduction) {
        if (unpaidLeaveDeduction < 0) {
            throw new IllegalArgumentException("Unpaid leave deduction cannot be negative");
        }
        this.unpaidLeaveDeduction = unpaidLeaveDeduction;
    }

    public double getSss() {
        return sss;
    }

    public void setSss(double sss) {
        if (sss < 0) {
            throw new IllegalArgumentException("SSS contribution cannot be negative");
        }
        this.sss = sss;
    }

    public double getPhilhealth() {
        return philhealth;
    }

    public void setPhilhealth(double philhealth) {
        if (philhealth < 0) {
            throw new IllegalArgumentException("PhilHealth contribution cannot be negative");
        }
        this.philhealth = philhealth;
    }

    public double getPagibig() {
        return pagibig;
    }

    public void setPagibig(double pagibig) {
        if (pagibig < 0) {
            throw new IllegalArgumentException("Pag-IBIG contribution cannot be negative");
        }
        this.pagibig = pagibig;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        if (tax < 0) {
            throw new IllegalArgumentException("Tax cannot be negative");
        }
        this.tax = tax;
    }

    // LocalDate compatibility methods - Fixed to work with your PayrollCalculator
    public void setStartDate(Date startDate) {
        this.periodStart = startDate;
    }

    public void setEndDate(Date endDate) {
        this.periodEnd = endDate;
    }

    public void setStartDate(LocalDate startDate) {
        if (startDate != null) {
            this.periodStart = Date.valueOf(startDate);
        }
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate != null) {
            this.periodEnd = Date.valueOf(endDate);
        }
    }

    public LocalDate getStartDateAsLocalDate() {
        return periodStart != null ? periodStart.toLocalDate() : null;
    }

    public LocalDate getEndDateAsLocalDate() {
        return periodEnd != null ? periodEnd.toLocalDate() : null;
    }

    // Enhanced calculation methods
    public void calculateBasicPay() {
        if (dailyRate > 0 && daysWorked >= 0) {
            double basicPay = daysWorked * dailyRate;
            this.grossEarnings = basicPay;
        }
    }

    public void calculateOvertimePay() {
        if (dailyRate > 0 && totalOvertimeHours > 0) {
            double hourlyRate = dailyRate / 8.0; // 8 hours per day
            this.overtimePay = totalOvertimeHours * hourlyRate * 1.25; // 125% of hourly rate
        }
    }

    public void calculateGrossPay() {
        // Calculate gross pay as sum of all earnings
        this.grossPay = grossEarnings + overtimePay + riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public void calculateTotalDeductions() {
        this.totalDeductions = lateDeduction + undertimeDeduction + unpaidLeaveDeduction + 
                              sss + philhealth + pagibig + tax;
    }

    public void calculateNetPay() {
        calculateGrossPay();
        calculateTotalDeductions();
        this.netPay = grossPay - totalDeductions;
    }

    // Utility methods for payroll processing
    public double getTotalEarnings() {
        return grossEarnings + overtimePay + riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public double getTotalGovernmentContributions() {
        return sss + philhealth + pagibig;
    }

    public double getTotalTimeDeductions() {
        return lateDeduction + undertimeDeduction + unpaidLeaveDeduction;
    }

    // Validation method
    public boolean isValid() {
        return employeeId > 0 && 
               periodStart != null && 
               periodEnd != null && 
               !periodEnd.before(periodStart) &&
               grossPay >= 0 && 
               totalDeductions >= 0;
    }

    @Override
    public String toString() {
        return "Payroll{" +
                "payrollId=" + payrollId +
                ", employeeId=" + employeeId +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                ", daysWorked=" + daysWorked +
                ", grossPay=" + grossPay +
                ", totalDeductions=" + totalDeductions +
                ", netPay=" + netPay +
                '}';
    }

    // Detailed toString for debugging
    public String toDetailedString() {
        return "Payroll Details{" +
                "\n  payrollId=" + payrollId +
                "\n  employeeId=" + employeeId +
                "\n  period=" + periodStart + " to " + periodEnd +
                "\n  daysWorked=" + daysWorked +
                "\n  dailyRate=" + dailyRate +
                "\n  grossEarnings=" + grossEarnings +
                "\n  overtimePay=" + overtimePay +
                "\n  allowances=" + (riceSubsidy + phoneAllowance + clothingAllowance) +
                "\n  grossPay=" + grossPay +
                "\n  deductions=" + totalDeductions +
                "\n  netPay=" + netPay +
                "\n}";
    }
}