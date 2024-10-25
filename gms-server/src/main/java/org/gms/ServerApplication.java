package org.gms;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.gms.property.ServerProperty;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@MapperScan("org.gms.dao.mapper")
@Slf4j
public class ServerApplication {
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals("tool")) {
                tool();
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
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
        if (resource == null) {
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

    private static void tool() {
        while (true) {
            System.out.println("--------------------");
            System.out.println("请选择你要进行的功能：");
            System.out.println("1.将yml配置导入数据库");
            System.out.println();
            System.out.println("提示：输入 exit 退出");
            System.out.println("--------------------");
            Scanner scanner = new Scanner(System.in);
            System.out.print("输入：");
            String choose = scanner.nextLine();
            switch (choose) {
                case "1":
                    try {
                        ymlToDB();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("错误：请输入正确的选项！");
                    break;
            }
            System.out.println();
        }
    }

    private static void ymlToDB() throws Exception {
        System.out.println("----------yml导入数据库----------");
        AtomicInteger step = new AtomicInteger();
        Scanner scanner = new Scanner(System.in);

        System.out.print(step.incrementAndGet() + ".输入旧yml路径：");
        String pathStr = scanner.nextLine();
        Path path = Path.of(pathStr);
        if (!Files.exists(path)) {
            System.out.println("错误：文件不存在！");
            return;
        }
        if (Files.isDirectory(path)) {
            path = path.resolve("application.yml");
        }

        System.out.println(step.incrementAndGet() + ".正在解析yml配置");
        FileReader fileReader = new FileReader(path.toFile());
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> property = yaml.load(fileReader);
        JSONObject datasource = JSONObject.parse(JSONObject.toJSONString(property.get("mybatis-flex")))
                .getJSONObject("datasource")
                .getJSONObject("mysql");
        String dbUrl = datasource.getString("url");
        String dbUser = datasource.getString("username");
        String dbPass = datasource.getString("password");
        JSONObject gmsProperty = JSONObject.parse(JSONObject.toJSONString(property.get("gms")));
        JSONArray worlds = gmsProperty.getJSONObject("world").getJSONArray("worlds");
        JSONObject server = gmsProperty.getJSONObject("server");

        System.out.println(step.incrementAndGet() + ".正在检查环境");
        Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        String version;
        try (PreparedStatement statement = connection.prepareStatement("select max(version) from flyway_schema_history");
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                System.out.println("错误：请先确保至少启动过一次服务！");
                return;
            }
            version = resultSet.getString(1);
            if ("1.5.3".compareTo(version) > 0) {
                System.out.println("错误：请先启动一次服务，升级完成后再进行此操作！");
                return;
            }
        }

        System.out.println(step.incrementAndGet() + ".正在生成sql");
        StringBuilder updateSql = new StringBuilder();
        for (int i = 0; i < worlds.size(); i++) {
            JSONObject world = worlds.getJSONObject(i);
            if (world.getFloat("exp_rate") == null) {
                continue;
            }
            updateSql.append("update world_prop set ")
                    .append("flag = ").append(world.getIntValue("flag")).append(", ")
                    .append("server_message = '").append(world.getString("server_message")).append("', ")
                    .append("event_message = '").append(world.getString("event_message")).append("', ")
                    .append("recommend_message = '").append(world.getString("why_am_i_recommended")).append("', ")
                    .append("channel_size = ").append(world.getIntValue("channels")).append(", ")
                    .append("exp_rate = ").append(world.getFloat("exp_rate")).append(", ")
                    .append("meso_rate = ").append(world.getFloat("meso_rate")).append(", ")
                    .append("drop_rate = ").append(world.getFloat("drop_rate")).append(", ")
                    .append("boss_drop_rate = ").append(world.getFloat("boss_drop_rate")).append(", ")
                    .append("quest_rate = ").append(world.getFloat("quest_rate")).append(", ")
                    .append("fishing_rate = ").append(world.getFloat("fishing_rate")).append(", ")
                    .append("travel_rate = ").append(world.getFloat("travel_rate")).append(", ")
                    .append("level_exp_rate = ").append(world.getFloat("level_exp_rate")).append(", ")
                    .append("quick_level = ").append(world.getFloat("quick_level")).append(", ")
                    .append("quick_level_exp_rate = ").append(world.getFloat("quick_level_exp_rate")).append(" ")
                    .append("where id = ").append(i).append(";\n");
        }
        for (Map.Entry<String, Object> entry : server.entrySet()) {
            Class<?> type = ServerProperty.class.getDeclaredField(entry.getKey()).getType();
            updateSql.append("update server_prop set ")
                            .append("prop_value = '").append(Map.class.isAssignableFrom(type) ? JSONObject.toJSONString(entry.getValue()) : entry.getValue()).append("' ")
                            .append("where prop_code = '").append(entry.getKey()).append("';\n");
        }
        String[] updateArr = updateSql.toString().split("\n");

        System.out.println(step.incrementAndGet() + ".正在更新数据库");
        for (String str : updateArr) {
            try(PreparedStatement statement = connection.prepareStatement(str)) {
                System.out.println("    执行更新：" + str);
                statement.executeUpdate();
            }
        }

        connection.close();
        System.out.println(step.incrementAndGet() + ".yml导入数据库完成");
    }

}
