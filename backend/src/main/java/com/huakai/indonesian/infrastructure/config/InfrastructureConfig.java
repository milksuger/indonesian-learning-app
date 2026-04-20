package com.huakai.indonesian.infrastructure.config;

import com.huakai.indonesian.domain.service.DailyQueueGenerator;
import com.huakai.indonesian.domain.service.PasswordEncoder;
import com.huakai.indonesian.domain.service.TokenProvider;
import com.huakai.indonesian.infrastructure.security.BCryptPasswordEncoder;
import com.huakai.indonesian.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 基础设施层 Bean 配置
 * 注册领域层依赖的外部服务实现
 */
@Configuration
@EnableAsync
public class InfrastructureConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenProvider tokenProvider(@Value("${jwt.secret:default-secret-key-must-be-replaced}") String jwtSecret) {
        return new JwtTokenProvider(jwtSecret);
    }

    @Bean
    public DailyQueueGenerator dailyQueueGenerator() {
        return new DailyQueueGenerator();
    }
}
