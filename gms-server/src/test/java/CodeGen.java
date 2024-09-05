import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import org.gms.ServerApplication;
import org.gms.property.ServerProperty;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
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
        globalConfig.setGenerateTable("modified_cash_item");

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
}
