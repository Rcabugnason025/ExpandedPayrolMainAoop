
package dao;

import util.DBConnection;
import model.EmploymentStatus;
import java.sql.*;

public class EmploymentStatusDAO {
    public EmploymentStatus getStatusById(int statusId) {
        String query = "SELECT * FROM employment_status WHERE employment_status_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, statusId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                EmploymentStatus es = new EmploymentStatus();
                es.setStatusId(rs.getInt("employment_status_id"));
                es.setStatusName(rs.getString("status_name"));
                return es;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}