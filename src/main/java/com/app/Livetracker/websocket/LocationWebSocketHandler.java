package com.app.Livetracker.websocket;

import com.app.Livetracker.dto.LocationUpdateDTO;
import com.app.Livetracker.entity.Order;
import com.app.Livetracker.entity.OrderStatus;
import com.app.Livetracker.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    /**
     * Active WebSocket sessions
     * key   -> userId / riderId
     * value -> WebSocketSession
     */
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // =====================================================
    // Connection opened
    // =====================================================
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        UUID userId = (UUID) session.getAttributes().get("userId");

        if (userId != null) {
            sessions.put(userId, session);
        }
    }

    // =====================================================
    // Connection closed
    // =====================================================
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        UUID userId = (UUID) session.getAttributes().get("userId");

        if (userId != null) {
            sessions.remove(userId);
        }
    }

    // =====================================================
    // Incoming WebSocket message
    // =====================================================
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {

        UUID senderId = (UUID) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");

        if (senderId == null || role == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }

        // -------------------------------------------------
        // Parse incoming payload
        // -------------------------------------------------
        LocationUpdateDTO location =
                objectMapper.readValue(message.getPayload(), LocationUpdateDTO.class);

        // -------------------------------------------------
        // Save location to Redis
        // -------------------------------------------------
        Map<String, Object> redisPayload = new HashMap<>();
        redisPayload.put("lat", location.getLat());
        redisPayload.put("lng", location.getLng());
        redisPayload.put("role", role);
        redisPayload.put("timestamp", Instant.now().getEpochSecond());

        String redisKey = "user:location:" + senderId;
        String redisValue = objectMapper.writeValueAsString(redisPayload);

        redisTemplate.opsForValue().set(redisKey, redisValue);

        // =====================================================
        // 🚚 RIDER → USER real-time forwarding
        // =====================================================
        if ("RIDER".equals(role)) {

            Optional<Order> activeOrder =
                    orderRepository.findByRiderIdAndStatusIn(
                            senderId,
                            List.of(
                                    OrderStatus.RIDER_ACCEPTED,
                                    OrderStatus.PICKED_UP
                            )
                    );

            if (activeOrder.isEmpty()) {
                return; // rider not assigned currently
            }

            UUID userId = activeOrder.get().getUserId();
            WebSocketSession userSession = sessions.get(userId);

            if (userSession == null || !userSession.isOpen()) {
                return; // user offline
            }

            // -------------------------------------------------
            // Payload sent to USER (clean & explicit)
            // -------------------------------------------------
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("riderId", senderId);
            userPayload.put("lat", location.getLat());
            userPayload.put("lng", location.getLng());
            userPayload.put("timestamp", Instant.now().getEpochSecond());

            userSession.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(userPayload))
            );
        }
    }


}
