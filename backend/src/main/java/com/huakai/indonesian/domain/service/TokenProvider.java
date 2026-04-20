package com.huakai.indonesian.domain.service;

/**
 * JWT 令牌提供器接口
 * 定义令牌的生成与校验操作，由基础设施层提供具体实现
 * Domain 层通过此接口解耦具体令牌技术
 */
public interface TokenProvider {

    /**
     * 生成访问令牌
     *
     * @param userId 用户唯一标识
     * @param email  用户邮箱
     * @param role   用户角色（user/admin）
     * @return JWT 字符串
     */
    String generateToken(Long userId, String email, String role);

    /**
     * 验证令牌是否有效
     *
     * @param token JWT 字符串
     * @return 有效返回 true，否则返回 false
     */
    boolean validateToken(String token);

    /**
     * 从令牌中提取用户 ID
     *
     * @param token JWT 字符串
     * @return 用户 ID，提取失败返回 null
     */
    Long extractUserId(String token);
}
