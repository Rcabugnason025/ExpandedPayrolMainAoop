
package dao;

import util.DBConnection;
import model.CompensationDetails;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompensationDetailsDAO {
    private static final Logger LOGGER = Logger.getLogger(CompensationDetailsDAO.class.getName());

    public CompensationDetails getById(int id) {
        String query = "SELECT * FROM compensation_details WHERE compensation_details_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCompensationDetails(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching compensation details by ID: " + id, ex);
            throw new RuntimeException("Failed to fetch compensation details", ex);
        }

        return null;
    }

    public CompensationDetails getCompensationDetailsByEmployeeId(int employeeId) {
        String query = "SELECT * FROM compensation_details WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCompensationDetails(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching compensation details for employee ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch compensation details", ex);
        }

        return null;
    }

    public boolean insertCompensationDetails(CompensationDetails compensationDetails) {
        String sql = "INSERT INTO compensation_details (employee_id, rice_subsidy, phone_allowance, clothing_allowance) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, compensationDetails.getEmployeeId());
            stmt.setDouble(2, compensationDetails.getRiceSubsidy());
            stmt.setDouble(3, compensationDetails.getPhoneAllowance());
            stmt.setDouble(4, compensationDetails.getClothingAllowance());

            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    compensationDetails.setCompId(keys.getInt(1));
                }
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting compensation details", ex);
            throw new RuntimeException("Failed to insert compensation details", ex);
        }

        return false;
    }

    public boolean updateCompensationDetails(CompensationDetails compensationDetails) {
        String sql = "UPDATE compensation_details SET employee_id=?, rice_subsidy=?, phone_allowance=?, clothing_allowance=? WHERE compensation_details_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compensationDetails.getEmployeeId());
            stmt.setDouble(2, compensationDetails.getRiceSubsidy());
            stmt.setDouble(3, compensationDetails.getPhoneAllowance());
            stmt.setDouble(4, compensationDetails.getClothingAllowance());
            stmt.setInt(5, compensationDetails.getCompId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating compensation details ID: " + compensationDetails.getCompId(), ex);
            throw new RuntimeException("Failed to update compensation details", ex);
        }
    }

    public boolean deleteCompensationDetails(int compId) {
        String sql = "DELETE FROM compensation_details WHERE compensation_details_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, compId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting compensation details ID: " + compId, ex);
            throw new RuntimeException("Failed to delete compensation details", ex);
        }
    }

    private CompensationDetails mapResultSetToCompensationDetails(ResultSet rs) throws SQLException {
        CompensationDetails c = new CompensationDetails();
        c.setCompId(rs.getInt("compensation_details_id"));
        c.setEmployeeId(rs.getInt("employee_id"));
        c.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        c.setPhoneAllowance(rs.getDouble("phone_allowance"));
        c.setClothingAllowance(rs.getDouble("clothing_allowance"));
        return c;
    }
}