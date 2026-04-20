package com.huakai.indonesian.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 扫描 infrastructure/persistence 包下的 Mapper 接口
 */
@Configuration
@MapperScan("com.huakai.indonesian.infrastructure.persistence")
public class MybatisPlusConfig {
}
