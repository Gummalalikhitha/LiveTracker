package com.app.Livetracker.dto;

import com.app.Livetracker.entity.NotificationType;
import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequestDTO {
    private UUID receiverId;
    private UUID senderId;
    private NotificationType type;
    private String message;
    private Long orderId;
    private String link;
}
