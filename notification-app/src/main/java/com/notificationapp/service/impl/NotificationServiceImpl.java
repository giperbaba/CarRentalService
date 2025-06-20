package com.notificationapp.service.impl;

import com.notificationapp.dto.NotificationMessage;
import com.notificationapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import com.notificationapp.dto.PaymentEmailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendNotification(NotificationMessage notification) {
        sendWebSocketNotification(notification);
        sendEmailNotification(notification);
    }

    @Override
    public void processPaymentEvent(PaymentEmailEvent event) {
        String status = event.getStatus();
        String email = event.getEmail();
        String subject;
        String text;
        switch (status) {
            case "NEW":
                subject = "Новая оплата";
                text = "Ваша оплата создана и ожидает подтверждения.";
                break;
            case "SUCCESS":
                subject = "Оплата успешна";
                text = "Ваша оплата прошла успешно. Спасибо!";
                break;
            case "CANCELLED":
                subject = "Оплата отменена";
                text = "Ваша оплата была отменена.";
                break;
            default:
                subject = "Статус оплаты обновлен";
                text = "Статус вашей оплаты: " + status;
        }

        sendEmailByEvent(email, subject, text);

        NotificationMessage notification = new NotificationMessage();
        notification.setEmail(email);
        notification.setMessage(text);
        sendWebSocketNotification(notification);
    }

    private void sendEmailByEvent(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    @Override
    public void sendEmailNotification(NotificationMessage notification) {
        if (notification.getEmail() != null && notification.getMessage() != null) {
            sendEmailByEvent(notification.getEmail(), "Уведомление", notification.getMessage());
        }
    }

    @Override
    public void sendWebSocketNotification(NotificationMessage notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
} 