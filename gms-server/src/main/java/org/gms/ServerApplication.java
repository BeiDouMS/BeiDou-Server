package org.gms;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
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
        System.out.println("    注意：因为部分参数已变动，并非所有参数能匹配并更新成功！");

        System.out.println(step.incrementAndGet() + ".正在生成sql");
        StringBuilder updateSql = new StringBuilder();
        for (int i = 0; i < worlds.size(); i++) {
            JSONObject world = worlds.getJSONObject(i);
            if (world.getFloat("exp_rate") == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : world.entrySet()) {
                String configCode = entry.getKey().toLowerCase();
                if ("why_am_i_recommended".equals(configCode)) {
                    configCode = "recommend_message";
                }
                if ("channels".equals(configCode)) {
                    configCode = "channel_size";
                }
                updateSql.append("update game_config set ")
                        .append("config_value = '")
                        .append(entry.getValue())
                        .append("' where config_type = 'world' and config_sub_type = '")
                        .append(i)
                        .append("' and config_code = '")
                        .append(configCode)
                        .append("';\n");
            }
        }
        for (Map.Entry<String, Object> entry : server.entrySet()) {
            String configCode = entry.getKey().toLowerCase();
            if ("wldlist_size".equals(configCode)) {
                configCode = "max_world_size";
            }
            if ("channel_size".equals(configCode)) {
                configCode = "max_channel_size";
            }
            if ("channel_load".equals(configCode)) {
                configCode = "channel_capacity";
            }
            if ("host".equals(configCode)) {
                configCode = "wan_host";
            }
            if ("lanhost".equals(configCode)) {
                configCode = "lan_host";
            }
            if ("use_debug_show_rcvd_mvlife".equals(configCode)) {
                configCode = "use_debug_show_life_move";
            }
            if (configCode.startsWith("use_maxrange")) {
                configCode = configCode.replace("use_maxrange", "use_max_range");
            }
            if (configCode.contains("charslot")) {
                configCode = configCode.replace("charslot", "chr_slot");
            }
            if (configCode.contains("multiclient")) {
                configCode = configCode.replace("multiclient", "multi_client");
            }
            if (configCode.contains("keyset")) {
                configCode = configCode.replace("keyset", "key_set");
            }
            if (configCode.contains("eqpexp")) {
                configCode = configCode.replace("eqpexp", "eqp_exp");
            }
            if (configCode.contains("autoassign")) {
                configCode = configCode.replace("autoassign", "auto_assign");
            }
            if (configCode.contains("autoban")) {
                configCode = configCode.replace("autoban", "auto_ban");
            }
            if (configCode.contains("openshop")) {
                configCode = configCode.replace("openshop", "open_shop");
            }
            if (configCode.contains("shopitemsold")) {
                configCode = configCode.replace("shopitemsold", "shop_item_sold");
            }
            if (configCode.contains("cashshop")) {
                configCode = configCode.replace("cashshop", "cash_shop");
            }
            if (configCode.contains("atkup")) {
                configCode = configCode.replace("atkup", "atk_up");
            }
            if (configCode.contains("unitprice")) {
                configCode = configCode.replace("unitprice", "unit_price");
            }
            if (configCode.contains("buffstat")) {
                configCode = configCode.replace("buffstat", "buff_stat");
            }
            if (configCode.contains("autoaggro")) {
                configCode = configCode.replace("autoaggro", "auto_aggro");
            }
            if (configCode.contains("chscroll")) {
                configCode = configCode.replace("chscroll", "chaos_scroll");
            }
            if (configCode.contains("skillset")) {
                configCode = configCode.replace("skillset", "skill_set");
            }
            if (configCode.contains("equipmnt")) {
                configCode = configCode.replace("equipmnt", "equipment");
            }
            if (configCode.contains("lvlup")) {
                configCode = configCode.replace("lvlup", "level_up");
            }
            if (configCode.contains("levelup")) {
                configCode = configCode.replace("levelup", "level_up");
            }
            if (configCode.contains("extraheal")) {
                configCode = configCode.replace("extraheal", "extra_heal");
            }
            if (configCode.contains("autopot")) {
                configCode = configCode.replace("autopot", "auto_pot");
            }
            if (configCode.contains("autohp")) {
                configCode = configCode.replace("autohp", "auto_hp");
            }
            if (configCode.contains("automp")) {
                configCode = configCode.replace("automp", "auto_mp");
            }
            Object configValue = entry.getValue();
            if ("npcs_scriptable".equalsIgnoreCase(entry.getKey())) {
                configValue = JSONObject.toJSONString(entry.getValue());
            }
            updateSql.append("update game_config set ")
                    .append("config_value = '")
                    .append(configValue)
                    .append("' where config_type = 'server'")
                    .append(" and config_code = '")
                    .append(configCode)
                    .append("';\n");
        }
        String[] updateArr = updateSql.toString().split("\n");

        System.out.println(step.incrementAndGet() + ".正在更新数据库");
        for (String str : updateArr) {
            try(PreparedStatement statement = connection.prepareStatement(str)) {
                System.out.println("    执行更新：" + str);
//                statement.executeUpdate();
            }
        }

        connection.close();
        System.out.println(step.incrementAndGet() + ".yml导入数据库完成");
    }

}
