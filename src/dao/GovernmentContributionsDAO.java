
package dao;

import util.DBConnection;
import model.GovernmentContributions;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GovernmentContributionsDAO {
    private static final Logger LOGGER = Logger.getLogger(GovernmentContributionsDAO.class.getName());

    public GovernmentContributions getById(int id) {
        String query = "SELECT * FROM government_contributions WHERE contribution_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGovernmentContributions(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching government contributions by ID: " + id, ex);
            throw new RuntimeException("Failed to fetch government contributions", ex);
        }

        return null;
    }

    public GovernmentContributions getByEmployeeId(int employeeId) {
        String query = "SELECT * FROM government_contributions WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGovernmentContributions(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching government contributions for employee ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch government contributions", ex);
        }

        return null;
    }

    public boolean insertGovernmentContributions(GovernmentContributions contributions) {
        String sql = "INSERT INTO government_contributions (employee_id, sss, philhealth, pagibig, tax) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, contributions.getEmployeeId());
            stmt.setDouble(2, contributions.getSss());
            stmt.setDouble(3, contributions.getPhilhealth());
            stmt.setDouble(4, contributions.getPagibig());
            stmt.setDouble(5, contributions.getTax());

            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    contributions.setContributionId(keys.getInt(1));
                }
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting government contributions", ex);
            throw new RuntimeException("Failed to insert government contributions", ex);
        }

        return false;
    }

    public boolean updateGovernmentContributions(GovernmentContributions contributions) {
        String sql = "UPDATE government_contributions SET employee_id=?, sss=?, philhealth=?, pagibig=?, tax=? WHERE contribution_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contributions.getEmployeeId());
            stmt.setDouble(2, contributions.getSss());
            stmt.setDouble(3, contributions.getPhilhealth());
            stmt.setDouble(4, contributions.getPagibig());
            stmt.setDouble(5, contributions.getTax());
            stmt.setInt(6, contributions.getContributionId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating government contributions ID: " + contributions.getContributionId(), ex);
            throw new RuntimeException("Failed to update government contributions", ex);
        }
    }

    public boolean deleteGovernmentContributions(int contributionId) {
        String sql = "DELETE FROM government_contributions WHERE contribution_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, contributionId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting government contributions ID: " + contributionId, ex);
            throw new RuntimeException("Failed to delete government contributions", ex);
        }
    }

    private GovernmentContributions mapResultSetToGovernmentContributions(ResultSet rs) throws SQLException {
        GovernmentContributions g = new GovernmentContributions();
        g.setContributionId(rs.getInt("contribution_id"));
        g.setEmployeeId(rs.getInt("employee_id"));
        g.setSss(rs.getDouble("sss"));
        g.setPhilhealth(rs.getDouble("philhealth"));
        g.setPagibig(rs.getDouble("pagibig"));
        g.setTax(rs.getDouble("tax"));
        return g;
    }
}
