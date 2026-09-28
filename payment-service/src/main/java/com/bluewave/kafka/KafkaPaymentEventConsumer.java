package com.bluewave.kafka;

import com.bluewave.kafka_common_event.BookingCancelledEvent;
import com.bluewave.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaConfig.BOOKING_CANCELLED_TOPIC, groupId = "payment-service-group")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent for bookingId: {}", event.bookingId());
        // Process automated partial refund if the cancellation was approved under the 24h refund policy
        if (event.cancellationReason() != null && event.cancellationReason().toUpperCase().contains("REFUND APPROVED")) {
            log.info("Triggering automated partial refund flow for bookingId: {}", event.bookingId());
            paymentService.processAutomatedRefund(event.bookingId());
        } else {
            log.info("Booking {} was not eligible for automatic refund (late or non-refundable cancellation).", event.bookingId());
        }
    }
}
