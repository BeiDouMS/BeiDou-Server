package tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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

    private static String getDbUrl() {
        // Environment variables override what's defined in the config file
        // This feature is used for the Docker support
        String hostOverride = System.getenv("DB_HOST");
        String host = hostOverride != null ? hostOverride : YamlConfig.config.server.DB_HOST;
        String dbUrl = String.format(YamlConfig.config.server.DB_URL_FORMAT, host);
        return dbUrl;
    }

    private static HikariConfig getConfig() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(getDbUrl());
        config.setUsername(YamlConfig.config.server.DB_USER);
        config.setPassword(YamlConfig.config.server.DB_PASS);

        final int initFailTimeoutSeconds = YamlConfig.config.server.INIT_CONNECTION_POOL_TIMEOUT;
        config.setInitializationFailTimeout(TimeUnit.SECONDS.toMillis(initFailTimeoutSeconds));
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
        System.out.println("Initializing connection pool...");
        final HikariConfig config = getConfig();
        Instant initStart = Instant.now();
        try {
            dataSource = new HikariDataSource(config);
            long initDuration = Duration.between(initStart, Instant.now()).toMillis();
            System.out.printf("Connection pool initialized in %d ms%n", initDuration);
            return true;
        } catch (Exception e) {
            long timeout = Duration.between(initStart, Instant.now()).getSeconds();
            System.err.printf("Failed to initialize database connection pool. Gave up after %d seconds.%n", timeout);
        }

        // Timed out - failed to initialize
        return false;
    }
}
