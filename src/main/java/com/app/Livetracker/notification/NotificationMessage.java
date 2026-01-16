package com.app.Livetracker.notification;

import com.app.Livetracker.entity.NotificationType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationMessage {

    private NotificationType type;
    private String message;
    private Long orderId;
    private Instant timestamp;
    private String link;
}

