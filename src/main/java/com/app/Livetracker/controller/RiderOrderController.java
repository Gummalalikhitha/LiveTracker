package com.app.Livetracker.controller;

import com.app.Livetracker.dto.OrderResponseDTO;
import com.app.Livetracker.dto.UserLocationResponseDTO;
import com.app.Livetracker.entity.OrderStatus;
import com.app.Livetracker.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rider/orders")
@RequiredArgsConstructor
public class RiderOrderController {

    private final OrderService orderService;

    // RIDER: get orders assigned to him
    @GetMapping
    @PreAuthorize("hasRole('RIDER')")
    public List<OrderResponseDTO> getAssignedOrders(
            @AuthenticationPrincipal UUID riderId) {
        return orderService.getOrdersByRider(riderId);
    }

    // RIDER: update order status (PICKED_UP / DELIVERED)
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('RIDER')")
    public OrderResponseDTO updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UUID riderId) {

        return orderService.riderUpdateStatus(orderId, riderId, status);
    }

    // 3️⃣ Get assigned order
    @PreAuthorize("hasRole('RIDER')")
    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderForRider(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UUID riderId) {
        return orderService.getOrderByIdForRider(orderId, riderId);
    }
    @PreAuthorize("hasRole('RIDER')")
    @GetMapping("/{orderId}/user-location")
    public UserLocationResponseDTO getUserLocation(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UUID riderId) {

        return orderService.getUserLocationForRider(orderId, riderId);
    }
}
