package com.paymentapp.exception;

public class UnauthorizedPaymentAccessException extends PaymentException {
    public UnauthorizedPaymentAccessException(String message) {
        super(message);
    }
} 