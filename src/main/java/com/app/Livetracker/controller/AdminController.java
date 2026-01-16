package com.app.Livetracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.app.Livetracker.dto.CreateRiderRequestDTO;
import com.app.Livetracker.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-rider")
    public ResponseEntity<String> createRider(@RequestBody CreateRiderRequestDTO dto) {
        adminService.createRider(dto);
        return ResponseEntity.ok("Rider created successfully");
    }
}

