package com.bluewave.kafka;

import com.bluewave.kafka_common_event.PaymentSuccessfulEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher {


    private final KafkaTemplate<String, Object> template;

    public void publishPaymentSuccessfulEvent(PaymentSuccessfulEvent event) {
        log.info("Emitting PaymentSuccessfulEvent for bookingId: {}", event.bookingId());
        template.send(KafkaConfig.PAYMENT_SUCCESSFUL_TOPIC, event.bookingId(), event);
    }

    public void publishPaymentRefundEvent(String bookingId, String stripePaymentIntentId) {
        log.info("Emitting PaymentRefundEvent for bookingId: {}", bookingId);
        template.send(KafkaConfig.PAYMENT_REFUND_TOPIC, bookingId, Map.of(
                "bookingId", bookingId,
                "stripePaymentIntentId", stripePaymentIntentId,
                "status", "REFUNDED"
        ));
    }

}
