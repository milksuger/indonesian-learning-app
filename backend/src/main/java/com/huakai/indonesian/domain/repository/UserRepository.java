package com.huakai.indonesian.domain.repository;

import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.valueobject.Email;

/**
 * 用户聚合根仓储接口
 * 定义用户数据的持久化操作，具体实现由基础设施层提供
 */
public interface UserRepository {

    /**
     * 根据 ID 查找用户
     *
     * @param id 用户唯一标识
     * @return 用户实体，不存在时返回 null
     */
    User findById(Long id);

    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱值对象
     * @return 用户实体，不存在时返回 null
     */
    User findByEmail(Email email);

    /**
     * 保存用户（新增或更新）
     *
     * @param user 用户实体
     * @return 保存后的用户实体（可能包含生成的 ID）
     */
    User save(User user);
}
