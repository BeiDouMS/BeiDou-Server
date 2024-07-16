package org.gms;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        try {
            initDb(args);
        } catch (Exception e) {
            log.error("自动创建数据库失败：", e);
            return;
        }
        SpringApplication.run(ServerApplication.class, args);
    }

    /**
     * 修复PreDataSourceConfig优先级不够，导致在创建数据库之前获取连接，进而无法正常启动
     * 以下组件FlywayAutoConfiguration、HibernateJpaAutoConfiguration在启动的时候会获取数据库连接，因为库名不存在，进而一直报错
     * 无法解决在获取MybatisFlexProperties之后，在以上自动配置之前执行，进而手动解析yml来自动创建库
     */
    private static void initDb(String[] args) throws Exception {
        InputStream resource = null;
        // 解析 jvm option -Dspring.config.location
        String location = System.getProperty("spring.config.location");
        if (location != null) {
            Path path = Path.of(location);
            if (!Files.exists(path)) {
                return;
            }
            resource = Files.newInputStream(path);
        }
        if (resource  == null) {
            // 解析 spring argument --spring.config.location
            for (String arg : args) {
                if (arg.startsWith("--spring.config.location=")) {
                    location = arg.split("=")[1];
                    Path path = Path.of(location);
                    if (!Files.exists(path)) {
                        return;
                    }
                    resource = Files.newInputStream(path);
                    break;
                }
            }
        }
        if (resource == null) {
            // 解析jar包自带的yml
            resource = ServerApplication.class.getClassLoader().getResourceAsStream("application.yml");
        }
        Yaml yaml = new Yaml();
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
    }

}
