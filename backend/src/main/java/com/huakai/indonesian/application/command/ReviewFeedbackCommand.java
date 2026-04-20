package com.huakai.indonesian.application.command;

/**
 * 复习反馈命令
 * 用户在复习单词时提交"认识"或"不认识"
 */
public record ReviewFeedbackCommand(
    Long wordId,
    boolean known
) {
}
