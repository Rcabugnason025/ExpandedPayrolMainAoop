package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Employee {
    private int employeeId;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String address;
    private String phoneNumber;
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;
    private String status; // 'Regular' or 'Probationary'
    private String position;
    private String immediateSupervisor;
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSemiMonthlyRate;
    private double hourlyRate;

    // Constructors
    public Employee() {}

    public Employee(String firstName, String lastName, LocalDate birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
    }

    // Getters and Setters
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim();
    }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be in the future");
        }
        this.birthday = birthday;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSssNumber() { return sssNumber; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }

    public String getPhilhealthNumber() { return philhealthNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }

    public String getTinNumber() { return tinNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }

    public String getPagibigNumber() { return pagibigNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getImmediateSupervisor() { return immediateSupervisor; }
    public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) {
        if (basicSalary < 0) throw new IllegalArgumentException("Basic salary cannot be negative");
        this.basicSalary = basicSalary;
    }

    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }

    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }

    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }

    public double getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public void setGrossSemiMonthlyRate(double grossSemiMonthlyRate) { this.grossSemiMonthlyRate = grossSemiMonthlyRate; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    // Utility methods
    public String getFullName() { return firstName + " " + lastName; }

    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    public int getAge() {
        if (birthday == null) return 0;
        return LocalDate.now().getYear() - birthday.getYear();
    }

    // Backward compatibility methods for existing code
    public LocalDate getBirthdate() { return birthday; }
    public void setBirthdate(LocalDate birthdate) { setBirthday(birthdate); }

    public String getContactInfo() { return phoneNumber; }
    public void setContactInfo(String contactInfo) { setPhoneNumber(contactInfo); }

    // For compatibility with existing DAO code that expects these methods
    public int getEmploymentStatusId() { return "Regular".equals(status) ? 1 : 2; }
    public void setEmploymentStatusId(int statusId) {
        this.status = (statusId == 1) ? "Regular" : "Probationary";
    }

    public int getPositionId() { return 1; } // Default value since we don't have position table
    public void setPositionId(int positionId) { /* No-op for compatibility */ }

    public int getSupervisorId() { return 0; } // Default value
    public void setSupervisorId(int supervisorId) { /* No-op for compatibility */ }

    // FIXED: Updated toString() method for combo box display
    @Override
    public String toString() {
        return employeeId + " - " + getFullName();
    }
}