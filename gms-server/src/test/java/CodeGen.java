import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import org.gms.ServerApplication;
import org.gms.client.command.CommandsExecutor;
import org.gms.property.ServerProperty;
import org.gms.util.Pair;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CodeGen {
    @Test
    public void genMapperAndEntity() {
        DruidDataSource dataSource = new DruidDataSource();
        Properties properties = new Properties();
        properties.setProperty("druid.name", "mysql");
        properties.setProperty("druid.url", "jdbc:mysql://localhost:3306/beidou?useInformationSchema=true&characterEncoding=utf-8");
        properties.setProperty("druid.username", "root");
        properties.setProperty("druid.password", "root");
        properties.setProperty("druid.testWhileIdle", "true");
        properties.setProperty("druid.validationQuery", "SELECT 1");
        dataSource.configFromPropeties(properties);

        GlobalConfig globalConfig = new GlobalConfig();
        //设置根包
        globalConfig.setBasePackage("org.gms.dao");
        //设置表前缀和只生成哪些表
//        globalConfig.setTablePrefix("tb_");
        globalConfig.setGenerateTable("world_prop", "server_prop");

        //设置生成 entity 并启用 Lombok
        globalConfig.setEntityGenerateEnable(true);
        globalConfig.setEntityWithLombok(true);
        globalConfig.setEntityClassSuffix("DO");
        globalConfig.setEntityJdkVersion(21);

        //设置生成 mapper
        globalConfig.setMapperGenerateEnable(true);

        Generator generator = new Generator(dataSource, globalConfig);
        generator.generate();
    }

    @Deprecated(since = "1.6", forRemoval = true)
    @Test
    public void genPropSql() throws Exception {
        final String[] WORLD_NAMES = {"Scania", "Bera", "Broa", "Windia", "Khaini", "Bellocan", "Mardia", "Kradia", "Yellonde", "Demethos", "Galicia", "El Nido", "Zenith", "Arcenia", "Kastia", "Judis", "Plana", "Kalluna", "Stius", "Croa", "Medere"};

        InputStream resource = ServerApplication.class.getClassLoader().getResourceAsStream("application.yml");
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> property = yaml.load(resource);
        JSONObject gmsProperty = JSONObject.parse(JSONObject.toJSONString(property.get("gms")));

        StringBuilder worldPropBuilder = new StringBuilder();
        worldPropBuilder.append("insert into world_prop (id, flag, server_message, event_message, recommend_message, channel_size, exp_rate, meso_rate, drop_rate, boss_drop_rate, quest_rate, fishing_rate, travel_rate, level_exp_rate, quick_level, quick_level_exp_rate, enabled) values ");
        for (int i = 0; i < WORLD_NAMES.length; i++) {
            worldPropBuilder.append("(")
                    // id
                    .append(i).append(", ")
                    // flag
                    .append(0).append(", ")
                    // server_message
                    .append("'Welcome to ").append(WORLD_NAMES[i]).append("!'").append(", ")
                    // event_message
                    .append("'").append(WORLD_NAMES[i]).append("'").append(", ")
                    // recommend_message
                    .append("'Welcome to ").append(WORLD_NAMES[i]).append("!'").append(", ")
                    // channel_size
                    .append(3).append(", ")
                    // exp_rate
                    .append(1.0).append(", ")
                    // meso_rate
                    .append(1.0).append(", ")
                    // drop_rate
                    .append(1.0).append(", ")
                    // boss_drop_rate
                    .append(1.0).append(", ")
                    // quest_rate
                    .append(1.0).append(", ")
                    // fishing_rate
                    .append(1.0).append(", ")
                    // travel_rate
                    .append(1.0).append(", ")
                    // level_exp_rate
                    .append(0).append(", ")
                    // quick_level
                    .append(0).append(", ")
                    // quick_level_exp_rate
                    .append(0).append(", ")
                    // enabled
                    .append(i == 0 ? 1 : 0)
                    .append(")").append(i < WORLD_NAMES.length - 1 ? ", " : ";");
        }
        System.out.println(worldPropBuilder);

        JSONObject serverProperty = gmsProperty.getJSONObject("server");
        StringBuilder serverPropBuilder = new StringBuilder();
        serverPropBuilder.append("insert into server_prop (prop_type, prop_code, prop_class, prop_value, prop_desc) values ");
        for (Map.Entry<String, Object> entry : serverProperty.entrySet()) {
            Class<?> type = ServerProperty.class.getDeclaredField(entry.getKey()).getType();
            serverPropBuilder.append("(")
                    // prop_type
                    .append("null").append(", ")
                    // prop_code
                    .append("'").append(entry.getKey()).append("'").append(", ")
                    // prop_class
                    .append("'").append(type.getSimpleName()).append("'").append(", ")
                    // prop_value
                    .append("'").append(Map.class.isAssignableFrom(type) ? JSONObject.toJSONString(entry.getValue()) : entry.getValue()).append("'").append(", ")
                    // prop_desc
                    .append("null")
                    .append(")").append(", ");
        }
        serverPropBuilder.deleteCharAt(serverPropBuilder.length() - 2);
        serverPropBuilder.append(";");
        System.out.println(serverPropBuilder);
    }

    @Test
    public void genCommandSql() throws Exception {
        Path path = Path.of("E:\\LocalGit\\OpenSource\\BeiDou-Server\\gms-server\\src\\main\\java\\org\\gms\\client\\command\\CommandsExecutor.java");
        try (FileReader fr = new FileReader(path.toFile());
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            boolean methodStated = false;
            String currLv = "";
            StringBuilder sqlBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected")) {
                    if (line.endsWith("{") && line.contains("registerLv")) {
                        methodStated = true;
                        int index = line.indexOf("registerLv") + 10;
                        currLv = line.substring(index, index + 1);
                    }
                    continue;
                }
                if (line.startsWith("}") && methodStated) {
                    methodStated = false;
                    continue;
                }
                if (!methodStated) {
                    continue;
                }
                if (!line.startsWith("addCommand")) {
                    continue;
                }
                String[] splits = line.split(",");
                String lastSplit = splits[splits.length - 1].trim();
                int clzIdx = lastSplit.indexOf(".class");
                String clz = lastSplit.substring(0, clzIdx);
                if (line.contains("{\"")) {
                    int start = line.indexOf("{\"");
                    int end = line.indexOf("\"}");
                    line = line.substring(start + 2, end);
                    line = line.replace("\"", "");
                    splits = line.split(",");
                    for (String split : splits) {
                        appendSql(sqlBuilder, split.trim(), currLv, clz);
                    }
                } else {
                    int start = line.indexOf("(\"");
                    int end = line.indexOf("\",");
                    line = line.substring(start + 2, end);
                    appendSql(sqlBuilder, line.trim(), currLv, clz);
                }
            }
            System.out.println(sqlBuilder);
        }
    }

    private void appendSql(StringBuilder sqlBuilder, String syntax, String level, String clz) {
        sqlBuilder.append("INSERT INTO command_info (syntax, level, enabled, clazz, default_level) VALUES (")
                .append("'").append(syntax).append("', ") // syntax
                .append(level).append(", ") // level
                .append(1).append(", ") // enabled
                .append("'").append(clz).append("'").append(", ") // clazz
                .append(level) // default_level
                .append(");\n");

    }
}
