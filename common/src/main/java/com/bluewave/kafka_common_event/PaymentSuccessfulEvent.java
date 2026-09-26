package com.bluewave.kafka_common_event;

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