package com.notificationapp.constant;

public final class NotificationConstants {

    private NotificationConstants() {
    }

    public static final class EmailSubjects {
        private EmailSubjects() {}
        public static final String NEW_PAYMENT = "Новая оплата";
        public static final String SUCCESSFUL_PAYMENT = "Оплата успешна";
        public static final String CANCELLED_PAYMENT = "Оплата отменена";
        public static final String UPDATED_PAYMENT_STATUS = "Статус оплаты обновлен";
        public static final String GENERIC_NOTIFICATION = "Уведомление";
    }

    public static final class EmailMessages {
        private EmailMessages() {}
        public static final String NEW_PAYMENT_BODY = "Ваша оплата создана и ожидает подтверждения.";
        public static final String SUCCESSFUL_PAYMENT_BODY = "Ваша оплата прошла успешно. Спасибо!";
        public static final String CANCELLED_PAYMENT_BODY = "Ваша оплата была отменена.";
        public static final String UPDATED_PAYMENT_STATUS_BODY_PREFIX = "Статус вашей оплаты: ";
    }

    public static final class PaymentStatus {
        private PaymentStatus() {}
        public static final String NEW = "NEW";
        public static final String SUCCESS = "SUCCESS";
        public static final String CANCELLED = "CANCELLED";
    }

    public static final class LogMessages {
        private LogMessages() {}
        public static final String EMAIL_SENT_SUCCESS = "Email notification sent successfully to {}";
        public static final String EMAIL_SEND_FAILURE = "Failed to send email to {}: {}";
    }
} 