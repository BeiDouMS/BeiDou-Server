package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import config.YamlConfig;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster - some modifications to this beautiful code
 * @author Ronan - some connection pool to this beautiful code
 */
public class DatabaseConnection {
    private static HikariDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("Unable to get connection from uninitialized connection pool");
        }

        return dataSource.getConnection();
    }

    private static HikariConfig getConfig() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(YamlConfig.config.server.DB_URL);
        config.setUsername(YamlConfig.config.server.DB_USER);
        config.setPassword(YamlConfig.config.server.DB_PASS);

        config.setConnectionTimeout(30 * 1000); // Hikari default
        config.setMaximumPoolSize(10); // Hikari default

        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 25);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        return config;
    }

    /**
     * Initiate connection to the database
     *
     * @return true if connection to the database initiated successfully, false if not successful
     */
    public static boolean initializeConnectionPool() {
        final int timeoutSeconds = YamlConfig.config.server.INIT_CONNECTION_POOL_TIMEOUT;
        final Instant timeout = Instant.now().plusSeconds(timeoutSeconds);

        System.out.println("Initializing connection pool...");
        final HikariConfig config = getConfig();
        HikariDataSource hikariDataSource;
        int attempt = 1;
        while (Instant.now().isBefore(timeout)) {
            try {
                hikariDataSource = new HikariDataSource(config);
            } catch (Exception e) {
                System.err.printf("Failed to initialize database connection pool after %d attempt(s)%n", attempt++);
                continue;
            }
            dataSource = hikariDataSource;
            return true;
        }

        // Timed out - failed to initialize
        return false;
    }
}
