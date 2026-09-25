package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String id;
    private String stripePaymentIntentId;
    private String clientSecret; // Mandatory for Stripe Elements
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private String bookingId;
    private String customerId;
    private String failureReason;
    private LocalDateTime createdAt;
}