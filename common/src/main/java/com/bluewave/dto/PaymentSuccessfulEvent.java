package com.bluewave.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSuccessfulEvent(
        String paymentId,
        String bookingId,
        String customerId,
        String stripePaymentIntentId,
        BigDecimal amount,
        LocalDateTime paymentTime
) {}