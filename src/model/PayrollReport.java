package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class PayrollReport {
    private String reportId;
    private String reportTitle;
    private LocalDate generatedDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String generatedBy;
    private ReportType reportType;
    private List<Payroll> payrollData;
    private ReportSummary summary;

    // Report types enum
    public enum ReportType {
        MONTHLY_PAYROLL,
        EMPLOYEE_PAYROLL,
        DEPARTMENT_PAYROLL,
        ANNUAL_SUMMARY,
        TAX_REPORT,
        GOVERNMENT_CONTRIBUTIONS
    }

    // Constructors
    public PayrollReport() {
        this.generatedDate = LocalDate.now();
    }

    public PayrollReport(String reportTitle, ReportType reportType, String generatedBy) {
        this();
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.generatedBy = generatedBy;
        this.reportId = generateReportId();
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public LocalDate getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDate generatedDate) {
        this.generatedDate = generatedDate;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public List<Payroll> getPayrollData() {
        return payrollData;
    }

    public void setPayrollData(List<Payroll> payrollData) {
        this.payrollData = payrollData;
    }

    public ReportSummary getSummary() {
        return summary;
    }

    public void setSummary(ReportSummary summary) {
        this.summary = summary;
    }

    // Utility methods
    private String generateReportId() {
        String dateStr = generatedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String typePrefix = reportType != null ? reportType.name().substring(0, 3) : "RPT";
        return typePrefix + "_" + dateStr + "_" + System.currentTimeMillis() % 10000;
    }

    public String getFormattedPeriod() {
        if (periodStart != null && periodEnd != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            return periodStart.format(formatter) + " - " + periodEnd.format(formatter);
        }
        return "N/A";
    }

    public int getTotalEmployees() {
        return payrollData != null ? payrollData.size() : 0;
    }

    public double getTotalGrossPay() {
        if (payrollData == null) return 0.0;
        return payrollData.stream().mapToDouble(Payroll::getGrossPay).sum();
    }

    public double getTotalDeductions() {
        if (payrollData == null) return 0.0;
        return payrollData.stream().mapToDouble(Payroll::getTotalDeductions).sum();
    }

    public double getTotalNetPay() {
        if (payrollData == null) return 0.0;
        return payrollData.stream().mapToDouble(Payroll::getNetPay).sum();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PayrollReport that = (PayrollReport) obj;
        return Objects.equals(reportId, that.reportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId);
    }

    @Override
    public String toString() {
        return "PayrollReport{" +
                "reportId='" + reportId + '\'' +
                ", reportTitle='" + reportTitle + '\'' +
                ", reportType=" + reportType +
                ", period='" + getFormattedPeriod() + '\'' +
                ", totalEmployees=" + getTotalEmployees() +
                '}';
    }

    // Inner class for report summary
    public static class ReportSummary {
        private double totalGrossPay;
        private double totalDeductions;
        private double totalNetPay;
        private double totalSSSContributions;
        private double totalPhilHealthContributions;
        private double totalPagIBIGContributions;
        private double totalTax;
        private int totalEmployees;

        // Constructors, getters, and setters
        public ReportSummary() {}

        public double getTotalGrossPay() { return totalGrossPay; }
        public void setTotalGrossPay(double totalGrossPay) { this.totalGrossPay = totalGrossPay; }

        public double getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

        public double getTotalNetPay() { return totalNetPay; }
        public void setTotalNetPay(double totalNetPay) { this.totalNetPay = totalNetPay; }

        public double getTotalSSSContributions() { return totalSSSContributions; }
        public void setTotalSSSContributions(double totalSSSContributions) { this.totalSSSContributions = totalSSSContributions; }

        public double getTotalPhilHealthContributions() { return totalPhilHealthContributions; }
        public void setTotalPhilHealthContributions(double totalPhilHealthContributions) { this.totalPhilHealthContributions = totalPhilHealthContributions; }

        public double getTotalPagIBIGContributions() { return totalPagIBIGContributions; }
        public void setTotalPagIBIGContributions(double totalPagIBIGContributions) { this.totalPagIBIGContributions = totalPagIBIGContributions; }

        public double getTotalTax() { return totalTax; }
        public void setTotalTax(double totalTax) { this.totalTax = totalTax; }

        public int getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }
    }
}
