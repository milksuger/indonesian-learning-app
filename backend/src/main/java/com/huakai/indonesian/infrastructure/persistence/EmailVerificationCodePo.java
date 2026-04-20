package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮箱验证码持久化对象（PO）
 * 对应数据库 email_verification_codes 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("email_verification_codes")
public class EmailVerificationCodePo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;
    private String code;
    private LocalDateTime expiresAt;
    private Integer used;
    private LocalDateTime createdAt;
}
