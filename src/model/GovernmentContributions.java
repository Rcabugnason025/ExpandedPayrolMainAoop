
package model;

import java.time.LocalDate;
import java.util.Objects;

public class GovernmentContributions {
    private int contributionId;
    private int employeeId;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;
    private String tin; // Tax Identification Number
    private LocalDate contributionPeriod;

    // Constructors
    public GovernmentContributions() {}

    public GovernmentContributions(int employeeId, double sss, double philhealth, double pagibig, double tax) {
        this.employeeId = employeeId;
        setSss(sss);
        setPhilhealth(philhealth);
        setPagibig(pagibig);
        setTax(tax);
        this.contributionPeriod = LocalDate.now();
    }

    // Getters and Setters with enhanced validation
    public int getContributionId() {
        return contributionId;
    }

    public void setContributionId(int contributionId) {
        this.contributionId = contributionId;
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

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        if (tin != null && !isValidTin(tin)) {
            throw new IllegalArgumentException("Invalid TIN format");
        }
        this.tin = tin;
    }

    public LocalDate getContributionPeriod() {
        return contributionPeriod;
    }

    public void setContributionPeriod(LocalDate contributionPeriod) {
        this.contributionPeriod = contributionPeriod;
    }

    // Utility methods
    public double getTotalContributions() {
        return sss + philhealth + pagibig + tax;
    }

    public double getTotalMandatoryContributions() {
        return sss + philhealth + pagibig; // Excluding tax
    }

    private boolean isValidTin(String tin) {
        if (tin == null || tin.trim().isEmpty()) {
            return false;
        }
        // Basic TIN validation (Philippines format: XXX-XXX-XXX-XXX)
        String cleanTin = tin.replaceAll("[^0-9]", "");
        return cleanTin.length() == 12;
    }

    public String getFormattedTotalContributions() {
        return String.format("₱%.2f", getTotalContributions());
    }

    public boolean hasValidContributions() {
        return sss >= 0 && philhealth >= 0 && pagibig >= 0 && tax >= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GovernmentContributions that = (GovernmentContributions) obj;
        return contributionId == that.contributionId &&
               employeeId == that.employeeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contributionId, employeeId);
    }

    @Override
    public String toString() {
        return "GovernmentContributions{" +
                "contributionId=" + contributionId +
                ", employeeId=" + employeeId +
                ", sss=" + sss +
                ", philhealth=" + philhealth +
                ", pagibig=" + pagibig +
                ", tax=" + tax +
                ", tin='" + tin + '\'' +
                ", total=" + getTotalContributions() +
                ", period=" + contributionPeriod +
                '}';
    }
}