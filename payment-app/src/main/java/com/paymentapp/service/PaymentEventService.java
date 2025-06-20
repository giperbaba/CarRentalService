package com.paymentapp.service;

import com.paymentapp.dto.PaymentEvent;
import com.paymentapp.dto.PaymentEmailEvent;
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
    private static final String PAYMENT_EMAIL_TOPIC = "payment-email-events";

    public void sendPaymentEvent(PaymentEvent payment) {
        kafkaTemplate.send(PAYMENT_TOPIC, payment);
    }

    public void sendEmailEvent(PaymentEmailEvent emailEvent) {
        kafkaTemplate.send(PAYMENT_EMAIL_TOPIC, emailEvent);
    }
} 