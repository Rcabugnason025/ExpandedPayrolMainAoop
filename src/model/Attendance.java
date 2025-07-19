package model;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Model class representing employee attendance
 * Updated to match actual database schema (log_in/log_out instead of login_time/logout_time)
 * @author rejoice
 */
public class Attendance {
    private int id; // Changed from attendanceId to match database
    private int employeeId;
    private Date date;
    private Time logIn; // Changed from loginTime to match database
    private Time logOut; // Changed from logoutTime to match database

    // Constructors
    public Attendance() {}

    public Attendance(int employeeId, Date date, Time logIn, Time logOut) {
        setEmployeeId(employeeId);
        setDate(date);
        setLogIn(logIn);
        setLogOut(logOut);
    }

    // Primary getters and setters (matching database schema)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        this.employeeId = employeeId;
    }

    public Date getDate() { return date; }
    public void setDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
    }

    public Time getLogIn() { return logIn; }
    public void setLogIn(Time logIn) { this.logIn = logIn; }

    public Time getLogOut() { return logOut; }
    public void setLogOut(Time logOut) {
        if (logIn != null && logOut != null && logOut.before(logIn)) {
            throw new IllegalArgumentException("Log out time cannot be before log in time");
        }
        this.logOut = logOut;
    }

    // Backward compatibility methods for existing code
    public int getAttendanceId() { return id; }
    public void setAttendanceId(int attendanceId) { this.id = attendanceId; }

    public Time getLoginTime() { return logIn; }
    public void setLoginTime(Time loginTime) { setLogIn(loginTime); }

    public Time getLogoutTime() { return logOut; }
    public void setLogoutTime(Time logoutTime) { setLogOut(logoutTime); }

    // Utility methods
    public Duration getWorkDuration() {
        if (logIn == null || logOut == null) {
            return Duration.ZERO;
        }
        LocalTime login = logIn.toLocalTime();
        LocalTime logout = logOut.toLocalTime();
        return Duration.between(login, logout);
    }

    public double getWorkHours() {
        return getWorkDuration().toMinutes() / 60.0;
    }

    public boolean isFullDay() {
        return getWorkDuration().toHours() >= 8;
    }

    public boolean isPresent() {
        return logIn != null;
    }

    public boolean isLate() {
        if (logIn == null) return false;
        LocalTime standardStart = LocalTime.of(8, 0);
        return logIn.toLocalTime().isAfter(standardStart);
    }

    public boolean hasUndertime() {
        if (logOut == null) return false;
        LocalTime standardEnd = LocalTime.of(17, 0);
        return logOut.toLocalTime().isBefore(standardEnd);
    }

    public double getLateMinutes() {
        if (!isLate()) return 0.0;
        LocalTime standardStart = LocalTime.of(8, 0);
        return Duration.between(standardStart, logIn.toLocalTime()).toMinutes();
    }

    public double getUndertimeMinutes() {
        if (!hasUndertime()) return 0.0;
        LocalTime standardEnd = LocalTime.of(17, 0);
        return Duration.between(logOut.toLocalTime(), standardEnd).toMinutes();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Attendance that = (Attendance) obj;
        return id == that.id &&
               employeeId == that.employeeId &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeId, date);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", date=" + date +
                ", logIn=" + logIn +
                ", logOut=" + logOut +
                ", workHours=" + String.format("%.2f", getWorkHours()) +
                '}';
    }
}