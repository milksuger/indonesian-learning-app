package com.huakai.indonesian.domain.service;

/**
 * 邮件服务接口
 * 定义发送验证码邮件的操作，由基础设施层提供具体实现
 */
public interface EmailService {

    /**
     * 向指定邮箱发送验证码
     *
     * @param toEmail 目标邮箱地址
     * @param code    6 位数字验证码
     */
    void sendVerificationCode(String toEmail, String code);
}
