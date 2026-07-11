package com.studyroom.booking.modules.space.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.space.entity.SeatUnavailable;
import com.studyroom.booking.modules.space.mapper.SeatUnavailableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatUnavailableService extends ServiceImpl<SeatUnavailableMapper, SeatUnavailable> {

    public List<SeatUnavailable> getUnavailableBySeatId(Long seatId) {
        return baseMapper.selectActiveBySeatId(seatId);
    }

    public SeatUnavailable getById(Long id) {
        SeatUnavailable item = baseMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "定时不可用记录不存在");
        }
        return item;
    }

    @Transactional
    public SeatUnavailable create(Long seatId, String repeatType, LocalDateTime startDateTime,
                                  LocalDateTime endDateTime, Integer dayOfWeek, Integer dayOfMonth, String reason) {
        SeatUnavailable item = new SeatUnavailable();
        item.setSeatId(seatId);
        item.setRepeatType(repeatType);
        item.setStartDateTime(startDateTime);
        item.setEndDateTime(endDateTime);
        item.setDayOfWeek(dayOfWeek);
        item.setDayOfMonth(dayOfMonth);
        item.setReason(reason);
        item.setStatus(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.insert(item);
        log.info("创建座位定时不可用记录: seatId={}, repeatType={}, start={}, end={}", seatId, repeatType, startDateTime, endDateTime);
        return item;
    }

    @Transactional
    public SeatUnavailable update(Long id, String repeatType, LocalDateTime startDateTime,
                                  LocalDateTime endDateTime, Integer dayOfWeek, Integer dayOfMonth, String reason) {
        SeatUnavailable item = getById(id);
        item.setRepeatType(repeatType);
        item.setStartDateTime(startDateTime);
        item.setEndDateTime(endDateTime);
        item.setDayOfWeek(dayOfWeek);
        item.setDayOfMonth(dayOfMonth);
        item.setReason(reason);
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(item);
        log.info("更新座位定时不可用记录: id={}", id);
        return item;
    }

    @Transactional
    public void delete(Long id) {
        SeatUnavailable item = getById(id);
        baseMapper.deleteById(id);
        log.info("删除座位定时不可用记录: id={}", id);
    }

    public boolean isSeatCurrentlyUnavailable(Long seatId) {
        List<SeatUnavailable> list = baseMapper.selectCurrentlyUnavailable(seatId, LocalDateTime.now());
        return !list.isEmpty();
    }

    public List<SeatUnavailable> getCurrentlyUnavailable(Long seatId) {
        return baseMapper.selectCurrentlyUnavailable(seatId, LocalDateTime.now());
    }

    public boolean isSeatUnavailableInPeriod(Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        List<SeatUnavailable> rules = getUnavailableBySeatId(seatId);
        LocalDateTime now = LocalDateTime.now();
        
        for (SeatUnavailable rule : rules) {
            if (!matchesRepeatRule(rule, startTime, endTime, now)) {
                continue;
            }
            if (isTimeOverlap(rule.getStartDateTime(), rule.getEndDateTime(), startTime, endTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesRepeatRule(SeatUnavailable rule, LocalDateTime queryStart, LocalDateTime queryEnd, LocalDateTime now) {
        String repeatType = rule.getRepeatType();
        
        LocalDate ruleStartDate = rule.getStartDateTime().toLocalDate();
        LocalDate ruleEndDate = rule.getEndDateTime().toLocalDate();
        LocalDate queryDate = queryStart.toLocalDate();
        
        if (queryDate.isBefore(ruleStartDate) || queryDate.isAfter(ruleEndDate)) {
            return false;
        }

        switch (repeatType) {
            case "ONCE":
                return queryDate.equals(ruleStartDate);
            case "DAILY":
                return true;
            case "WEEKLY":
                int queryDayOfWeek = queryStart.getDayOfWeek().getValue();
                return rule.getDayOfWeek() != null && rule.getDayOfWeek() == queryDayOfWeek;
            case "MONTHLY":
                int queryDayOfMonth = queryStart.getDayOfMonth();
                return rule.getDayOfMonth() != null && rule.getDayOfMonth() == queryDayOfMonth;
            default:
                return false;
        }
    }

    private boolean isTimeOverlap(LocalDateTime ruleStart, LocalDateTime ruleEnd,
                                   LocalDateTime queryStart, LocalDateTime queryEnd) {
        java.time.LocalTime ruleStartTime = ruleStart.toLocalTime();
        java.time.LocalTime ruleEndTime = ruleEnd.toLocalTime();
        java.time.LocalTime queryStartTime = queryStart.toLocalTime();
        java.time.LocalTime queryEndTime = queryEnd.toLocalTime();

        return !(queryEndTime.isBefore(ruleStartTime) || queryStartTime.isAfter(ruleEndTime));
    }

    /**
     * 自动清理过期的不可用规则
     * 每天凌晨3点执行，逻辑删除所有已过期的规则。
     * 判断标准：end_date_time 所在日期已过今天。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void autoCleanExpiredRules() {
        LocalDate today = LocalDate.now();

        List<SeatUnavailable> expired = baseMapper.selectList(
                new LambdaQueryWrapper<SeatUnavailable>()
                        .eq(SeatUnavailable::getDeleted, 0)
                        .eq(SeatUnavailable::getStatus, 1)
                        .lt(SeatUnavailable::getEndDateTime, today.atStartOfDay())
        );

        if (expired.isEmpty()) return;

        for (SeatUnavailable rule : expired) {
            baseMapper.deleteById(rule.getId());
        }

        log.info("自动清理过期不可用规则: 共删除{}条", expired.size());
    }
}