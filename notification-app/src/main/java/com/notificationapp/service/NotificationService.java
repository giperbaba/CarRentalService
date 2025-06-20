package com.notificationapp.service;

import com.notificationapp.dto.NotificationMessage;
import com.notificationapp.dto.PaymentEmailEvent;

public interface NotificationService {
    void sendNotification(NotificationMessage notification);
    void sendEmailNotification(NotificationMessage notification);
    void sendWebSocketNotification(NotificationMessage notification);
    void processPaymentEvent(PaymentEmailEvent event);
} 