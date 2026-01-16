package com.app.Livetracker.dto;

import com.app.Livetracker.entity.AssignmentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class RiderDecisionDTO {
    private Long orderId;
    private UUID riderId;
    private AssignmentStatus decision; // ACCEPTED / REJECTED
}
