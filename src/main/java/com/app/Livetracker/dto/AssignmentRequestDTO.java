package com.app.Livetracker.dto;

import lombok.Data;

import java.util.UUID;


@Data
public class AssignmentRequestDTO {
    private Long orderId;
    private UUID riderId;
}
