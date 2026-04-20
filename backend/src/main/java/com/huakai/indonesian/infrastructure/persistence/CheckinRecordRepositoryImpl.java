package com.huakai.indonesian.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huakai.indonesian.domain.repository.CheckinRecordRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到记录仓储实现
 * 基于 MyBatis-Plus 实现签到记录的增删查
 */
@Repository
public class CheckinRecordRepositoryImpl implements CheckinRecordRepository {

    private final CheckinRecordMapper checkinRecordMapper;

    public CheckinRecordRepositoryImpl(CheckinRecordMapper checkinRecordMapper) {
        this.checkinRecordMapper = checkinRecordMapper;
    }

    @Override
    public void save(Long userId, LocalDate checkinDate) {
        CheckinRecordPo po = new CheckinRecordPo();
        po.setUserId(userId);
        po.setCheckinDate(checkinDate);
        checkinRecordMapper.insert(po);
    }

    @Override
    public List<LocalDate> findByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<CheckinRecordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecordPo::getUserId, userId)
               .ge(CheckinRecordPo::getCheckinDate, start)
               .le(CheckinRecordPo::getCheckinDate, end)
               .orderByAsc(CheckinRecordPo::getCheckinDate);
        List<CheckinRecordPo> pos = checkinRecordMapper.selectList(wrapper);
        return pos.stream()
            .map(CheckinRecordPo::getCheckinDate)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<CheckinRecordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecordPo::getUserId, userId)
               .eq(CheckinRecordPo::getCheckinDate, date);
        return checkinRecordMapper.selectCount(wrapper) > 0;
    }
}
