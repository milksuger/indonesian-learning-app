package com.huakai.indonesian.application.command;

/**
 * 更新用户设置命令
 */
public record UpdateSettingsCommand(
    Integer dailyGoal,
    String uiLanguage,
    String themeMode
) {
}
