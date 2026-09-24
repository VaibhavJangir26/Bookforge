package com.bluewave.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String BOOKING_CREATED_TOPIC = "booking-created-topic";
    public static final String BOOKING_CONFIRMED_TOPIC = "booking-confirmed-topic";
    public static final String BOOKING_CANCELLED_TOPIC = "booking-cancelled-topic";

    @Bean
    public NewTopic bookingCreatedTopic() {
        return new NewTopic(BOOKING_CREATED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return new NewTopic(BOOKING_CONFIRMED_TOPIC, 3, (short) 1);
    }

    @Bean
    public NewTopic bookingCancelledTopic() {
        return new NewTopic(BOOKING_CANCELLED_TOPIC, 3, (short) 1);
    }
}