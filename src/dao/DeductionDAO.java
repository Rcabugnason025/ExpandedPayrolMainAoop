package dao;

import model.Deduction;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Fixed DeductionDAO with concrete Deduction implementation
 * @author Enhanced by Assistant
 */
public class DeductionDAO {
    private static final Logger LOGGER = Logger.getLogger(DeductionDAO.class.getName());

    /**
     * Adds a deduction record to the database
     */
    public void addDeduction(Deduction deduction) throws SQLException {
        if (deduction == null) {
            throw new IllegalArgumentException("Deduction cannot be null");
        }

        String sql = "INSERT INTO deductions (employee_id, type, amount, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, deduction.getEmployeeId());
            stmt.setString(2, deduction.getType());
            stmt.setDouble(3, deduction.getAmount());
            stmt.setString(4, deduction.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        deduction.setDeductionId(generatedKeys.getInt(1));
                    }
                }
                LOGGER.info("Successfully added deduction for employee: " + deduction.getEmployeeId());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding deduction", e);
            throw e;
        }
    }

    /**
     * Retrieves all deductions for a specific employee
     */
    public List<Deduction> getDeductionsByEmployeeId(int employeeId) throws SQLException {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "SELECT * FROM deductions WHERE employee_id = ? ORDER BY deduction_date DESC";
        List<Deduction> deductions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Create concrete implementation of Deduction
                ConcreteDeduction d = new ConcreteDeduction(
                        rs.getInt("employee_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                d.setDeductionId(rs.getInt("deduction_id"));

                // Set deduction date if it exists in the database
                Date deductionDate = rs.getDate("deduction_date");
                if (deductionDate != null) {
                    d.setDeductionDate(deductionDate);
                }

                deductions.add(d);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving deductions for employee ID: " + employeeId, e);
            throw e;
        }

        return deductions;
    }

    /**
     * Updates an existing deduction record
     */
    public boolean updateDeduction(Deduction deduction) throws SQLException {
        if (deduction == null || deduction.getDeductionId() <= 0) {
            throw new IllegalArgumentException("Invalid deduction or deduction ID");
        }

        String sql = "UPDATE deductions SET employee_id = ?, type = ?, amount = ?, description = ? WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deduction.getEmployeeId());
            stmt.setString(2, deduction.getType());
            stmt.setDouble(3, deduction.getAmount());
            stmt.setString(4, deduction.getDescription());
            stmt.setInt(5, deduction.getDeductionId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                LOGGER.info("Successfully updated deduction ID: " + deduction.getDeductionId());
            }

            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating deduction", e);
            throw e;
        }
    }

    /**
     * Deletes a deduction record
     */
    public boolean deleteDeduction(int deductionId) throws SQLException {
        if (deductionId <= 0) {
            throw new IllegalArgumentException("Deduction ID must be positive");
        }

        String sql = "DELETE FROM deductions WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deductionId);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                LOGGER.info("Successfully deleted deduction ID: " + deductionId);
            }

            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting deduction ID: " + deductionId, e);
            throw e;
        }
    }

    /**
     * Gets a specific deduction by ID
     */
    public Deduction getDeductionById(int deductionId) throws SQLException {
        if (deductionId <= 0) {
            throw new IllegalArgumentException("Deduction ID must be positive");
        }

        String sql = "SELECT * FROM deductions WHERE deduction_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deductionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ConcreteDeduction deduction = new ConcreteDeduction(
                        rs.getInt("employee_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                );
                deduction.setDeductionId(rs.getInt("deduction_id"));

                Date deductionDate = rs.getDate("deduction_date");
                if (deductionDate != null) {
                    deduction.setDeductionDate(deductionDate);
                }

                return deduction;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving deduction by ID: " + deductionId, e);
            throw e;
        }

        return null;
    }

    /**
     * Gets total deductions for an employee by type
     */
    public double getTotalDeductionsByType(int employeeId, String type) throws SQLException {
        if (employeeId <= 0 || type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid employee ID or deduction type");
        }

        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM deductions WHERE employee_id = ? AND type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, type.trim());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating total deductions", e);
            throw e;
        }

        return 0.0;
    }

    /**
     * Concrete implementation of the abstract Deduction class
     * This is used internally by the DAO to create Deduction instances from database records
     */
    private static class ConcreteDeduction extends Deduction {

        public ConcreteDeduction() {
            super();
        }

        public ConcreteDeduction(int employeeId, String type, double amount, String description) {
            super(employeeId, type, amount, description);
        }

        @Override
        public void calculateDeduction() {
            // For generic deductions loaded from database,
            // the amount is already calculated and stored
            // This method is a no-op for database-loaded deductions
        }
    }
}