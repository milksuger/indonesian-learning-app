package com.huakai.indonesian.interfaces.security;

import com.huakai.indonesian.infrastructure.security.JwtTokenProvider;
import org.springframework.stereotype.Component;

/**
 * JWT 认证辅助类
 * 从 HTTP Authorization 请求头中提取并校验用户身份
 */
@Component
public class JwtAuthHelper {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthHelper(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * 从 Authorization 请求头中提取用户 ID
     *
     * @param authHeader 请求头值，格式：Bearer {token}
     * @return 用户 ID
     * @throws IllegalArgumentException 令牌缺失或无效
     */
    public Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("缺少有效的认证令牌");
        }
        String token = authHeader.substring(7);
        if (!tokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("认证令牌无效或已过期");
        }
        Long userId = tokenProvider.extractUserId(token);
        if (userId == null) {
            throw new IllegalArgumentException("无法从令牌中提取用户身份");
        }
        return userId;
    }
}
