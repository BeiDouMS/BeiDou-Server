import com.alibaba.druid.pool.DruidDataSource;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import org.junit.jupiter.api.Test;

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
//        globalConfig.setGenerateTable("tb_account", "tb_account_session");

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
}
