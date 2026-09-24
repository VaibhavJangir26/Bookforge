package com.bluewave.kafka;


import com.bluewave.kafka_common_event.BookingCancelledEvent;
import com.bluewave.kafka_common_event.BookingConfirmedEvent;
import com.bluewave.kafka_common_event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, Object> template;

    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Emitting BookingCreatedEvent for bookingId: {}", event.bookingId());
        template.send(KafkaConfig.BOOKING_CREATED_TOPIC, event.spaceId(), event);
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Emitting BookingConfirmedEvent for bookingId: {}", event.bookingId());
        template.send(KafkaConfig.BOOKING_CONFIRMED_TOPIC, event.spaceId(), event);
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Emitting BookingCancelledEvent for bookingId: {}", event.bookingId());
        template.send(KafkaConfig.BOOKING_CANCELLED_TOPIC, event.spaceId(), event);
    }
}