package org.gms.tools;

import com.alibaba.druid.pool.DruidDataSource;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.MybatisFlexBootstrap;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.gms.config.YamlConfig;
import org.gms.database.note.NoteRowMapper;
import org.gms.manager.ServerManager;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private static Jdbi jdbi;

    public static Connection getConnection() throws SQLException {
//        if (dataSource == null) {
//            throw new IllegalStateException("数据库连接未初始化，创建连接失败！");
//        }
//
//        return dataSource.getConnection();
        return ServerManager.getApplicationContext().getBean(DataSource.class).getConnection();
    }

    public static Handle getHandle() {
        if (jdbi == null) {
            throw new IllegalStateException("数据库连接未初始化，创建jdbi失败！");
        }

        return jdbi.open();
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
     *
     * @return true if connection to the database initiated successfully, false if not successful
     */
    public static boolean initializeConnectionPool() {
        if (dataSource != null) {
            return true;
        }

        try {
            Instant initStart = Instant.now();
            log.info("正在自动更新数据库...");
            initializeDb();
            initializeSql();
            log.info("数据库更新完成，耗时：{} s", Duration.between(initStart, Instant.now()).toMillis()/ 1000.0);

            initStart = Instant.now();
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
            initializeMybatisFlex();
            initializeJdbi(dataSource);
            long initDuration = Duration.between(initStart, Instant.now()).toMillis();
            log.info("数据库连接池初始化完成，耗时：{} s", initDuration / 1000.0);
            return true;
        } catch (Exception e) {
            log.error("数据库连接池初始化失败：{}", e.getMessage(), e);
        }
        return false;
    }

    private static void initializeJdbi(DataSource dataSource) {
        jdbi = Jdbi.create(dataSource)
                .registerRowMapper(new NoteRowMapper());
    }

    /**
     * 自动创建数据库
     */
    private static void initializeDb() {
        String dbUrl = getDbUrl();
        // 分离数据库连接和库名
        String[] dbUrlParts = dbUrl.split("/");
        String dbSuffix = dbUrlParts[dbUrlParts.length - 1];
        String dbName = dbSuffix.split("\\?")[0];
        String dbPrefix = dbUrl.substring(0, dbUrl.length() - dbSuffix.length());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("mysql的版本太低，建议升级到mysql8.0以上");
        }
        try(Connection connection = DriverManager.getConnection(dbPrefix + "mysql", YamlConfig.config.server.DB_USER, YamlConfig.config.server.DB_PASS)) {
            PreparedStatement preparedStatement = connection.prepareStatement("SHOW DATABASES LIKE '" + dbName + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return;
            }
            resultSet.close();
            preparedStatement = connection.prepareStatement("CREATE DATABASE `napms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            throw new IllegalStateException("与mysql建立连接失败");
        }
    }

    /**
     * 自动执行初始化sql
     */
    private static void initializeSql() {
        String dbUrl = getDbUrl();
        Flyway.configure()
                .locations(Location.FILESYSTEM_PREFIX + "database/flyway")
                .dataSource(dbUrl, YamlConfig.config.server.DB_USER, YamlConfig.config.server.DB_PASS)
                .load()
                .migrate();
    }

    private static void initializeMybatisFlex() throws Exception {
        MybatisFlexBootstrap.getInstance()
                .setLogImpl(NoLoggingImpl.class)
                .addDataSource("mysql", dataSource);
        String mapperPackage = "org.gms.dao.mapper";
        String packagePath = mapperPackage.replaceAll("\\.", "/");
        URL resource = DatabaseConnection.class.getClassLoader().getResource(packagePath);
        if (resource == null) {
            throw new FileNotFoundException("找不到mapper路径");
        }
        File[] files = new File(resource.getPath()).listFiles();
        if (files == null) {
            throw new FileNotFoundException("找不到mapper路径");
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                MybatisFlexBootstrap.getInstance().addMapper(Class.forName(mapperPackage + "." + file.getName().replace(".class", "")));
            }
        }
        FlexGlobalConfig defaultConfig = FlexGlobalConfig.getDefaultConfig();
        // 不打印banner
        defaultConfig.setPrintBanner(false);
        FlexGlobalConfig.setDefaultConfig(defaultConfig);
        MybatisFlexBootstrap.getInstance().start();
    }
}
