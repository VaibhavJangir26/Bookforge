package com.bluewave.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher {

    private final KafkaTemplate<String,Object> template;

    public void paymentSuccessfulEvent(){
        log.info("emitting payment successfully done event");
        template.send("payment-successful-topic","","");
    }

    public void  paymentRefundEvent(){
        log.info("emitting payment refund request event");
        template.send("payment-refund-topic","","");
    }

}
