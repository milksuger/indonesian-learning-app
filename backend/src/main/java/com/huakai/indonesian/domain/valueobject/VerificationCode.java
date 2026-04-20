package com.huakai.indonesian.domain.valueobject;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 邮箱验证码值对象
 * 封装 6 位数字验证码及其过期时间、使用状态
 * 过期后或使用后将失效
 */
public final class VerificationCode {

    /** 验证码格式：6 位数字 */
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");

    private final String code;
    private final LocalDateTime expiresAt;
    private boolean used;

    /**
     * 创建验证码值对象
     *
     * @param code      6 位数字验证码
     * @param expiresAt 过期时间
     * @throws IllegalArgumentException 验证码格式不合法时抛出
     */
    public VerificationCode(String code, LocalDateTime expiresAt) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("验证码必须是 6 位数字");
        }
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    /**
     * 获取验证码字符串
     */
    public String code() {
        return code;
    }

    /**
     * 判断验证码在指定时间点是否有效（未过期且未使用）
     *
     * @param now 当前时间
     * @return 有效返回 true，否则返回 false
     */
    public boolean isValidAt(LocalDateTime now) {
        return !used && now.isBefore(expiresAt);
    }

    /**
     * 标记验证码为已使用
     */
    public void markUsed() {
        this.used = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerificationCode)) return false;
        VerificationCode other = (VerificationCode) o;
        return code.equals(other.code) && expiresAt.equals(other.expiresAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(code, expiresAt);
    }

    @Override
    public String toString() {
        return "VerificationCode{code='" + code + "', expiresAt=" + expiresAt + "}";
    }
}
