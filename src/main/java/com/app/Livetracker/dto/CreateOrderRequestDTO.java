package com.app.Livetracker.dto;

import com.app.Livetracker.entity.PaymentMode;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {

    private List<OrderItemRequestDTO> items;

    private PaymentMode paymentMode; // ONLINE or COD
}
