package com.bluewave.kafka;

import com.bluewave.booking.BookingRepo;
import com.bluewave.booking.model.Booking;
import com.bluewave.constants.BookingStatus;
import com.bluewave.kafka_common_event.PaymentSuccessfulEvent;
import com.bluewave.booking.client.CatalogClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final BookingRepo bookingRepo;
    private final RedissonClient redissonClient;
    private final CatalogClient catalogClient;

    @Transactional
    @KafkaListener(
            topics = "${kafka.topics.payment-successful:payment-successful-topic}",
            groupId = "${spring.kafka.consumer.group-id:booking-service-group}"
    )
    public void handlePaymentSuccessful(PaymentSuccessfulEvent event) {
        log.info("Received PaymentSuccessfulEvent for bookingId: {}", event.bookingId());

        try {
            Booking booking = bookingRepo.findById(event.bookingId())
                    .orElse(null);

            if (booking == null) {
                log.error("Booking not found for confirmed payment: {}", event.bookingId());
                return;
            }

            // 1. Idempotency Check: if already CONFIRMED, ignore
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                log.info("Booking {} is already CONFIRMED. Skipping.", booking.getId());
                return;
            }

            // 2. Transition Booking status to CONFIRMED
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepo.save(booking);
            log.info("Successfully updated Booking {} to CONFIRMED status in database", booking.getId());

            // 3. Clear the 5-minute Redis temporary hold key
            String holdKey = String.format("hold:space:%s:time:%s_%s",
                    booking.getSpaceId(), booking.getSlotStartTime(), booking.getSlotEndTime());
            redissonClient.getBucket(holdKey).delete();

            // 4. Deduct resource inventory in Catalog Service
            if (booking.getBookingResourceList() != null && !booking.getBookingResourceList().isEmpty()) {
                for (var resItem : booking.getBookingResourceList()) {
                    try {
                        catalogClient.deductResourceStock(resItem.getResourceId(), resItem.getQuantity());
                        log.info("Deducted {} units for resource {} (Booking: {})",
                                resItem.getQuantity(), resItem.getResourceId(), booking.getId());
                    } catch (Exception ex) {
                        log.error("Failed to deduct inventory for resource {}: {}", resItem.getResourceId(), ex.getMessage(), ex);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error processing PaymentSuccessfulEvent for bookingId {}: {}", event.bookingId(), e.getMessage(), e);
        }
    }
}
