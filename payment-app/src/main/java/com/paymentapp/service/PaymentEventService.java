package com.paymentapp.service;

import com.paymentapp.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-events-v2";

    public void sendPaymentEvent(PaymentEvent payment) {
        kafkaTemplate.send(PAYMENT_TOPIC, payment);
    }
} 