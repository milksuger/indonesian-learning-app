package com.huakai.indonesian.domain.repository;

import java.util.List;

/**
 * 用户激活词书仓储接口
 */
public interface UserWordbookRepository {

    /**
     * 获取用户激活的所有词书 ID
     */
    List<Long> findActiveWordbookIdsByUserId(Long userId);

    /**
     * 激活词书
     */
    void activate(Long userId, Long wordbookId);

    /**
     * 取消激活词书
     */
    void deactivate(Long userId, Long wordbookId);

    /**
     * 判断是否已激活
     */
    boolean isActivated(Long userId, Long wordbookId);
}
