package com.huakai.indonesian.domain.service;

/**
 * 密码加密服务接口
 * 定义密码哈希与校验操作，由基础设施层提供具体实现（如 BCrypt）
 * Domain 层通过此接口解耦具体加密算法
 */
public interface PasswordEncoder {

    /**
     * 对明文密码进行哈希加密
     *
     * @param rawPassword 明文密码
     * @return 哈希后的密码字符串
     */
    String encode(String rawPassword);

    /**
     * 校验明文密码是否与已哈希密码匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 已哈希的密码
     * @return 匹配返回 true，否则返回 false
     */
    boolean matches(String rawPassword, String encodedPassword);
}
