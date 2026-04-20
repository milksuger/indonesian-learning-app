package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户激活词书持久化对象（PO）
 * 对应数据库 user_wordbooks 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("user_wordbooks")
public class UserWordbookPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long wordbookId;
    private LocalDateTime activatedAt;
}
