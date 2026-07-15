package com.studyroom.booking.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.booking.modules.notification.entity.Notification;
import com.studyroom.booking.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    public void sendNotification(Long userId, String type, String title, String content, String data) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setData(data);
        notification.setReadFlag(0);
        notification.setExpireAt(LocalDateTime.now().plusDays(7));
        notification.setCreatedAt(LocalDateTime.now());
        save(notification);
    }

    public void sendBatchNotification(List<Long> userIds, String type, String title, String content, String data) {
        for (Long userId : userIds) {
            sendNotification(userId, type, title, content, data);
        }
    }

    public Page<Notification> getUserNotifications(Long userId, int pageNum, int pageSize, Integer readFlag) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (readFlag != null) wrapper.eq(Notification::getReadFlag, readFlag);
        wrapper.le(Notification::getExpireAt, LocalDateTime.now());
        wrapper.orderByDesc(Notification::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadFlag, 0)
                .ge(Notification::getExpireAt, LocalDateTime.now()));
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setReadFlag(1);
            updateById(notification);
        }
    }

    public void markAllRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getReadFlag, 0);
        List<Notification> list = list(wrapper);
        for (Notification notification : list) {
            notification.setReadFlag(1);
        }
        updateBatchById(list);
    }

    public void cleanExpired() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Notification::getExpireAt, LocalDateTime.now());
        remove(wrapper);
    }
}