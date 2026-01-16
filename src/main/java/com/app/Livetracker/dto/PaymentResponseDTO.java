package com.app.Livetracker.dto;

import com.app.Livetracker.entity.PaymentStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private Long paymentId;
    private Long orderId;
    private UUID userId;

    private String razorpayOrderId;
    private String razorpayPaymentId;//always null because created at razorpaycheckout
    private String razorpaySignature;//always null because created at razorpaycheckout

    private Double amount;
    private String currency; // INR
    private PaymentStatus status;
}

