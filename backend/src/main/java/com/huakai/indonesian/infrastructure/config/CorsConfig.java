package com.huakai.indonesian.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 * 允许前端开发服务器访问后端 API
 * 生产环境通过 ALLOWED_ORIGINS 环境变量配置，多个来源用逗号分隔
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS");
                String[] origins;
                if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
                    origins = allowedOriginsEnv.split(",");
                } else {
                    origins = new String[]{
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:5174"
                    };
                }
                registry.addMapping("/api/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
