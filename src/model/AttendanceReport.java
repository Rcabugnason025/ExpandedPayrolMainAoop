package model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AttendanceReport {
    private String reportId;
    private String reportTitle;
    private LocalDate generatedDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String generatedBy;
    private Map<Integer, List<Attendance>> attendanceData;
    private AttendanceSummary summary;

    // Constructors
    public AttendanceReport() {
        this.generatedDate = LocalDate.now();
        this.summary = new AttendanceSummary();
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public Map<Integer, List<Attendance>> getAttendanceData() { return attendanceData; }
    public void setAttendanceData(Map<Integer, List<Attendance>> attendanceData) { this.attendanceData = attendanceData; }

    public AttendanceSummary getSummary() { return summary; }
    public void setSummary(AttendanceSummary summary) { this.summary = summary; }

    /**
     * Calculate summary statistics from attendance data
     */
    public void calculateSummaryStatistics() {
        if (attendanceData == null || attendanceData.isEmpty()) {
            return;
        }

        int totalEmployees = attendanceData.size();
        int totalPresentDays = 0;
        int totalLateDays = 0;
        int totalAbsentDays = 0;
        double totalWorkHours = 0.0;

        for (List<Attendance> attendanceList : attendanceData.values()) {
            for (Attendance attendance : attendanceList) {
                totalPresentDays++;
                totalWorkHours += attendance.getWorkHours();

                if (attendance.isLate()) {
                    totalLateDays++;
                }
            }
        }

        // Calculate working days in period
        int workingDays = calculateWorkingDays(periodStart, periodEnd);
        totalAbsentDays = (totalEmployees * workingDays) - totalPresentDays;

        summary.setTotalEmployees(totalEmployees);
        summary.setTotalPresentDays(totalPresentDays);
        summary.setTotalLateDays(totalLateDays);
        summary.setTotalAbsentDays(totalAbsentDays);
        summary.setTotalWorkHours(totalWorkHours);
        summary.setAverageWorkHours(totalPresentDays > 0 ? totalWorkHours / totalPresentDays : 0.0);
        summary.setAttendanceRate(workingDays > 0 ? (double) totalPresentDays / (totalEmployees * workingDays) * 100 : 0.0);
    }

    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        // Simple calculation - excludes weekends
        int workingDays = 0;
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (current.getDayOfWeek().getValue() <= 5) { // Monday to Friday
                workingDays++;
            }
            current = current.plusDays(1);
        }

        return workingDays;
    }

    // Inner class for attendance summary
    public static class AttendanceSummary {
        private int totalEmployees;
        private int totalPresentDays;
        private int totalLateDays;
        private int totalAbsentDays;
        private double totalWorkHours;
        private double averageWorkHours;
        private double attendanceRate;

        // Getters and Setters
        public int getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

        public int getTotalPresentDays() { return totalPresentDays; }
        public void setTotalPresentDays(int totalPresentDays) { this.totalPresentDays = totalPresentDays; }

        public int getTotalLateDays() { return totalLateDays; }
        public void setTotalLateDays(int totalLateDays) { this.totalLateDays = totalLateDays; }

        public int getTotalAbsentDays() { return totalAbsentDays; }
        public void setTotalAbsentDays(int totalAbsentDays) { this.totalAbsentDays = totalAbsentDays; }

        public double getTotalWorkHours() { return totalWorkHours; }
        public void setTotalWorkHours(double totalWorkHours) { this.totalWorkHours = totalWorkHours; }

        public double getAverageWorkHours() { return averageWorkHours; }
        public void setAverageWorkHours(double averageWorkHours) { this.averageWorkHours = averageWorkHours; }

        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }
}
