package com.huakai.indonesian.infrastructure.security;

import com.huakai.indonesian.domain.service.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BCrypt 密码编码器单元测试
 * 验证密码哈希与校验的正确性
 */
class BCryptPasswordEncoderTest {

    private PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
    }

    // Feature: indonesian-learning-app, Property 41: 哈希结果应不同于明文且非空
    @Test
    void encode_returnsNonEmptyHashDifferentFromRaw() {
        String raw = "myPassword123";
        String hash = encoder.encode(raw);

        assertThat(hash).isNotNull().isNotEmpty();
        assertThat(hash).isNotEqualTo(raw);
    }

    // Feature: indonesian-learning-app, Property 42: 正确密码应校验通过
    @Test
    void matches_withCorrectPassword_returnsTrue() {
        String raw = "correctPassword";
        String hash = encoder.encode(raw);

        assertThat(encoder.matches(raw, hash)).isTrue();
    }

    // Feature: indonesian-learning-app, Property 43: 错误密码应校验失败
    @Test
    void matches_withWrongPassword_returnsFalse() {
        String raw = "correctPassword";
        String hash = encoder.encode(raw);

        assertThat(encoder.matches("wrongPassword", hash)).isFalse();
    }

    // Feature: indonesian-learning-app, Property 44: 同一明文两次哈希结果应不同（盐值不同）
    @Test
    void encode_sameRawTwice_producesDifferentHashes() {
        String raw = "samePassword";
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(raw, hash1)).isTrue();
        assertThat(encoder.matches(raw, hash2)).isTrue();
    }
}
