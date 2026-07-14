package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.ResultCode;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.seat.dto.BlacklistVO;
import com.studyroom.booking.modules.seat.entity.Blacklist;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.mapper.BlacklistMapper;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 黑名单管理服务
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistMapper blacklistMapper;
    private final NoShowRecordMapper noShowRecordMapper;

    /** 黑名单爽约阈值（7天内），默认3次 */
    @Value("${booking.rules.blacklist-threshold:3}")
    private int blacklistThreshold;

    /** 黑名单持续天数，默认7天 */
    @Value("${booking.rules.blacklist-days:7}")
    private int blacklistDays;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== 查询 =====================

    /**
     * 分页查询黑名单列表（管理员）
     */
    public Page<BlacklistVO> getBlacklistPage(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        pageSize = Math.min(pageSize, 100);
        Page<Blacklist> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（通过userId关联，这里用简单的userId匹配）
        // 实际应关联user表查询，此处通过userId字段模糊匹配
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Blacklist::getReason, keyword)
            );
        }

        if (status != null) {
            wrapper.eq(Blacklist::getStatus, status);
        }

        wrapper.orderByDesc(Blacklist::getCreatedAt);

        Page<Blacklist> blacklistPage = blacklistMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<BlacklistVO> voPage = new Page<>(blacklistPage.getCurrent(), blacklistPage.getSize(), blacklistPage.getTotal());
        List<BlacklistVO> voList = blacklistPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 获取黑名单详情
     */
    public BlacklistVO getBlacklistById(Long id) {
        Blacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null) {
            throw new BusinessException(ResultCode.BLACKLIST_NOT_FOUND);
        }
        return convertToVO(blacklist);
    }

    /**
     * 查看当前用户的黑名单状态（学生）
     */
    public BlacklistVO getMyBlacklistStatus() {
        Long userId = UserContext.getUserId();

        Blacklist blacklist = blacklistMapper.selectOne(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, 1)
                        .orderByDesc(Blacklist::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (blacklist == null) {
            return null;
        }

        return convertToVO(blacklist);
    }

    // ===================== 手动管理 =====================

    /**
     * 手动加入黑名单（管理员）
     */
    @Transactional
    public BlacklistVO addToBlacklist(Long userId, String reason, String endTimeStr) {
        Long operatorId = UserContext.getUserId();

        // 检查是否已在黑名单中
        Blacklist existing = blacklistMapper.selectOne(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, 1)
                        .last("LIMIT 1")
        );

        if (existing != null) {
            throw new BusinessException(ResultCode.BLACKLIST_ALREADY_EXISTS);
        }

        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);

        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setReason(reason);
        blacklist.setNoShowCount(0);
        blacklist.setStartTime(LocalDateTime.now());
        blacklist.setEndTime(endTime);
        blacklist.setStatus(1);
        blacklist.setOperatorId(operatorId);

        blacklistMapper.insert(blacklist);

        log.info("管理员 {} 将用户 {} 加入黑名单，结束时间: {}", operatorId, userId, endTimeStr);

        return convertToVO(blacklist);
    }

    /**
     * 手动移出黑名单（管理员）
     */
    @Transactional
    public void removeFromBlacklist(Long id) {
        Blacklist blacklist = blacklistMapper.selectById(id);
        if (blacklist == null) {
            throw new BusinessException(ResultCode.BLACKLIST_NOT_FOUND);
        }

        blacklist.setStatus(0);
        blacklistMapper.updateById(blacklist);

        log.info("管理员 {} 将黑名单记录 {} 解除", UserContext.getUserId(), id);
    }

    // ===================== 自动管理（供定时任务调用） =====================

    /**
     * 自动检查并加入黑名单
     * <p>
     * 检查所有用户在过去N天内的爽约次数，达到阈值则自动加入黑名单。
     * 由定时任务定期调用。
     */
    @Transactional
    public void autoCheckAndAddToBlacklist() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        // 查询7天内所有爽约记录
        List<NoShowRecord> recentRecords = noShowRecordMapper.selectList(
                new LambdaQueryWrapper<NoShowRecord>()
                        .ge(NoShowRecord::getRecordDate, sevenDaysAgo)
        );

        // 按用户分组统计爽约次数
        java.util.Map<Long, Long> userNoShowCount = recentRecords.stream()
                .collect(Collectors.groupingBy(NoShowRecord::getUserId, Collectors.counting()));

        // 检查每个用户是否应加入黑名单
        for (java.util.Map.Entry<Long, Long> entry : userNoShowCount.entrySet()) {
            Long userId = entry.getKey();
            Long count = entry.getValue();

            if (count >= blacklistThreshold) {
                // 检查是否已在黑名单中
                Blacklist existing = blacklistMapper.selectOne(
                        new LambdaQueryWrapper<Blacklist>()
                                .eq(Blacklist::getUserId, userId)
                                .eq(Blacklist::getStatus, 1)
                                .last("LIMIT 1")
                );

                if (existing == null) {
                    // 自动加入黑名单
                    Blacklist blacklist = new Blacklist();
                    blacklist.setUserId(userId);
                    blacklist.setReason(String.format("7天内累计爽约%d次，系统自动加入黑名单", count));
                    blacklist.setNoShowCount(count.intValue());
                    blacklist.setStartTime(LocalDateTime.now());
                    blacklist.setEndTime(LocalDateTime.now().plusDays(blacklistDays));
                    blacklist.setStatus(1);
                    blacklist.setOperatorId(null); // 系统自动操作，无操作人

                    blacklistMapper.insert(blacklist);

                    log.info("用户 {} 因{}天内爽约{}次，自动加入黑名单 {} 天",
                            userId, 7, count, blacklistDays);
                }
            }
        }
    }

    /**
     * 自动解除过期的黑名单
     * <p>
     * 由定时任务定期调用。
     */
    @Transactional
    public void autoReleaseExpiredBlacklist() {
        List<Blacklist> expiredList = blacklistMapper.selectList(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getStatus, 1)
                        .lt(Blacklist::getEndTime, LocalDateTime.now())
        );

        for (Blacklist blacklist : expiredList) {
            blacklist.setStatus(0);
            blacklistMapper.updateById(blacklist);

            log.info("用户 {} 的黑名单已到期自动解除", blacklist.getUserId());
        }
    }

    /**
     * 检查用户是否在黑名单中（供预约模块调用）
     *
     * @param userId 用户ID
     * @return true-在黑名单中
     */
    public boolean isUserBlacklisted(Long userId) {
        Blacklist blacklist = blacklistMapper.selectOne(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getUserId, userId)
                        .eq(Blacklist::getStatus, 1)
                        .last("LIMIT 1")
        );
        return blacklist != null;
    }

    // ===================== 辅助方法 =====================

    private BlacklistVO convertToVO(Blacklist blacklist) {
        BlacklistVO vo = new BlacklistVO();
        vo.setId(blacklist.getId());
        vo.setUserId(blacklist.getUserId());
        vo.setReason(blacklist.getReason());
        vo.setNoShowCount(blacklist.getNoShowCount());
        vo.setStartTime(blacklist.getStartTime() != null ? blacklist.getStartTime().format(FORMATTER) : null);
        vo.setEndTime(blacklist.getEndTime() != null ? blacklist.getEndTime().format(FORMATTER) : null);
        vo.setStatus(blacklist.getStatus());
        vo.setCreatedAt(blacklist.getCreatedAt() != null ? blacklist.getCreatedAt().format(FORMATTER) : null);
        return vo;
    }
}
