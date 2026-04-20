package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 单词数据访问映射器
 * 继承 MyBatis-Plus BaseMapper 提供基础 CRUD 操作
 */
@Mapper
public interface WordMapper extends BaseMapper<WordPo> {
}
