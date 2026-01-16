//package com.app.Livetracker.dto;
//
//import com.app.Livetracker.entity.OrderStatus;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class OrderResponseDTO {
//
//    private Long orderId;
//    private UUID userId;
//    private UUID riderId;
//    private Double totalAmount;
//    private OrderStatus status;
//    private LocalDateTime createdAt;
//    private List<OrderItemResponseDTO> items;
//}




package com.app.Livetracker.dto;

import com.app.Livetracker.entity.OrderStatus;
import com.app.Livetracker.entity.PaymentMode;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long orderId;
    private UUID userId;
    private UUID riderId;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
    private PaymentMode paymentMode;
}