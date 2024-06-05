package org.gms.config;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.gms.aop.ApiRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {
    private final ApiRequestInterceptor apiRequestInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(apiRequestInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/test/**", "/*/login");
    }
}
