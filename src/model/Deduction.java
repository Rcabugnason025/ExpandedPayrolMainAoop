package model;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Enhanced Deduction model class
 * @author rejoice
 */
public abstract class Deduction {
    protected int deductionId;
    protected int employeeId;
    protected String type;
    protected double amount;
    protected String description;
    protected Date deductionDate;

    // Deduction type constants
    public static final String TYPE_LATE = "Late";
    public static final String TYPE_UNDERTIME = "Undertime";
    public static final String TYPE_UNPAID_LEAVE = "UnpaidLeave";
    public static final String TYPE_SSS = "SSS";
    public static final String TYPE_PHILHEALTH = "PhilHealth";
    public static final String TYPE_PAGIBIG = "PagIBIG";
    public static final String TYPE_TAX = "Tax";

    // Constructors
    public Deduction() {
        this.deductionDate = Date.valueOf(LocalDate.now());
    }

    public Deduction(int employeeId, String type, double amount) {
        this();
        setEmployeeId(employeeId);
        setType(type);
        setAmount(amount);
    }

    public Deduction(int employeeId, String type, double amount, String description) {
        this(employeeId, type, amount);
        setDescription(description);
    }

    // Abstract method that subclasses must implement
    public abstract void calculateDeduction();

    // Getters and Setters with validation
    public int getDeductionId() {
        return deductionId;
    }

