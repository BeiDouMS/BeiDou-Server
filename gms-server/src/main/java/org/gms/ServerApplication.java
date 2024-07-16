package org.gms;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;

@SpringBootApplication
@MapperScan("org.gms.dao.mapper")
@Slf4j
public class ServerApplication {
    public static void main(String[] args) {
        initDb();
        SpringApplication.run(ServerApplication.class, args);
    }

    /**
     * 修复PreDataSourceConfig优先级不够，导致在创建数据库之前获取连接，进而无法正常启动
     * 以下组件FlywayAutoConfiguration、HibernateJpaAutoConfiguration在启动的时候会获取数据库连接，因为库名不存在，进而一直报错
     * 无法解决在获取MybatisFlexProperties之后，在以上自动配置之前执行，进而手动解析yml来自动创建库
     */
    private static void initDb() {
        InputStream resource = ServerApplication.class.getClassLoader().getResourceAsStream("application.yml");
        if (resource == null) {
            return;
        }
        Yaml yaml = new Yaml();
        try {
            LinkedHashMap<String, Object> property = yaml.load(resource);
            JSONObject mybatisFlex = JSONObject.parse(JSONObject.toJSONString(property.get("mybatis-flex")));
            JSONObject datasource = mybatisFlex.getJSONObject("datasource").getJSONObject("mysql");
            String dbUrl = datasource.getString("url");
            String urlPrefix = dbUrl.split("\\?")[0];
            String[] dbSplit = urlPrefix.split("/");
            String dbName = dbSplit[dbSplit.length - 1];
            String dbPrefix = urlPrefix.substring(0, urlPrefix.length() - dbName.length());
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(dbPrefix + "mysql", datasource.getString("username"), datasource.getString("password"))) {
                PreparedStatement preparedStatement = connection.prepareStatement("SHOW DATABASES LIKE '" + dbName + "'");
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return;
                }
                resultSet.close();
                preparedStatement = connection.prepareStatement("CREATE DATABASE " + dbName + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (Exception e) {
            log.error("自动创建数据库失败：", e);
            throw new RuntimeException(e);
        }
    }

}
