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
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

import static com.notificationapp.constant.NotificationConstants.EmailMessages.*;
import static com.notificationapp.constant.NotificationConstants.EmailSubjects.*;
import static com.notificationapp.constant.NotificationConstants.LogMessages.*;
import static com.notificationapp.constant.NotificationConstants.PaymentStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void processPaymentEvent(PaymentEmailEvent event) {
        String status = event.getStatus();
        String email = event.getEmail();
        String subject;
        String text;
        switch (status) {
            case NEW:
                subject = NEW_PAYMENT;
                text = NEW_PAYMENT_BODY;
                break;
            case SUCCESS:
                subject = SUCCESSFUL_PAYMENT;
                text = SUCCESSFUL_PAYMENT_BODY;
                break;
            case CANCELLED:
                subject = CANCELLED_PAYMENT;
                text = CANCELLED_PAYMENT_BODY;
                break;
            default:
                subject = UPDATED_PAYMENT_STATUS;
                text = UPDATED_PAYMENT_STATUS_BODY_PREFIX + status;
        }

        sendEmailByEvent(email, subject, text);

        NotificationMessage notification = new NotificationMessage();
        notification.setEmail(email);
        notification.setMessage(text);
        sendWebSocketNotification(notification);
    }

    private void sendEmailByEvent(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            log.info(EMAIL_SENT_SUCCESS, to);
        } catch (MailException e) {
            log.error(EMAIL_SEND_FAILURE, to, e.getMessage());
        }
    }

    @Override
    public void sendEmailNotification(NotificationMessage notification) {
        if (notification.getEmail() != null && notification.getMessage() != null) {
            sendEmailByEvent(notification.getEmail(), GENERIC_NOTIFICATION, notification.getMessage());
        }
    }

    @Override
    public void sendWebSocketNotification(NotificationMessage notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
} 