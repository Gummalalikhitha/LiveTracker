package com.app.Livetracker.notification;

import com.app.Livetracker.dto.NotificationRequestDTO;
import com.app.Livetracker.entity.Notification;
import com.app.Livetracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // ===============================
    // SSE SUBSCRIBE
    // ===============================
    @GetMapping("/subscribe")
    public SseEmitter subscribe(
            @AuthenticationPrincipal UUID userId)
    {
        return notificationService.subscribe(userId);
    }

    // ===============================
    // GET HISTORY
    // ===============================
    @GetMapping
    public List<Notification> getMyNotifications(
            @AuthenticationPrincipal UUID userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ===============================
    // UNREAD COUNT (BADGE)
    // ===============================
    @GetMapping("/unread-count")
    public long unreadCount(
            @AuthenticationPrincipal UUID userId) {

        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }

    // ===============================
    // MARK AS READ
    // ===============================
    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationRepository.findById(id)
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }
    // ===============================
// SSE TEST (NO AUTH - TEMPORARY)
// ===============================
    @GetMapping("/subscribe-test")
    public SseEmitter subscribeTest() {
        return notificationService.subscribe(
                UUID.fromString("11111111-1111-1111-1111-111111111111")
        );
    }
        @GetMapping("/fire-test")
    public void fireTest() {
        notificationService.testNotification();
    }
    @PostMapping("/send")
    public void sendNotification(
            @RequestBody NotificationRequestDTO dto) {

        notificationService.send(
                dto.getReceiverId(),
                dto.getSenderId(),
                dto.getType(),
                dto.getMessage(),
                dto.getOrderId(),
                dto.getLink()

        );
    }
}
