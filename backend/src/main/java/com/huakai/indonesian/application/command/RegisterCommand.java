package com.huakai.indonesian.application.command;

/**
 * 用户注册命令
 * 封装注册所需的全部参数
 */
public record RegisterCommand(
    String email,
    String password,
    String verificationCode
) {
}
