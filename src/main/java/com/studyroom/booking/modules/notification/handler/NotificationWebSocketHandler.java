package com.studyroom.booking.modules.notification.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.booking.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            Long userId = extractUserId(session);
            if (userId == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未提供有效的token"));
                return;
            }
            session.getAttributes().put("userId", userId);
            sessionManager.register(userId, session);

            Map<String, Object> welcome = new HashMap<>();
            welcome.put("type", "CONNECTED");
            welcome.put("message", "WebSocket连接成功");
            welcome.put("userId", userId);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcome)));
        } catch (Exception e) {
            log.error("WebSocket连接建立失败: {}", e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String action = (String) payload.get("action");

            if ("PING".equals(action)) {
                response.put("type", "PONG");
            } else {
                response.put("type", "UNKNOWN");
            }
        } catch (Exception e) {
            response.put("type", "ERROR");
            response.put("message", "消息格式错误");
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.unregister(userId, session);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        try {
            String token = extractToken(session);
            if (token == null) {
                return null;
            }
            Claims claims = jwtUtils.parseToken(token);
            if (claims == null) {
                return null;
            }
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.warn("从WebSocket连接提取userId失败: {}", e.getMessage());
            return null;
        }
    }

    private String extractToken(WebSocketSession session) {
        if (session.getUri() != null) {
            String query = session.getUri().getQuery();
            if (query != null && query.contains("token=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        return param.substring(6);
                    }
                }
            }
        }
        return null;
    }
}