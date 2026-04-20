package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录持久化对象（PO）
 * 对应数据库 checkin_records 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("checkin_records")
public class CheckinRecordPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDate checkinDate;
    private LocalDateTime createdAt;
}
