package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单词持久化对象（PO）
 * 对应数据库 words 表，用于 MyBatis-Plus 映射
 */
@Data
@TableName("words")
public class WordPo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wordbookId;
    private String indonesian;
    private String chinese;
    private String english;
    private String exampleIndonesian;
    private String exampleZh;
    private String exampleEn;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
