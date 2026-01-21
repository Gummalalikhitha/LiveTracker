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

@RequiredArgsConstructor
public class AdminAssignmentController {

    private final RiderAssignmentService service;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> assign(@RequestBody AssignmentRequestDTO dto) {
        service.assignOrder(dto);
        return ResponseEntity.ok("Order assigned to rider");
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RiderAssignment> getAll() {
        return service.getAllAssignments();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/{orderId}")
    public List<RiderAssignment> getByOrder(@PathVariable Long orderId) {
        return service.getAssignmentsByOrderId(orderId);
    }


    @GetMapping("/rider/{riderId}")
    @PreAuthorize("hasRole('RIDER')")
    public List<RiderAssignment> getByRider(@PathVariable UUID riderId) {
        return service.getAssignmentsByRiderId(riderId);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN','RIDER')")
    public List<RiderAssignment> getByStatus(
            @PathVariable AssignmentStatus status) {
        return service.getAssignmentsByStatus(status);
    }
}



