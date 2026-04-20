package com.huakai.indonesian.application.service;

import com.huakai.indonesian.domain.service.EmailService;
import com.huakai.indonesian.domain.valueobject.Email;
import com.huakai.indonesian.domain.valueobject.VerificationCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 邮箱验证码服务单元测试
 * 验证验证码生成、校验与过期清理逻辑
 */
class EmailVerificationServiceTest {

    private EmailVerificationService service;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        service = new EmailVerificationService(emailService);
    }

    // Feature: indonesian-learning-app, Property 60: 发送验证码应调用邮件服务
    @Test
    void sendCode_callsEmailServiceWithValidCode() {
        Email email = new Email("user@example.com");

        service.sendVerificationCode(email);

        verify(emailService, times(1)).sendVerificationCode(eq("user@example.com"), any(String.class));
    }

    // Feature: indonesian-learning-app, Property 61: 正确验证码校验应通过
    @Test
    void verifyCode_withValidCode_returnsTrue() {
        Email email = new Email("user@example.com");
        service.sendVerificationCode(email);

        String code = service.getLatestCodeForTest(email);
        boolean result = service.verifyCode(email, code);

        assertThat(result).isTrue();
    }

    // Feature: indonesian-learning-app, Property 62: 错误验证码校验应失败
    @Test
    void verifyCode_withWrongCode_returnsFalse() {
        Email email = new Email("user@example.com");
        service.sendVerificationCode(email);

        boolean result = service.verifyCode(email, "999999");

        assertThat(result).isFalse();
    }

    // Feature: indonesian-learning-app, Property 63: 过期验证码校验应失败
    @Test
    void verifyCode_withExpiredCode_returnsFalse() {
        Email email = new Email("user@example.com");
        VerificationCode expiredCode = new VerificationCode("123456", LocalDateTime.now().minusMinutes(11));
        service.injectCodeForTest(email, expiredCode);

        boolean result = service.verifyCode(email, "123456");

        assertThat(result).isFalse();
    }

    // Feature: indonesian-learning-app, Property 64: 已使用的验证码校验应失败
    @Test
    void verifyCode_afterUsed_returnsFalse() {
        Email email = new Email("user@example.com");
        service.sendVerificationCode(email);
        String code = service.getLatestCodeForTest(email);

        service.verifyCode(email, code); // 第一次使用
        boolean secondAttempt = service.verifyCode(email, code); // 第二次使用

        assertThat(secondAttempt).isFalse();
    }

    // Feature: indonesian-learning-app, Property 65: 不存在的邮箱校验应失败
    @Test
    void verifyCode_nonExistentEmail_returnsFalse() {
        Email email = new Email("nobody@example.com");
        boolean result = service.verifyCode(email, "123456");
        assertThat(result).isFalse();
    }
}
