package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 词书持久化对象（PO）
 * 对应数据库 wordbooks 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("wordbooks")
public class WordbookPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String nameEn;
    private String level;
    private Integer wordCount;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
