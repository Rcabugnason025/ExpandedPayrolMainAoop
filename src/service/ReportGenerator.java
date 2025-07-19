// ReportGenerator.java - Complete Implementation
package service;

import dao.*;
import model.*;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReportGenerator {
    private static final Logger logger = Logger.getLogger(ReportGenerator.class.getName());

    private final EmployeeDAO employeeDAO;
    private final PayrollDAO payrollDAO;
    private final AttendanceDAO attendanceDAO;
    private final PayrollCalculator payrollCalculator;

    public ReportGenerator() {
        this.employeeDAO = new EmployeeDAO();
        this.payrollDAO = new PayrollDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.payrollCalculator = new PayrollCalculator();
    }

    /**
     * Generate monthly payroll report for all employees
     */
    public PayrollReport generateMonthlyPayrollReport(LocalDate month, String generatedBy) {
        LocalDate periodStart = month.withDayOfMonth(1);
        LocalDate periodEnd = month.withDayOfMonth(month.lengthOfMonth());

        PayrollReport report = new PayrollReport(
                "Monthly Payroll Report - " + month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                PayrollReport.ReportType.MONTHLY_PAYROLL,
                generatedBy
        );

        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            List<Payroll> payrollData = new ArrayList<>();

            for (Employee emp : employees) {
                try {
                    Payroll payroll = payrollCalculator.calculatePayroll(
                            emp.getEmployeeId(), periodStart, periodEnd);
                    payrollData.add(payroll);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error calculating payroll for employee " + emp.getEmployeeId(), e);
                }
            }

            report.setPayrollData(payrollData);
            report.setSummary(generateReportSummary(payrollData));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating monthly payroll report", e);
            throw new RuntimeException("Failed to generate monthly payroll report", e);
        }

        return report;
    }

    /**
     * Generate payroll report for specific employee
     */
    public PayrollReport generateEmployeePayrollReport(int employeeId, LocalDate periodStart, LocalDate periodEnd, String generatedBy) {
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }

        PayrollReport report = new PayrollReport(
                "Employee Payroll Report - " + employee.getFullName(),
                PayrollReport.ReportType.EMPLOYEE_PAYROLL,
                generatedBy
        );

        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);

        try {
            List<Payroll> existingPayrolls = payrollDAO.getPayrollByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            if (existingPayrolls.isEmpty()) {
                // Calculate new payroll
                Payroll payroll = payrollCalculator.calculatePayroll(employeeId, periodStart, periodEnd);
                existingPayrolls = Arrays.asList(payroll);
            }

            report.setPayrollData(existingPayrolls);
            report.setSummary(generateReportSummary(existingPayrolls));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating employee payroll report", e);
            throw new RuntimeException("Failed to generate employee payroll report", e);
        }

        return report;
    }

    /**
     * Generate attendance summary report
     */
    public AttendanceReport generateAttendanceSummaryReport(LocalDate periodStart, LocalDate periodEnd, String generatedBy) {
        AttendanceReport report = new AttendanceReport();
        report.setReportTitle("Attendance Summary Report");
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setGeneratedBy(generatedBy);
        report.setGeneratedDate(LocalDate.now());

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            Map<Integer, List<Attendance>> attendanceMap = new HashMap<>();

            for (Employee emp : employees) {
                List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                        emp.getEmployeeId(), periodStart, periodEnd);
                attendanceMap.put(emp.getEmployeeId(), attendanceList);
            }

            report.setAttendanceData(attendanceMap);
            report.calculateSummaryStatistics();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating attendance summary report", e);
            throw new RuntimeException("Failed to generate attendance summary report", e);
        }

        return report;
    }

    /**
     * Generate government contributions report
     */
    public PayrollReport generateGovernmentContributionsReport(LocalDate month, String generatedBy) {
        PayrollReport report = generateMonthlyPayrollReport(month, generatedBy);
        report.setReportType(PayrollReport.ReportType.GOVERNMENT_CONTRIBUTIONS);
        report.setReportTitle("Government Contributions Report - " +
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        return report;
    }

    /**
     * Export report to CSV format
     */
    public void exportReportToCSV(PayrollReport report, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filePath)))) {
            // Write header
            writer.println("Employee ID,Name,Period,Gross Pay,Deductions,Net Pay,SSS,PhilHealth,Pag-IBIG,Tax");

            // Write data
            for (Payroll payroll : report.getPayrollData()) {
                Employee emp = employeeDAO.getEmployeeById(payroll.getEmployeeId());
                writer.printf("%d,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                        payroll.getEmployeeId(),
                        emp != null ? emp.getFullName() : "Unknown",
                        report.getFormattedPeriod(),
                        payroll.getGrossPay(),
                        payroll.getTotalDeductions(),
                        payroll.getNetPay(),
                        payroll.getSss(),
                        payroll.getPhilhealth(),
                        payroll.getPagibig(),
                        payroll.getTax()
                );
            }

            // Write summary
            writer.println();
            writer.println("SUMMARY");
            writer.printf("Total Employees,%d%n", report.getTotalEmployees());
            writer.printf("Total Gross Pay,%.2f%n", report.getTotalGrossPay());
            writer.printf("Total Deductions,%.2f%n", report.getTotalDeductions());
            writer.printf("Total Net Pay,%.2f%n", report.getTotalNetPay());
        }
    }

    /**
     * Export report to HTML format
     */
    public void exportReportToHTML(PayrollReport report, String filePath) throws IOException {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>").append(report.getReportTitle()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append(".summary { margin-top: 20px; background-color: #f9f9f9; padding: 15px; }");
        html.append("</style></head><body>");

        // Header
        html.append("<h1>").append(report.getReportTitle()).append("</h1>");
        html.append("<p>Generated on: ").append(report.getGeneratedDate()).append("</p>");
        html.append("<p>Period: ").append(report.getFormattedPeriod()).append("</p>");
        html.append("<p>Generated by: ").append(report.getGeneratedBy()).append("</p>");

        // Table
        html.append("<table>");
        html.append("<tr><th>Employee ID</th><th>Name</th><th>Gross Pay</th><th>Deductions</th><th>Net Pay</th></tr>");

        for (Payroll payroll : report.getPayrollData()) {
            Employee emp = employeeDAO.getEmployeeById(payroll.getEmployeeId());
            html.append("<tr>");
            html.append("<td>").append(payroll.getEmployeeId()).append("</td>");
            html.append("<td>").append(emp != null ? emp.getFullName() : "Unknown").append("</td>");
            html.append("<td>₱").append(String.format("%.2f", payroll.getGrossPay())).append("</td>");
            html.append("<td>₱").append(String.format("%.2f", payroll.getTotalDeductions())).append("</td>");
            html.append("<td>₱").append(String.format("%.2f", payroll.getNetPay())).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        // Summary
        html.append("<div class='summary'>");
        html.append("<h3>Summary</h3>");
        html.append("<p>Total Employees: ").append(report.getTotalEmployees()).append("</p>");
        html.append("<p>Total Gross Pay: ₱").append(String.format("%.2f", report.getTotalGrossPay())).append("</p>");
        html.append("<p>Total Deductions: ₱").append(String.format("%.2f", report.getTotalDeductions())).append("</p>");
        html.append("<p>Total Net Pay: ₱").append(String.format("%.2f", report.getTotalNetPay())).append("</p>");
        html.append("</div>");

        html.append("</body></html>");

        Files.write(Paths.get(filePath), html.toString().getBytes());
    }

    /**
     * Generate report summary from payroll data
     */
    private PayrollReport.ReportSummary generateReportSummary(List<Payroll> payrollData) {
        PayrollReport.ReportSummary summary = new PayrollReport.ReportSummary();

        if (payrollData == null || payrollData.isEmpty()) {
            return summary;
        }

        summary.setTotalEmployees(payrollData.size());
        summary.setTotalGrossPay(payrollData.stream().mapToDouble(Payroll::getGrossPay).sum());
        summary.setTotalDeductions(payrollData.stream().mapToDouble(Payroll::getTotalDeductions).sum());
        summary.setTotalNetPay(payrollData.stream().mapToDouble(Payroll::getNetPay).sum());
        summary.setTotalSSSContributions(payrollData.stream().mapToDouble(Payroll::getSss).sum());
        summary.setTotalPhilHealthContributions(payrollData.stream().mapToDouble(Payroll::getPhilhealth).sum());
        summary.setTotalPagIBIGContributions(payrollData.stream().mapToDouble(Payroll::getPagibig).sum());
        summary.setTotalTax(payrollData.stream().mapToDouble(Payroll::getTax).sum());

        return summary;
    }

    /**
     * Generate daily attendance report
     */
    public String generateDailyAttendanceReport(LocalDate date) {
        StringBuilder report = new StringBuilder();

        try {
            List<Employee> employees = employeeDAO.getAllEmployees();

            report.append("DAILY ATTENDANCE REPORT\n");
            report.append("Date: ").append(date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n");
            report.append("=".repeat(80)).append("\n");
            report.append(String.format("%-6s %-20s %-10s %-10s %-12s %-10s%n",
                    "ID", "Name", "Log In", "Log Out", "Work Hours", "Status"));
            report.append("-".repeat(80)).append("\n");

            int presentCount = 0;
            int lateCount = 0;
            int absentCount = 0;

            for (Employee emp : employees) {
                Attendance attendance = attendanceDAO.getAttendanceByEmployeeAndDate(
                        emp.getEmployeeId(), date);

                if (attendance != null) {
                    presentCount++;
                    String status = "Present";
                    if (attendance.isLate()) {
                        lateCount++;
                        status = "Late";
                    }

                    report.append(String.format("%-6d %-20s %-10s %-10s %-12.2f %-10s%n",
                            emp.getEmployeeId(),
                            emp.getFullName().length() > 20 ? emp.getFullName().substring(0, 17) + "..." : emp.getFullName(),
                            attendance.getLogIn() != null ? attendance.getLogIn().toString() : "N/A",
                            attendance.getLogOut() != null ? attendance.getLogOut().toString() : "N/A",
                            attendance.getWorkHours(),
                            status
                    ));
                } else {
                    absentCount++;
                    report.append(String.format("%-6d %-20s %-10s %-10s %-12s %-10s%n",
                            emp.getEmployeeId(),
                            emp.getFullName().length() > 20 ? emp.getFullName().substring(0, 17) + "..." : emp.getFullName(),
                            "ABSENT",
                            "ABSENT",
                            "0.00",
                            "Absent"
                    ));
                }
            }

            report.append("-".repeat(80)).append("\n");
            report.append("SUMMARY:\n");
            report.append(String.format("Total Employees: %d%n", employees.size()));
            report.append(String.format("Present: %d%n", presentCount));
            report.append(String.format("Late: %d%n", lateCount));
            report.append(String.format("Absent: %d%n", absentCount));
            report.append(String.format("Attendance Rate: %.2f%%%n",
                    (double) presentCount / employees.size() * 100));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating daily attendance report", e);
            throw new RuntimeException("Failed to generate daily attendance report", e);
        }

        return report.toString();
    }
}

