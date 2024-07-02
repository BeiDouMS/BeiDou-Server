package api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ApiServer {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ApiServer.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
        log.info("API 服务已就绪");
        net.server.Server.main(args);
    }
}
