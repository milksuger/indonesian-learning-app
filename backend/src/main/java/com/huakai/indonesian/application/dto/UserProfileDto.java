package com.huakai.indonesian.application.dto;

import java.time.LocalDate;

/**
 * 用户个人资料数据传输对象
 */
public record UserProfileDto(
    Long id,
    String email,
    int dailyGoal,
    String uiLanguage,
    String themeMode,
    int streak,
    LocalDate lastCheckinDate
) {
}
