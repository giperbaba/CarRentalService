package com.common.enums;

public enum BookingStatus {
    PENDING_PAYMENT,    // аренда ожидает оплаты
    CONFIRMED,         // оплата прошла, аренда началась
    COMPLETED,         // аренда закончилась
    CANCELLED,         // аренда отменена
    PAYMENT_FAILED     // оплата не прошла
} 