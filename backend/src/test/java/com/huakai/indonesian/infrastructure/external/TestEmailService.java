package com.huakai.indonesian.infrastructure.external;

import com.huakai.indonesian.domain.service.EmailService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 测试环境邮件服务 Mock 实现
 * 仅打印日志，不真正发送邮件
 */
@Service
@Primary
@Profile("test")
public class TestEmailService implements EmailService {

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        System.out.println("[TestEmail] 向 " + toEmail + " 发送验证码: " + code);
    }
}
