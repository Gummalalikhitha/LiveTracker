package com.app.Livetracker.service;

import com.app.Livetracker.dto.*;
import com.app.Livetracker.entity.*;
import com.app.Livetracker.entity.Order;
import com.app.Livetracker.entity.Payment;
import com.app.Livetracker.exception.BadRequestException;
import com.app.Livetracker.exception.NotFoundException;
import com.app.Livetracker.repository.*;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;


    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    private PaymentResponseDTO mapToDTO(Payment p) {
        return PaymentResponseDTO.builder()
                .paymentId(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .razorpaySignature(p.getRazorpaySignature())
                .amount(p.getAmount())
                .currency("INR")
                .status(p.getStatus())
                .build();
    }

    // CREATE PAYMENT
    @Transactional
    public PaymentResponseDTO createPayment(Long orderId, UUID userId) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }

        JSONObject options = new JSONObject();
        options.put("amount", order.getTotalAmount() * 100);
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(options);

        Payment payment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(order.getTotalAmount())
                .status(PaymentStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return mapToDTO(payment);
    }

    // VERIFY PAYMENT
    @Transactional
    public PaymentResponseDTO verifyPayment(PaymentVerificationDTO dto) throws Exception {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(dto.getRazorpayOrderId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        String payload =
                dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

        boolean valid = Utils.verifySignature(
                payload,
                dto.getRazorpaySignature(),
                razorpaySecret
        );

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            return mapToDTO(paymentRepository.save(payment));
        }

        payment.setRazorpayPaymentId(dto.getRazorpayPaymentId());
        payment.setRazorpaySignature(dto.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId()).get();
        order.setStatus(OrderStatus.RIDER_REQUESTED);
        orderRepository.save(order);

        return mapToDTO(payment);
    }
}

