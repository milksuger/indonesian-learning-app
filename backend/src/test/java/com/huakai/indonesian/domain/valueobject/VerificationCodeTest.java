package com.huakai.indonesian.domain.valueobject;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * VerificationCode 值对象的属性测试
 * 验证邮箱验证码的格式、过期与使用状态
 */
class VerificationCodeTest {

    // Feature: indonesian-learning-app, Property 14: 有效 6 位数字验证码应被接受
    @Property(tries = 200)
    void validSixDigitCode_isAccepted(@ForAll @IntRange(min = 100000, max = 999999) int codeNumber) {
        String code = String.valueOf(codeNumber);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        VerificationCode vc = new VerificationCode(code, expiresAt);
        assertThat(vc.code()).isEqualTo(code);
    }

    // Feature: indonesian-learning-app, Property 15: 非 6 位验证码应抛出异常
    @Property(tries = 100)
    void nonSixDigitCode_throwsException(@ForAll("invalidCodes") String invalidCode) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        assertThatThrownBy(() -> new VerificationCode(invalidCode, expiresAt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("验证码必须是 6 位数字");
    }

    // Feature: indonesian-learning-app, Property 16: 未过期的验证码验证应通过
    @Example
    void validCode_notExpired_passesVerification() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        VerificationCode vc = new VerificationCode("123456", future);
        assertThat(vc.isValidAt(LocalDateTime.now())).isTrue();
    }

    // Feature: indonesian-learning-app, Property 17: 已过期的验证码验证应失败
    @Example
    void expiredCode_failsVerification() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        VerificationCode vc = new VerificationCode("123456", past);
        assertThat(vc.isValidAt(LocalDateTime.now())).isFalse();
    }

    // Feature: indonesian-learning-app, Property 18: 使用后的验证码验证应失败
    @Example
    void usedCode_failsVerification() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        VerificationCode vc = new VerificationCode("123456", future);
        vc.markUsed();
        assertThat(vc.isValidAt(LocalDateTime.now())).isFalse();
    }

    // Feature: indonesian-learning-app, Property 19: 验证码标记使用后状态不可变
    @Example
    void markUsed_idempotent() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        VerificationCode vc = new VerificationCode("123456", future);
        vc.markUsed();
        vc.markUsed(); // 第二次调用不应报错
        assertThat(vc.isValidAt(LocalDateTime.now())).isFalse();
    }

    // Feature: indonesian-learning-app, Property 20: null 验证码应抛出异常
    @Example
    void nullCode_throwsException() {
        assertThatThrownBy(() -> new VerificationCode(null, LocalDateTime.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("验证码必须是 6 位数字");
    }

    @Provide
    Arbitrary<String> invalidCodes() {
        return Arbitraries.of("12345", "1234567", "abcdef", "12 345", "");
    }
}
