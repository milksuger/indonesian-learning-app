package com.huakai.indonesian.application.command;

/**
 * 学习反馈命令
 * 用户在学习新词时提交"认识"或"不认识"
 */
public record LearningFeedbackCommand(
    Long wordId,
    boolean known
) {
}
