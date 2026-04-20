package com.huakai.indonesian.application.service;

import com.huakai.indonesian.domain.service.EmailService;
import com.huakai.indonesian.domain.valueobject.Email;
import com.huakai.indonesian.domain.valueobject.VerificationCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码应用服务
 * 负责生成、存储与校验验证码
 * 当前使用内存存储，后续迁移至 MySQL 持久化
 */
@Service
public class EmailVerificationService {

    /** 验证码有效期：10 分钟 */
    private static final int EXPIRATION_MINUTES = 10;
    /** 验证码长度：6 位 */
    private static final int CODE_LENGTH = 6;

    private final EmailService emailService;
    private final Map<String, VerificationCode> codeStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public EmailVerificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * 生成并发送验证码到指定邮箱
     *
     * @param email 目标邮箱值对象
     */
    public void sendVerificationCode(Email email) {
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        VerificationCode verificationCode = new VerificationCode(code, expiresAt);
        codeStore.put(email.value(), verificationCode);
        emailService.sendVerificationCode(email.value(), code);
    }

    /**
     * 校验邮箱验证码
     *
     * @param email 邮箱值对象
     * @param code  用户输入的验证码
     * @return 校验通过返回 true，否则返回 false
     */
    public boolean verifyCode(Email email, String code) {
        VerificationCode stored = codeStore.get(email.value());
        if (stored == null) {
            return false;
        }
        if (!stored.isValidAt(LocalDateTime.now())) {
            return false;
        }
        if (!stored.code().equals(code)) {
            return false;
        }
        stored.markUsed();
        return true;
    }

    /**
     * 生成随机 6 位数字验证码
     */
    private String generateCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // ========== 以下为测试辅助方法 ==========

    String getLatestCodeForTest(Email email) {
        VerificationCode vc = codeStore.get(email.value());
        return vc == null ? null : vc.code();
    }

    void injectCodeForTest(Email email, VerificationCode code) {
        codeStore.put(email.value(), code);
    }
}
