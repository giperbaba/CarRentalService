package com.paymentapp.service;

import com.paymentapp.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PAYMENT_TOPIC = "payment-events";

    public void sendPaymentEvent(Payment payment) {
        log.info("Sending payment event for payment: {}", payment.getId());
        kafkaTemplate.send(PAYMENT_TOPIC, payment);
        log.info("Payment event sent successfully");
    }
} 