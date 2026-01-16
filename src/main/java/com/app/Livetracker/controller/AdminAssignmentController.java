package com.app.Livetracker.controller;
import com.app.Livetracker.dto.AssignmentRequestDTO;
import com.app.Livetracker.entity.AssignmentStatus;
import com.app.Livetracker.entity.RiderAssignment;
import com.app.Livetracker.service.RiderAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/assign")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAssignmentController {

    private final RiderAssignmentService service;

    @PostMapping
    public ResponseEntity<String> assign(@RequestBody AssignmentRequestDTO dto) {
        service.assignOrder(dto);
        return ResponseEntity.ok("Order assigned to rider");
    }
    @GetMapping
    public List<RiderAssignment> getAll() {
        return service.getAllAssignments();
    }

    // ✅ Get by orderId
    @GetMapping("/order/{orderId}")
    public List<RiderAssignment> getByOrder(@PathVariable Long orderId) {
        return service.getAssignmentsByOrderId(orderId);
    }

    // ✅ Get by riderId
    @GetMapping("/rider/{riderId}")
    public List<RiderAssignment> getByRider(@PathVariable UUID riderId) {
        return service.getAssignmentsByRiderId(riderId);
    }

    // ✅ Get by status (PENDING / ACCEPTED / REJECTED / EXPIRED)
    @GetMapping("/status/{status}")
    public List<RiderAssignment> getByStatus(
            @PathVariable AssignmentStatus status) {
        return service.getAssignmentsByStatus(status);
    }


}
