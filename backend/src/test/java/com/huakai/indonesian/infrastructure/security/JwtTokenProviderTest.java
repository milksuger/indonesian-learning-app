package com.huakai.indonesian.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 令牌提供器单元测试
 * 验证令牌的生成、解析与有效性校验
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider("my-super-secret-key-for-jwt-signing-must-be-long-enough");
    }

    // Feature: indonesian-learning-app, Property 37: 生成的令牌不应为空且可被验证通过
    @Test
    void generateToken_returnsNonEmptyValidToken() {
        String token = tokenProvider.generateToken(1L, "user@example.com", "user");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    // Feature: indonesian-learning-app, Property 38: 无效令牌应校验失败
    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("invalid-token-string")).isFalse();
    }

    // Feature: indonesian-learning-app, Property 39: 篡改过的令牌应校验失败
    @Test
    void validateToken_withTamperedToken_returnsFalse() {
        String token = tokenProvider.generateToken(1L, "user@example.com", "user");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    // Feature: indonesian-learning-app, Property 40: 空令牌应校验失败
    @Test
    void validateToken_withEmptyToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("")).isFalse();
    }
}
