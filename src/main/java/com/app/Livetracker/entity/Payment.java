package com.app.Livetracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Internal Order reference
    @Column(nullable = false)
    private Long orderId;

    // Who paid
    @Column(nullable = false)
    private UUID userId;

    // Razorpay order id
    @Column(nullable = false, unique = true)
    private String razorpayOrderId;

    // Razorpay payment id (after success)
    private String razorpayPaymentId;

    private String razorpaySignature;

    @Column(nullable = false)
    private Double amount; // INR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime createdAt;
}
