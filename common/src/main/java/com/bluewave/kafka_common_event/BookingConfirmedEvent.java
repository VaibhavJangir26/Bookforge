package com.bluewave.kafka_common_event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingConfirmedEvent(
        String bookingId,
        String customerId,
        String spaceId,
        String venueId,
        LocalDateTime slotStartTime,
        LocalDateTime slotEndTime,
        BigDecimal totalAmount,
        LocalDateTime confirmedAt
) {}