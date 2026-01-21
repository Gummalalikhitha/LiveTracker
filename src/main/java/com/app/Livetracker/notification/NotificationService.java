package com.app.Livetracker.notification;

import com.app.Livetracker.entity.Notification;
import com.app.Livetracker.entity.NotificationType;
import com.app.Livetracker.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Value("${baseurl}")
    private String baseUrl;

    private final NotificationRepository notificationRepository;

    // SSE connections
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    // ===============================
    // SUBSCRIBE
    // ===============================
    public SseEmitter subscribe(UUID userId) {

        // Keep connection alive
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        //  Initial ping (VERY IMPORTANT)
        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("connected"));
        } catch (Exception e) {
            emitters.remove(userId);
        }

        return emitter;
    }

public void send(
        UUID userId,
        UUID senderId,
        NotificationType type,
        String message,
        Long orderId,
        String link) {

    // 1️⃣ Save to DB
    Notification notification = Notification.builder()
            .userId(userId)
            .senderId(senderId)
            .type(type)
            .message(message)
            .orderId(orderId)
            .link(link)
            .read(false)
            .createdAt(Instant.now())
            .build();

    notificationRepository.save(notification);

    // 2️⃣ Push via SSE
    SseEmitter emitter = emitters.get(userId);
    if (emitter == null) return;

    NotificationMessage payload = NotificationMessage.builder()
            .id(notification.getId())
            .type(type)
            .message(message)
            .orderId(orderId)
            .senderId(senderId)
            .link(link)
            .timestamp(notification.getCreatedAt())
            .build();

    try {
        emitter.send(SseEmitter.event()
                .name(type.name())
                .data(payload));
    } catch (Exception e) {
        emitters.remove(userId);
    }
}


    // ===============================
    // TEMP TEST METHOD (AUTO FIRE)
    // ===============================
//    @PostConstruct
    public void testNotification() {
        send(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                NotificationType.ORDER_PLACED,
                "Order placed successfully",
                101L,
                baseUrl+"/api/admin/assign"
        );
    }
}