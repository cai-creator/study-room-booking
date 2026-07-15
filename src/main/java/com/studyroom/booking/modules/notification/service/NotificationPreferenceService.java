package com.studyroom.booking.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.booking.modules.notification.dto.NotificationPreferenceVO;
import com.studyroom.booking.modules.notification.entity.NotificationPreference;
import com.studyroom.booking.modules.notification.mapper.NotificationPreferenceMapper;
import com.studyroom.booking.modules.space.entity.Campus;
import com.studyroom.booking.modules.space.mapper.CampusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceMapper preferenceMapper;
    private final CampusMapper campusMapper;

    /**
     * 获取用户通知偏好设置
     */
    public NotificationPreferenceVO getPreference(Long userId) {
        NotificationPreference pref = preferenceMapper.selectOne(
                new LambdaQueryWrapper<NotificationPreference>()
                        .eq(NotificationPreference::getUserId, userId)
        );

        if (pref == null) {
            return NotificationPreferenceVO.builder()
                    .bookingReminder(true)
                    .checkinReminder(true)
                    .systemNotice(true)
                    .blacklistAlert(true)
                    .build();
        }

        String campusName = null;
        if (pref.getCampusId() != null) {
            Campus campus = campusMapper.selectById(pref.getCampusId());
            if (campus != null) campusName = campus.getName();
        }

        return NotificationPreferenceVO.builder()
                .bookingReminder(pref.getBookingReminder() != null ? pref.getBookingReminder() : true)
                .checkinReminder(pref.getCheckinReminder() != null ? pref.getCheckinReminder() : true)
                .systemNotice(pref.getSystemNotice() != null ? pref.getSystemNotice() : true)
                .blacklistAlert(pref.getBlacklistAlert() != null ? pref.getBlacklistAlert() : true)
                .campusId(pref.getCampusId())
                .campusName(campusName)
                .roomType(pref.getRoomType())
                .build();
    }

    /**
     * 保存用户通知偏好设置
     */
    @Transactional
    public NotificationPreferenceVO savePreference(Long userId, NotificationPreferenceVO vo) {
        NotificationPreference pref = preferenceMapper.selectOne(
                new LambdaQueryWrapper<NotificationPreference>()
                        .eq(NotificationPreference::getUserId, userId)
        );

        if (pref == null) {
            pref = new NotificationPreference();
            pref.setUserId(userId);
            pref.setBookingReminder(vo.getBookingReminder() != null ? vo.getBookingReminder() : true);
            pref.setCheckinReminder(vo.getCheckinReminder() != null ? vo.getCheckinReminder() : true);
            pref.setSystemNotice(vo.getSystemNotice() != null ? vo.getSystemNotice() : true);
            pref.setBlacklistAlert(vo.getBlacklistAlert() != null ? vo.getBlacklistAlert() : true);
            pref.setCampusId(vo.getCampusId());
            pref.setRoomType(vo.getRoomType());
            preferenceMapper.insert(pref);
        } else {
            if (vo.getBookingReminder() != null) pref.setBookingReminder(vo.getBookingReminder());
            if (vo.getCheckinReminder() != null) pref.setCheckinReminder(vo.getCheckinReminder());
            if (vo.getSystemNotice() != null) pref.setSystemNotice(vo.getSystemNotice());
            if (vo.getBlacklistAlert() != null) pref.setBlacklistAlert(vo.getBlacklistAlert());
            pref.setCampusId(vo.getCampusId());
            pref.setRoomType(vo.getRoomType());
            preferenceMapper.updateById(pref);
        }

        log.info("用户 {} 的通知偏好已更新", userId);
        return getPreference(userId);
    }
}