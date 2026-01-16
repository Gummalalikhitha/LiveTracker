package com.app.Livetracker.controller;

import com.app.Livetracker.dto.OrderResponseDTO;
import com.app.Livetracker.entity.OrderStatus;
import com.app.Livetracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderService orderService;
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDTO updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return orderService.adminUpdateStatus(orderId, status);
    }
    // 1️⃣ Get all orders
    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.getAllOrdersForAdmin();
    }
    // 2️⃣ Get order by ID
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDTO getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderByIdForAdmin(orderId);
    }

    // 3️⃣ Get all RIDER_REQUESTED orders
    @GetMapping("/rider-requested")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponseDTO> getRiderRequestedOrders() {
        return orderService.getRiderRequestedOrdersForAdmin();
    }
}
