package com.app.Livetracker.controller;

import com.app.Livetracker.dto.RiderDecisionDTO;
import com.app.Livetracker.service.RiderAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderDecisionController {
    private final RiderAssignmentService service;
    @PreAuthorize("hasRole('RIDER')")
    @PostMapping("/decision")
    public ResponseEntity<String> riderDecision(
            @RequestBody RiderDecisionDTO dto) {
        service.riderDecision(dto);
        return ResponseEntity.ok("Rider decision updated successfully");
    }
}
