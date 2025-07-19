
package dao;
 
import util.DBConnection;
import model.Position;
import java.sql.*;
import java.util.*;

public class PositionDAO {
    public Position getPositionById(int positionId) {
        String query = "SELECT * FROM position WHERE position_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Position p = new Position();
                p.setPositionId(rs.getInt("position_id"));
                p.setPositionName(rs.getString("position_name"));
                return p;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
