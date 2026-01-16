package com.app.Livetracker.controller;

import com.app.Livetracker.dto.*;
import com.app.Livetracker.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create/{orderId}")
    public PaymentResponseDTO createPayment(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UUID userId) throws Exception {

        return paymentService.createPayment(orderId, userId);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/verify")
    public PaymentResponseDTO verifyPayment(
            @RequestBody PaymentVerificationDTO dto) throws Exception {

        return paymentService.verifyPayment(dto);
    }
}
