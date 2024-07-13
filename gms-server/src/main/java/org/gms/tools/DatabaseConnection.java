package org.gms.tools;

import com.alibaba.druid.pool.DruidDataSource;
import org.gms.config.YamlConfig;
import org.gms.manager.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

/**
 * @author Frz (Big Daddy)
 * @author The Real Spookster - some modifications to this beautiful code
 * @author Ronan - some connection pool to this beautiful code
 */
public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DruidDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        return ServerManager.getApplicationContext().getBean(DataSource.class).getConnection();
    }

    private static String getDbUrl() {
        // Environment variables override what's defined in the config file
        // This feature is used for the Docker support
        String hostOverride = System.getenv("DB_HOST");
        String host = hostOverride != null ? hostOverride : YamlConfig.config.server.DB_HOST;
        return String.format(YamlConfig.config.server.DB_URL_FORMAT, host);
    }

    /**
     * Initiate connection to the database
     */
    public static void initializeConnectionPool() {
        if (dataSource != null) {
            return;
        }

        try {
            Instant initStart = Instant.now();
            log.info("正在初始化数据库连接池...");
            dataSource = new DruidDataSource();
            Properties properties = new Properties();
            properties.setProperty("druid.name", "mysql");
            properties.setProperty("druid.url", getDbUrl());
            properties.setProperty("druid.username", YamlConfig.config.server.DB_USER);
            properties.setProperty("druid.password", YamlConfig.config.server.DB_PASS);
            properties.setProperty("druid.testWhileIdle", "true");
            properties.setProperty("druid.validationQuery", "SELECT 1");
            dataSource.configFromPropeties(properties);
            // 测试一次连接，避免后面报错
            dataSource.validateConnection(dataSource.getConnection());
            long initDuration = Duration.between(initStart, Instant.now()).toMillis();
            log.info("数据库连接池初始化完成，耗时：{} s", initDuration / 1000.0);
        } catch (Exception e) {
            log.error("数据库连接池初始化失败：{}", e.getMessage(), e);
        }
    }
}
