package org.gms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.gms.mapper")
public class ApiApplication {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}