    public void setDeductionId(int deductionId) {
        this.deductionId = deductionId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        this.type = type.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public Date getDeductionDate() {
        return deductionDate;
    }

    public void setDeductionDate(Date deductionDate) {
        this.deductionDate = deductionDate;
    }

    // Utility methods
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public String getFormattedAmount() {
        return String.format("%.2f", amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Deduction deduction = (Deduction) obj;
        return deductionId == deduction.deductionId &&
                employeeId == deduction.employeeId &&
                Objects.equals(type, deduction.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deductionId, employeeId, type);
    }

    @Override
    public String toString() {
        return "Deduction{" +
                "deductionId=" + deductionId +
                ", employeeId=" + employeeId +
                ", type='" + type + '\'' +
                ", amount=" + getFormattedAmount() +
                ", description='" + description + '\'' +
                '}';
    }

    // Factory method for creating specific deduction types
    public static Deduction createDeduction(String type, int employeeId, Object... params) {
        switch (type.toLowerCase()) {
            case "late":
                if (params.length >= 2 && params[0] instanceof Time && params[1] instanceof Double) {
                    return new LateDeduction(employeeId, (Time) params[0], (Double) params[1]);
                }
                break;
            case "undertime":
                if (params.length >= 2 && params[0] instanceof Time && params[1] instanceof Double) {
                    return new UndertimeDeduction(employeeId, (Time) params[0], (Double) params[1]);
                }
                break;
            case "unpaidleave":
                if (params.length >= 2 && params[0] instanceof Integer && params[1] instanceof Double) {
                    return new UnpaidLeaveDeduction(employeeId, (Integer) params[0], (Double) params[1]);
                }
                break;
        }
        throw new IllegalArgumentException("Invalid deduction type or parameters");
    }

    /**
     * Late Deduction subclass
     */
    public static class LateDeduction extends Deduction {
        private Time actualArrivalTime;
        private Time expectedArrivalTime;
        private long minutesLate;
        private double hourlyRate;

        // Constants
        private static final LocalTime STANDARD_START_TIME = LocalTime.of(8, 0);
        private static final int GRACE_PERIOD_MINUTES = 15;

        public LateDeduction() {
            super();
            setType(TYPE_LATE);
            setExpectedArrivalTime(Time.valueOf(STANDARD_START_TIME));
        }

        public LateDeduction(int employeeId, Time actualArrivalTime, double hourlyRate) {
            this();
            setEmployeeId(employeeId);
            setActualArrivalTime(actualArrivalTime);
            setHourlyRate(hourlyRate);
            calculateDeduction();
        }

        // Getters and Setters
        public Time getActualArrivalTime() { return actualArrivalTime; }

        public void setActualArrivalTime(Time actualArrivalTime) {
            this.actualArrivalTime = actualArrivalTime;
            if (actualArrivalTime != null && expectedArrivalTime != null) {
                calculateMinutesLate();
            }
        }

        public Time getExpectedArrivalTime() { return expectedArrivalTime; }

        public void setExpectedArrivalTime(Time expectedArrivalTime) {
            this.expectedArrivalTime = expectedArrivalTime;
            if (actualArrivalTime != null && expectedArrivalTime != null) {
                calculateMinutesLate();
            }
        }

        public long getMinutesLate() { return minutesLate; }
        public double getHourlyRate() { return hourlyRate; }

        public void setHourlyRate(double hourlyRate) {
            if (hourlyRate < 0) {
                throw new IllegalArgumentException("Hourly rate cannot be negative");
            }
            this.hourlyRate = hourlyRate;
        }

        private void calculateMinutesLate() {
            if (actualArrivalTime == null || expectedArrivalTime == null) {
                this.minutesLate = 0;
                return;
            }

            LocalTime actual = actualArrivalTime.toLocalTime();
            LocalTime expected = expectedArrivalTime.toLocalTime();

            if (actual.isAfter(expected.plusMinutes(GRACE_PERIOD_MINUTES))) {
                this.minutesLate = ChronoUnit.MINUTES.between(expected, actual);
            } else {
                this.minutesLate = 0;
            }
        }

        @Override
        public void calculateDeduction() {
            calculateMinutesLate();

            if (minutesLate > 0 && hourlyRate > 0) {
                double hoursLate = minutesLate / 60.0;
                double deductionAmount = hoursLate * hourlyRate;
                setAmount(deductionAmount);
                setDescription(String.format("Late arrival deduction: %d minutes late", minutesLate));
            } else {
                setAmount(0.0);
                setDescription("No late deduction applicable");
            }
        }

        public boolean isLate() { return minutesLate > 0; }
        public double getHoursLate() { return minutesLate / 60.0; }
    }

    /**
     * Undertime Deduction subclass
     */
    public static class UndertimeDeduction extends Deduction {
        private Time actualDepartureTime;
        private Time expectedDepartureTime;
        private long minutesEarly;
        private double hourlyRate;

        // Constants
        private static final LocalTime STANDARD_END_TIME = LocalTime.of(17, 0);

        public UndertimeDeduction() {
            super();
            setType(TYPE_UNDERTIME);
            setExpectedDepartureTime(Time.valueOf(STANDARD_END_TIME));
        }

        public UndertimeDeduction(int employeeId, Time actualDepartureTime, double hourlyRate) {
            this();
            setEmployeeId(employeeId);
            setActualDepartureTime(actualDepartureTime);
            setHourlyRate(hourlyRate);
            calculateDeduction();
        }

        // Getters and Setters
        public Time getActualDepartureTime() { return actualDepartureTime; }

        public void setActualDepartureTime(Time actualDepartureTime) {
            this.actualDepartureTime = actualDepartureTime;
            if (actualDepartureTime != null && expectedDepartureTime != null) {
                calculateMinutesEarly();
            }
        }

        public Time getExpectedDepartureTime() { return expectedDepartureTime; }

        public void setExpectedDepartureTime(Time expectedDepartureTime) {
            this.expectedDepartureTime = expectedDepartureTime;
            if (actualDepartureTime != null && expectedDepartureTime != null) {
                calculateMinutesEarly();
            }
        }

        public long getMinutesEarly() { return minutesEarly; }
        public double getHourlyRate() { return hourlyRate; }

        public void setHourlyRate(double hourlyRate) {
            if (hourlyRate < 0) {
                throw new IllegalArgumentException("Hourly rate cannot be negative");
            }
            this.hourlyRate = hourlyRate;
        }

        private void calculateMinutesEarly() {
            if (actualDepartureTime == null || expectedDepartureTime == null) {
                this.minutesEarly = 0;
                return;
            }

            LocalTime actual = actualDepartureTime.toLocalTime();
            LocalTime expected = expectedDepartureTime.toLocalTime();

            if (actual.isBefore(expected)) {
                this.minutesEarly = ChronoUnit.MINUTES.between(actual, expected);
            } else {
                this.minutesEarly = 0;
            }
        }

        @Override
        public void calculateDeduction() {
            calculateMinutesEarly();

            if (minutesEarly > 0 && hourlyRate > 0) {
                double hoursEarly = minutesEarly / 60.0;
                double deductionAmount = hoursEarly * hourlyRate;
                setAmount(deductionAmount);
                setDescription(String.format("Undertime deduction: %d minutes early departure", minutesEarly));
            } else {
                setAmount(0.0);
                setDescription("No undertime deduction applicable");
            }
        }

        public boolean hasUndertime() { return minutesEarly > 0; }
        public double getHoursEarly() { return minutesEarly / 60.0; }
    }

    /**
     * Unpaid Leave Deduction subclass
     */
    public static class UnpaidLeaveDeduction extends Deduction {
        private int unpaidLeaveDays;
        private double dailyRate;
        private Date leaveStartDate;
        private Date leaveEndDate;
        private String leaveReason;

        public UnpaidLeaveDeduction() {
            super();
            setType(TYPE_UNPAID_LEAVE);
        }

        public UnpaidLeaveDeduction(int employeeId, int unpaidLeaveDays, double dailyRate) {
            this();
            setEmployeeId(employeeId);
            setUnpaidLeaveDays(unpaidLeaveDays);
            setDailyRate(dailyRate);
            calculateDeduction();
        }

        public UnpaidLeaveDeduction(int employeeId, Date leaveStartDate, Date leaveEndDate, double dailyRate, String reason) {
            this();
            setEmployeeId(employeeId);
            setLeaveStartDate(leaveStartDate);
            setLeaveEndDate(leaveEndDate);
            setDailyRate(dailyRate);
            setLeaveReason(reason);
            calculateLeaveDays();
            calculateDeduction();
        }

        // Getters and Setters
        public int getUnpaidLeaveDays() { return unpaidLeaveDays; }

        public void setUnpaidLeaveDays(int unpaidLeaveDays) {
            if (unpaidLeaveDays < 0) {
                throw new IllegalArgumentException("Unpaid leave days cannot be negative");
            }
            this.unpaidLeaveDays = unpaidLeaveDays;
        }

        public double getDailyRate() { return dailyRate; }

        public void setDailyRate(double dailyRate) {
            if (dailyRate < 0) {
                throw new IllegalArgumentException("Daily rate cannot be negative");
            }
            this.dailyRate = dailyRate;
        }

        public Date getLeaveStartDate() { return leaveStartDate; }
        public void setLeaveStartDate(Date leaveStartDate) { this.leaveStartDate = leaveStartDate; }

        public Date getLeaveEndDate() { return leaveEndDate; }

        public void setLeaveEndDate(Date leaveEndDate) {
            if (leaveStartDate != null && leaveEndDate != null && leaveEndDate.before(leaveStartDate)) {
                throw new IllegalArgumentException("Leave end date cannot be before start date");
            }
            this.leaveEndDate = leaveEndDate;
        }

        public String getLeaveReason() { return leaveReason; }
        public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }

        private void calculateLeaveDays() {
            if (leaveStartDate != null && leaveEndDate != null) {
                LocalDate start = leaveStartDate.toLocalDate();
                LocalDate end = leaveEndDate.toLocalDate();

                // Count working days (Monday to Friday)
                int workingDays = 0;
                LocalDate current = start;

                while (!current.isAfter(end)) {
                    if (current.getDayOfWeek().getValue() <= 5) { // Monday = 1, Friday = 5
                        workingDays++;
                    }
                    current = current.plusDays(1);
                }

                this.unpaidLeaveDays = workingDays;
            }
        }

        @Override
        public void calculateDeduction() {
            if (unpaidLeaveDays > 0 && dailyRate > 0) {
                double deductionAmount = unpaidLeaveDays * dailyRate;
                setAmount(deductionAmount);

                String description = String.format("Unpaid leave deduction: %d days", unpaidLeaveDays);
                if (leaveReason != null && !leaveReason.trim().isEmpty()) {
                    description += " (" + leaveReason + ")";
                }
                setDescription(description);
            } else {
                setAmount(0.0);
                setDescription("No unpaid leave deduction applicable");
            }
        }

        public boolean hasUnpaidLeave() { return unpaidLeaveDays > 0; }
        public LocalDate getLeaveStartDateAsLocalDate() { return leaveStartDate != null ? leaveStartDate.toLocalDate() : null; }
        public LocalDate getLeaveEndDateAsLocalDate() { return leaveEndDate != null ? leaveEndDate.toLocalDate() : null; }
    }
}