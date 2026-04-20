package com.huakai.indonesian.infrastructure.external;

import com.huakai.indonesian.domain.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现
 * 基于 Spring Boot Mail 与 163 SMTP 发送验证码邮件
 * 若邮件账号未配置，则降级为控制台输出验证码（仅开发环境）
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${spring.mail.username:}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    @Async
    public void sendVerificationCode(String toEmail, String code) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("【开发模式】邮件账号未配置，验证码将输出到控制台: 邮箱={}, 验证码={}", toEmail, code);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("印尼语学习应用 - 注册验证码");
            message.setText("您的验证码是：" + code + "，有效期 10 分钟，请勿泄露给他人。");
            mailSender.send(message);
            log.info("验证码邮件发送成功: 邮箱={}", toEmail);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: 邮箱={}", toEmail, e);
        }
    }
}
