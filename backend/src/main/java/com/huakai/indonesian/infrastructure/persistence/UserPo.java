package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户持久化对象（PO）
 * 对应数据库 users 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("users")
public class UserPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;
    private String passwordHash;
    private Integer dailyGoal;
    private String uiLanguage;
    private String themeMode;
    private Integer streak;
    private LocalDate lastCheckinDate;
}
