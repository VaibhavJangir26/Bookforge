package com.bluewave.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String PAYMENT_SUCCESSFUL_TOPIC="payment-successful-topic";
    public static final String PAYMENT_REFUND_TOPIC="payment-refund-topic";
    public static final String BOOKING_CANCELLED_TOPIC="payment-cancel-topic";

    @Bean
    public NewTopic paymentSuccessfulTopic(){
        return new NewTopic(PAYMENT_SUCCESSFUL_TOPIC,3,(short) 1);
    }

    @Bean
    public NewTopic paymentRefundTopic(){
        return new NewTopic(PAYMENT_REFUND_TOPIC,3,(short) 1);
    }






}
