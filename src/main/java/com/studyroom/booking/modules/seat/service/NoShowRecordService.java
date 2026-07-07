package com.studyroom.booking.modules.seat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.booking.common.context.UserContext;
import com.studyroom.booking.common.exception.BusinessException;
import com.studyroom.booking.modules.seat.dto.NoShowRecordVO;
import com.studyroom.booking.modules.seat.entity.NoShowRecord;
import com.studyroom.booking.modules.seat.entity.Reservation;
import com.studyroom.booking.modules.seat.entity.SeatControl;
import com.studyroom.booking.modules.seat.mapper.NoShowRecordMapper;
import com.studyroom.booking.modules.seat.mapper.ReservationMapper;
import com.studyroom.booking.modules.seat.mapper.SeatControlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爽约记录管理服务
 *
 * @author 邓祺然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoShowRecordService {

    private final NoShowRecordMapper noShowRecordMapper;
    private final ReservationMapper reservationMapper;
    private final SeatControlMapper seatMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== 查询 =====================

    /**
     * 分页查询爽约记录（管理员）
     */
    public Page<NoShowRecordVO> getNoShowRecordPage(Integer pageNum, Integer pageSize, Long userId, String startDate, String endDate) {
        Page<NoShowRecord> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<NoShowRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(NoShowRecord::getUserId, userId);
        }

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(NoShowRecord::getRecordDate, LocalDate.parse(startDate));
        }

        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(NoShowRecord::getRecordDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(NoShowRecord::getCreatedAt);

        Page<NoShowRecord> recordPage = noShowRecordMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<NoShowRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<NoShowRecordVO> voList = recordPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询我的爽约记录（学生）
     */
    public Page<NoShowRecordVO> getMyNoShowRecords(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        return getNoShowRecordPage(pageNum, pageSize, userId, null, null);
    }

    // ===================== 创建（供定时任务和签到处调用） =====================

    /**
     * 创建爽约记录
     *
     * @param userId        用户ID
     * @param reservationId 预约ID
     * @param reason        爽约原因: NO_CHECKIN-未签到, TEMPORARY_LEAVE_TIMEOUT-暂离超时
     */
    @Transactional
    public void createNoShowRecord(Long userId, Long reservationId, String reason) {
        NoShowRecord record = new NoShowRecord();
        record.setUserId(userId);
        record.setReservationId(reservationId);
        record.setReason(reason);
        record.setRecordDate(LocalDate.now());
        noShowRecordMapper.insert(record);

        log.info("创建爽约记录: 用户={}, 预约={}, 原因={}", userId, reservationId, reason);
    }

    /**
     * 查询用户在一定天数内的爽约次数
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 爽约次数
     */
    public long getNoShowCountInDays(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return noShowRecordMapper.selectCount(
                new LambdaQueryWrapper<NoShowRecord>()
                        .eq(NoShowRecord::getUserId, userId)
                        .ge(NoShowRecord::getRecordDate, startDate)
        );
    }

    // ===================== 辅助方法 =====================

    private NoShowRecordVO convertToVO(NoShowRecord record) {
        NoShowRecordVO vo = new NoShowRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setReservationId(record.getReservationId());
        vo.setReason(record.getReason());
        vo.setRecordDate(record.getRecordDate() != null ? record.getRecordDate().toString() : null);
        vo.setCreatedAt(record.getCreatedAt() != null ? record.getCreatedAt().format(FORMATTER) : null);

        // 尝试获取预约和座位信息
        try {
            Reservation reservation = reservationMapper.selectById(record.getReservationId());
            if (reservation != null) {
                SeatControl seat = seatMapper.selectById(reservation.getSeatId());
                if (seat != null) {
                    vo.setSeatCode(seat.getSeatCode());
                }
            }
        } catch (Exception e) {
            // 预约可能已被删除，忽略
        }

        return vo;
    }
}
