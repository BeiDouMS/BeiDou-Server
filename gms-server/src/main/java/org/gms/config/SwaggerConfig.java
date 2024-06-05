package org.gms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Cosmic-Nap api").description("基于Cosmic的GMS 083项目，地址：https://github.com/SleepNap/Cosmic-Nap").version("v1"));
    }
}
