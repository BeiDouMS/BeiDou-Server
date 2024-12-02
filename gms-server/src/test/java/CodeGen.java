import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import org.gms.ServerApplication;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Path;
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
        globalConfig.setGenerateTable("lang_resources");

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
