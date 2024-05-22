package org.gms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.gms.mapper")
public class ApiApplication {

    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(ApiApplication.class, args);
    }
}