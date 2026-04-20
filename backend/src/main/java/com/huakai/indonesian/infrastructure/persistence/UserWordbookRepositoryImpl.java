package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huakai.indonesian.domain.repository.UserWordbookRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户激活词书仓储实现
 * 基于 MyBatis-Plus 实现用户词书激活关系的增删查
 */
@Repository
public class UserWordbookRepositoryImpl implements UserWordbookRepository {

    private final UserWordbookMapper userWordbookMapper;

    public UserWordbookRepositoryImpl(UserWordbookMapper userWordbookMapper) {
        this.userWordbookMapper = userWordbookMapper;
    }

    @Override
    public List<Long> findActiveWordbookIdsByUserId(Long userId) {
        LambdaQueryWrapper<UserWordbookPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordbookPo::getUserId, userId)
               .orderByAsc(UserWordbookPo::getActivatedAt);
        List<UserWordbookPo> pos = userWordbookMapper.selectList(wrapper);
        return pos.stream()
            .map(UserWordbookPo::getWordbookId)
            .collect(Collectors.toList());
    }

    @Override
    public void activate(Long userId, Long wordbookId) {
        if (isActivated(userId, wordbookId)) {
            return;
        }
        UserWordbookPo po = new UserWordbookPo();
        po.setUserId(userId);
        po.setWordbookId(wordbookId);
        userWordbookMapper.insert(po);
    }

    @Override
    public void deactivate(Long userId, Long wordbookId) {
        LambdaQueryWrapper<UserWordbookPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordbookPo::getUserId, userId)
               .eq(UserWordbookPo::getWordbookId, wordbookId);
        userWordbookMapper.delete(wrapper);
    }

    @Override
    public boolean isActivated(Long userId, Long wordbookId) {
        LambdaQueryWrapper<UserWordbookPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserWordbookPo::getUserId, userId)
               .eq(UserWordbookPo::getWordbookId, wordbookId);
        return userWordbookMapper.selectCount(wrapper) > 0;
    }
}
