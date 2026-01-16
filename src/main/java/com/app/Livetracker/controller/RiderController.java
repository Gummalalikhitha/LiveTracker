package com.app.Livetracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.app.Livetracker.dto.RiderStatusUpdateDTO;
import com.app.Livetracker.service.AuthService;

@RestController
@RequestMapping("/api/rider")
public class RiderController {

    @Autowired
    private AuthService authService;
    @PreAuthorize("hasRole('RIDER')")
    @PutMapping("/status")
    public ResponseEntity<String> updateStatus(
            @RequestBody RiderStatusUpdateDTO dto) {

        authService.updateRiderStatus(dto);
        return ResponseEntity.ok("Rider status updated successfully");
    }
}
