package com.app.Livetracker.repository;

import com.app.Livetracker.entity.Order;
import com.app.Livetracker.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(UUID userId);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Order> findByRiderId(UUID riderId);

    Optional<Order> findByIdAndRiderId(Long orderId, UUID riderId);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    Optional<Order> findByRiderIdAndStatusIn(UUID riderId, List<OrderStatus> statuses);

    List<Order> findByStatus(OrderStatus status);
}
