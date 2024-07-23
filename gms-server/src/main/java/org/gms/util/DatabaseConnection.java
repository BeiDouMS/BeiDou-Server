package org.gms.util;

import org.gms.manager.ServerManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster - some modifications to this beautiful code
 * @author Ronan - some connection pool to this beautiful code
 */
public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        return ServerManager.getApplicationContext().getBean(DataSource.class).getConnection();
    }
}
