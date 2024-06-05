package org.gms.config;

import com.mybatisflex.spring.boot.MybatisFlexProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * 初始化完dataSource之后
 */
@Configuration
@DependsOn("dataSource")
public class PreDataSourceConfig {
    public PreDataSourceConfig(MybatisFlexProperties mybatisFlexProperties) {
        initializeDb(mybatisFlexProperties);
    }

    /**
     * 数据库不存在，自动创建数据库
     */
    private void initializeDb(MybatisFlexProperties mybatisFlexProperties) {
        Map<String, String> mysql = mybatisFlexProperties.getDatasource().get("mysql");
        String dbUrl = mysql.get("url");
        String urlPrefix = dbUrl.split("\\?")[0];
        String[] dbSplit = urlPrefix.split("/");
        String dbName = dbSplit[dbSplit.length - 1];
        String dbPrefix = urlPrefix.substring(0, urlPrefix.length() - dbName.length());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("mysql驱动的版本太低");
        }
        try (Connection connection = DriverManager.getConnection(dbPrefix + "mysql", mysql.get("username"), mysql.get("password"))) {
            PreparedStatement preparedStatement = connection.prepareStatement("SHOW DATABASES LIKE '" + dbName + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return;
            }
            resultSet.close();
            preparedStatement = connection.prepareStatement("CREATE DATABASE " + dbName + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            throw new IllegalStateException("与mysql建立连接失败");
        }
    }
}
