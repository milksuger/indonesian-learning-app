package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户单词学习进度持久化对象（PO）
 * 对应数据库 user_word_progress 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("user_word_progress")
public class UserWordProgressPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long wordId;
    private String status;
    private Integer isFavorited;
    private Integer isMemorized;
    private Integer isMistake;
    private Integer srsInterval;
    private Integer srsRepetitions;
    private LocalDate nextReviewDate;
    private LocalDate firstLearnedAt;
    private LocalDate lastReviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
