package org.gms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("BeiDou api").description("基于Cosmic的GMS 083项目，地址：https://github.com/SleepNap/Cosmic-Nap").version("v1"))
                // 给swagger的所有接口装配AUTHORIZATION请求头
                .schemaRequirement(HttpHeaders.AUTHORIZATION, new SecurityScheme().type(SecurityScheme.Type.APIKEY).name(HttpHeaders.AUTHORIZATION).in(SecurityScheme.In.HEADER))
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
    }
}
