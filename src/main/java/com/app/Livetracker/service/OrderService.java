package com.app.Livetracker.service;
import com.app.Livetracker.dto.*;
import com.app.Livetracker.entity.*;
import com.app.Livetracker.exception.BadRequestException;
import com.app.Livetracker.exception.CustomException;
import com.app.Livetracker.exception.NotFoundException;
import com.app.Livetracker.notification.NotificationService;
import com.app.Livetracker.entity.NotificationType;
import com.app.Livetracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final productRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;


    @Transactional
    public OrderResponseDTO createOrder(UUID userId, CreateOrderRequestDTO request) {

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PLACED)
                .paymentMode(request.getPaymentMode())
                .createdAt(LocalDateTime.now())
                .totalAmount(0.0)
                .build();

        if (request.getPaymentMode() == PaymentMode.COD) {
            order.setStatus(OrderStatus.RIDER_REQUESTED);
        } else {
            order.setStatus(OrderStatus.PAYMENT_PENDING);
        }

        order = orderRepository.save(order);

        double totalAmount = 0.0;

        for (OrderItemRequestDTO item : request.getItems()) {

            products product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            if (product.getStock() < item.getQuantity()) {
                throw new NotFoundException("Insufficient stock");
            }

            // Reduce stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getPid())
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();

            totalAmount += product.getPrice() * item.getQuantity();

            orderItemRepository.save(orderItem);
            order.setTotalAmount(totalAmount);
            orderRepository.save(order);
        }

        final Long orderId = order.getId();

        // ===============================
        // 🔔 SEND NOTIFICATION TO ADMINS
        // ===============================
        userRepository.findByRole(Role.ADMIN)
                .forEach(admin ->
                        notificationService.send(
                                admin.getId(),
                                userId,
                                NotificationType.ORDER_PLACED,
                                "New order received",
                                orderId,
                                frontendBaseUrl + "/assignRider?orderId=" + orderId
                        )
                );

        return buildOrderResponse(order);
    }

    public OrderResponseDTO cancelOrder(Long orderId, UUID userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.PICKED_UP ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new CustomException("Cannot cancel order now");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return buildOrderResponse(order);
    }

    // =====================================================
    // ADMIN: UPDATE ORDER STATUS
    // =====================================================
    public OrderResponseDTO adminUpdateStatus(Long orderId, OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (status != OrderStatus.RIDER_REQUESTED &&
                status != OrderStatus.RIDER_ACCEPTED) {
            throw new BadRequestException("Invalid admin status transition");
        }

        order.setStatus(status);
        orderRepository.save(order);

        // 🔔 NOTIFICATION → USER & RIDER (RIDER ASSIGNED)
        if (status == OrderStatus.RIDER_ACCEPTED) {
            userRepository.findByRole(Role.ADMIN)
                    .forEach(admin ->
                            notificationService.send(
                                    order.getUserId(),
                                    admin.getId(),
                                    NotificationType.RIDER_ASSIGNED,
                                    "Rider assigned to your order",
                                    order.getId(),
                                    frontendBaseUrl
                                            + "/CurrentOrder?orderId=" + order.getId()
                                            + "&userId=" + order.getUserId()
                            )
                    );

            if (order.getRiderId() != null) {
                userRepository.findByRole(Role.ADMIN)
                        .forEach(admin ->
                                notificationService.send(
                                        order.getRiderId(),
                                        admin.getId(),
                                        NotificationType.RIDER_REQUEST,
                                        "New delivery assigned",
                                        order.getId(),
                                        frontendBaseUrl + "/riderDashboard"
                                ));
            }
        }

        return buildOrderResponse(order);
    }

    // =====================================================
    // RIDER: UPDATE ORDER STATUS
    // =====================================================
    public OrderResponseDTO riderUpdateStatus(
            Long orderId,
            UUID riderId,
            OrderStatus status) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!riderId.equals(order.getRiderId())) {
            throw new NotFoundException("Unauthorized rider");
        }

        if (status != OrderStatus.PICKED_UP &&
                status != OrderStatus.OUT_FOR_DELIVERY &&
                status != OrderStatus.CASH_COLLECTED &&
                status != OrderStatus.REACHED_DESTINATION &&
                status != OrderStatus.DELIVERED) {
            throw new BadRequestException("Invalid rider status update");
        }

        if (status == OrderStatus.DELIVERED &&
                order.getStatus() != OrderStatus.PICKED_UP &&
                order.getStatus() != OrderStatus.REACHED_DESTINATION) {
            throw new CustomException("Order must be picked up first");
        }

        order.setStatus(status);
        orderRepository.save(order);

        // 🔔 NOTIFICATIONS BASED ON STATUS
        if (status == OrderStatus.PICKED_UP) {
            notificationService.send(
                    order.getUserId(),
                    riderId,
                    NotificationType.PICKED_UP,
                    "Your order has been picked up",
                    order.getId(),
                    frontendBaseUrl
                            + "/CurrentOrder?orderId=" + order.getId()
                            + "&userId=" + order.getUserId()
            );
        }

        if (status == OrderStatus.OUT_FOR_DELIVERY) {
            notificationService.send(
                    order.getUserId(),
                    riderId,
                    NotificationType.OUT_FOR_DELIVERY,
                    "Out for delivery",
                    order.getId(),
                    frontendBaseUrl
                            + "/CurrentOrder?orderId=" + order.getId()
                            + "&userId=" + order.getUserId()
            );
        }

        if (status == OrderStatus.REACHED_DESTINATION) {
            notificationService.send(
                    order.getUserId(),
                    riderId,
                    NotificationType.REACHED_DESTINATION,
                    "Rider has arrived at your location",
                    order.getId(),
                    frontendBaseUrl
                            + "/CurrentOrder?orderId=" + order.getId()
                            + "&userId=" + order.getUserId()
            );
        }

        if (status == OrderStatus.DELIVERED) {
            notificationService.send(
                    order.getUserId(),
                    riderId,
                    NotificationType.DELIVERED,
                    "Order delivered successfully",
                    order.getId(),
                    frontendBaseUrl
                            + "/CurrentOrder?orderId=" + order.getId()
                            + "&userId=" + order.getUserId()
            );

            userRepository.findByRole(Role.ADMIN)
                    .forEach(admin ->
                            notificationService.send(
                                    admin.getId(),
                                    riderId,
                                    NotificationType.DELIVERED,
                                    "Order delivered",
                                    order.getId(),
                                    frontendBaseUrl + "/orderDetail"
                            )
                    );
        }

        return buildOrderResponse(order);
    }

    // =====================================================
    // ADMIN: GET ALL ORDERS
    // =====================================================
    public List<OrderResponseDTO> getAllOrdersForAdmin() {

        return orderRepository.findAll()
                .stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
// ADMIN: GET ALL RIDER_REQUESTED ORDERS
// =====================================================
    public List<OrderResponseDTO> getRiderRequestedOrdersForAdmin() {

        return orderRepository
                .findByStatus(OrderStatus.RIDER_REQUESTED)
                .stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // ADMIN: GET ORDER BY ID
    // =====================================================
    public OrderResponseDTO getOrderByIdForAdmin(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return buildOrderResponse(order);
    }

    // =====================================================
    // RIDER: GET ORDER BY ID
    // =====================================================
    public OrderResponseDTO getOrderByIdForRider(Long orderId, UUID riderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!riderId.equals(order.getRiderId())) {
            throw new NotFoundException("Order not assigned to this rider");
        }

        return buildOrderResponse(order);
    }

    // =====================================================
    // RIDER: GET ALL ASSIGNED ORDERS
    // =====================================================
    public List<OrderResponseDTO> getOrdersByRider(UUID riderId) {

        return orderRepository.findByRiderId(riderId)
                .stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    // USER: GET ORDER BY ID
    // =====================================================
    public OrderResponseDTO getOrderByIdForUser(Long orderId, UUID userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to this order");
        }

        return buildOrderResponse(order);
    }

    // =====================================================
    // USER: GET ALL ORDERS
    // =====================================================
    public List<OrderResponseDTO> getOrdersForUser(UUID userId) {

        List<Order> orders =
                orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return orders.stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    // =====================================================
    // RIDER: GET USER LOCATION
    // =====================================================
    public UserLocationResponseDTO getUserLocationForRider(
            Long orderId,
            UUID riderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!riderId.equals(order.getRiderId())) {
            throw new BadRequestException("Unauthorized rider");
        }

        if (order.getStatus() != OrderStatus.RIDER_ACCEPTED &&
                order.getStatus() != OrderStatus.PICKED_UP &&
                order.getStatus() != OrderStatus.OUT_FOR_DELIVERY &&
                order.getStatus() != OrderStatus.REACHED_DESTINATION) {
            throw new BadRequestException("Order not active");
        }

        String redisKey = "user:location:" + order.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue == null) {
            throw new NotFoundException("User location not available");
        }

        try {
            Map<String, Object> location =
                    objectMapper.readValue(redisValue, Map.class);

            return UserLocationResponseDTO.builder()
                    .lat(((Number) location.get("lat")).doubleValue())
                    .lng(((Number) location.get("lng")).doubleValue())
                    .build();

        } catch (Exception e) {
            throw new BadRequestException("Invalid location data");
        }
    }

    // =====================================================
    // COMMON RESPONSE BUILDER
    // =====================================================
    private OrderResponseDTO buildOrderResponse(Order order) {

        List<OrderItemResponseDTO> items =
                orderItemRepository.findByOrderId(order.getId())
                        .stream()
                        .map(i -> OrderItemResponseDTO.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .build())
                        .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .riderId(order.getRiderId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMode(order.getPaymentMode()) // ✅ FIX
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}


