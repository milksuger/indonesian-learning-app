package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huakai.indonesian.domain.entity.User;
import com.huakai.indonesian.domain.repository.UserRepository;
import com.huakai.indonesian.domain.valueobject.Email;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储实现
 * 基于 MyBatis-Plus 实现领域层定义的仓储接口
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Long id) {
        UserPo po = userMapper.selectById(id);
        return po == null ? null : toDomain(po);
    }

    @Override
    public User findByEmail(Email email) {
        LambdaQueryWrapper<UserPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPo::getEmail, email.value());
        UserPo po = userMapper.selectOne(wrapper);
        return po == null ? null : toDomain(po);
    }

    @Override
    public User save(User user) {
        UserPo po = toPo(user);
        if (user.id() == null) {
            userMapper.insert(po);
        } else {
            userMapper.updateById(po);
        }
        return toDomain(po);
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private User toDomain(UserPo po) {
        User user = new User(po.getId(), new Email(po.getEmail()), po.getPasswordHash());
        user.changeDailyGoal(po.getDailyGoal());
        user.changeUiLanguage(po.getUiLanguage());
        user.changeThemeMode(po.getThemeMode());
        if (po.getLastCheckinDate() != null) {
            user.recordCheckin(po.getLastCheckinDate());
        }
        return user;
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private UserPo toPo(User user) {
        UserPo po = new UserPo();
        po.setId(user.id());
        po.setEmail(user.email().value());
        po.setPasswordHash(user.passwordHash());
        po.setDailyGoal(user.dailyGoal());
        po.setUiLanguage(user.uiLanguage());
        po.setThemeMode(user.themeMode());
        po.setStreak(user.streak());
        po.setLastCheckinDate(user.lastCheckinDate());
        return po;
    }
}
