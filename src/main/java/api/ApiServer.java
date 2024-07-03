package api;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("dao.mapper")
@ComponentScan(basePackages = {"api", "dao", "service", "model", "config", "utils"})
@Slf4j
public class ApiServer {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ApiServer.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
        log.info("启动完毕");
    }
}
