package com.huakai.indonesian.infrastructure.security;

import com.huakai.indonesian.domain.service.PasswordEncoder;

/**
 * BCrypt 密码编码器实现
 * 基于 spring-security-crypto 的 BCryptPasswordEncoder 提供密码哈希与校验
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder delegate;

    public BCryptPasswordEncoder() {
        this.delegate = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
