package com.app.Livetracker.controller;

import com.app.Livetracker.dto.CreateOrderRequestDTO;
import com.app.Livetracker.dto.OrderResponseDTO;
import com.app.Livetracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;

    // USER creates an order
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public OrderResponseDTO createOrder(
            @RequestBody CreateOrderRequestDTO request,
            @AuthenticationPrincipal UUID userId) {
        return orderService.createOrder(userId, request);
    }

    // USER cancels an order
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public OrderResponseDTO cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UUID userId) {
        return orderService.cancelOrder(orderId, userId);
    }

    // USER gets his order details by orderId
    @GetMapping("/{orderId}")
     @PreAuthorize("hasRole('USER')")
    public OrderResponseDTO getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UUID userId) {
        return orderService.getOrderByIdForUser(orderId, userId);
    }
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<OrderResponseDTO> getMyOrders(
            @AuthenticationPrincipal UUID userId) {
        return orderService.getOrdersForUser(userId);
    }
}
