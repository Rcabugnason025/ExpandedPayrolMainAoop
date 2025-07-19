package dao;

import util.DBConnection;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CredentialsDAO {
    private static final Logger logger = Logger.getLogger(CredentialsDAO.class.getName());

    public boolean authenticateUser(int employeeId, String password) {
        String query = "SELECT employee_id FROM credentials WHERE employee_id = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error authenticating user", ex);
            return false;
        }
    }

    public boolean updatePassword(int employeeId, String newPassword) {
        String query = "UPDATE credentials SET password = ? WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, employeeId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating password", ex);
            return false;
        }
    }

    public boolean createCredentials(int employeeId, String password) {
        String query = "INSERT INTO credentials (employee_id, password) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            stmt.setString(2, password);

            return stmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error creating credentials", ex);
            return false;
        }
    }
}
