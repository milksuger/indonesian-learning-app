package com.huakai.indonesian.interfaces.dto;

/**
 * 用户信息响应 DTO
 * 接口层对外暴露的用户数据结构，屏蔽领域实体细节
 */
public record UserResponse(
    Long id,
    String email,
    int dailyGoal,
    String uiLanguage,
    String themeMode,
    int streak
) {
}
