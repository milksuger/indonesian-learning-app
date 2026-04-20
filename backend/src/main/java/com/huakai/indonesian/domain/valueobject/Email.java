package com.huakai.indonesian.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * 封装邮箱地址，确保格式合法，统一小写存储
 * 不可变，具有值对象相等语义
 */
public final class Email {

    /** 邮箱格式正则表达式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    /**
     * 创建邮箱值对象，自动校验格式并转为小写
     *
     * @param rawEmail 原始邮箱字符串
     * @throws IllegalArgumentException 格式不合法或为空时抛出
     */
    public Email(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException("邮箱格式不正确：邮箱不能为空");
        }
        String trimmed = rawEmail.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确：" + trimmed);
        }
        this.value = trimmed.toLowerCase();
    }

    /**
     * 获取小写邮箱地址
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email other = (Email) o;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
