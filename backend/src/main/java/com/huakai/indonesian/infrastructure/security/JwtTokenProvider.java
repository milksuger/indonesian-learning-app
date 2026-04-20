package com.huakai.indonesian.infrastructure.security;

import com.huakai.indonesian.domain.service.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 令牌提供器实现
 * 基于 jjwt 库生成和校验 HS256 签名的 JWT
 * 令牌默认有效期 7 天
 */
public class JwtTokenProvider implements TokenProvider {

    /** 令牌有效期：7 天 */
    private static final long EXPIRATION_DAYS = 7;

    private final SecretKey key;

    /**
     * 使用指定密钥创建令牌提供器
     *
     * @param secret 密钥字符串，长度必须满足 HS256 要求
     */
    public JwtTokenProvider(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(EXPIRATION_DAYS, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Long extractUserId(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.valueOf(subject);
        } catch (Exception e) {
            return null;
        }
    }
}
