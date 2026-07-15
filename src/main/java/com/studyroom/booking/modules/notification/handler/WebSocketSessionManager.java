package com.studyroom.booking.modules.notification.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("用户 {} 建立WebSocket连接, 当前连接数: {}", userId, userSessions.get(userId).size());
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.info("用户 {} 断开WebSocket连接", userId);
    }

    public boolean sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        TextMessage textMessage = new TextMessage(message);
        int successCount = 0;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                    successCount++;
                } catch (IOException e) {
                    log.warn("向用户 {} 发送WebSocket消息失败: {}", userId, e.getMessage());
                }
            }
        }
        return successCount > 0;
    }

    public int getOnlineUserCount() {
        return userSessions.size();
    }

    public int getTotalConnectionCount() {
        return userSessions.values().stream().mapToInt(Set::size).sum();
    }
}