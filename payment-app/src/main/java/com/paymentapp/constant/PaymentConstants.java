package com.paymentapp.constant;

public final class PaymentConstants {
    private PaymentConstants() {

    }

    public static final class LogMessages {
        private LogMessages() {}
        
        public static final String PAYMENT_INIT = "Initializing payment for booking: {}";
        public static final String PAYMENT_INIT_SUCCESS = "Payment initialized successfully: {}";
        public static final String GETTING_PAYMENT = "Getting payment: {}";
        public static final String GETTING_ALL_PAYMENTS = "Getting all payments with pagination";
        public static final String PROCESSING_PAYMENT = "Processing payment transaction for amount: {}";
        public static final String TRANSACTION_COMPLETED = "Transaction completed with ID: {}";
        public static final String PAYMENT_PROCESSED = "Payment processed successfully: {}";
        public static final String PAYMENT_CANCELLED = "Payment cancelled successfully: {}";
    }

    public static final class ErrorMessages {
        private ErrorMessages() {}
        
        public static final String PAYMENT_NOT_FOUND = "Payment not found";
        public static final String PAYMENT_NOT_IN_NEW_STATUS = "Payment is not in NEW status";
        public static final String PAYMENT_CANCEL_INVALID_STATUS = "Can only cancel payments in NEW status";
        public static final String UNAUTHORIZED_PAYMENT_ACCESS = "You are not authorized to access this payment";
    }
} 