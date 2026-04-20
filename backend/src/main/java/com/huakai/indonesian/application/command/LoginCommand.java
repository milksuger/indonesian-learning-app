package com.huakai.indonesian.application.command;

/**
 * 用户登录命令
 * 封装登录所需的全部参数
 */
public record LoginCommand(
    String email,
    String password
) {
}
