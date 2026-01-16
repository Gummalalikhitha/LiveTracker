package com.app.Livetracker.controller;

import com.app.Livetracker.dto.NearestRiderResponseDTO;
import com.app.Livetracker.service.AdminRiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/riders")
@RequiredArgsConstructor
public class AdminRiderController {

    private final AdminRiderService adminRiderService;

    // =====================================================
    // ADMIN: FETCH NEAREST AVAILABLE RIDERS
    // =====================================================
    @GetMapping("/nearest")
    @PreAuthorize("hasRole('ADMIN')")
    public List<NearestRiderResponseDTO> getNearestAvailableRiders() {
        return adminRiderService.getNearestAvailableRiders();
    }
}
