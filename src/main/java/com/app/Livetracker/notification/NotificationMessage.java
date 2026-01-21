package com.app.Livetracker.notification;

import com.app.Livetracker.entity.NotificationType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationMessage {

    private Long id;
    private NotificationType type;
    private String message;
    private Long orderId;
    private UUID senderId;
    private String link;
    private Instant timestamp;
}
