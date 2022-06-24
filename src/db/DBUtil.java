package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Thushara Supun
 */
public class DBUtil {

    private static PreparedStatement getPreparedStatement(String sql, Object... params) throws Exception {

        Connection connection = DBConnection.getInstance().getConnection();

        PreparedStatement pstm = connection.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstm.setObject(i + 1, params[i]);
        }
        return pstm;
    }

    public static boolean executeUpdate(String sql, Object... parms) throws Exception {
        return getPreparedStatement(sql, parms).executeUpdate() > 0;
    }

    public static ResultSet executeQuery(String sql, Object... parms) throws Exception {
        return getPreparedStatement(sql, parms).executeQuery();
    }

}
