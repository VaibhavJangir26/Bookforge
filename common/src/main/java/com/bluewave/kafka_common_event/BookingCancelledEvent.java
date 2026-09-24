package com.bluewave.kafka_common_event;

import java.time.LocalDateTime;

public record BookingCancelledEvent(
        String bookingId,
        String customerId,
        String spaceId,
        LocalDateTime slotStartTime,
        LocalDateTime slotEndTime,
        String cancellationReason,
        LocalDateTime cancelledAt
) {}