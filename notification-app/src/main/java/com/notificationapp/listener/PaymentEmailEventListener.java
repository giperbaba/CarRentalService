package com.notificationapp.listener;

import com.notificationapp.dto.PaymentEmailEvent;
import com.notificationapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEmailEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-email-events", groupId = "notification-group")
    public void listen(PaymentEmailEvent event) {
        notificationService.processPaymentEvent(event);
    }
} 