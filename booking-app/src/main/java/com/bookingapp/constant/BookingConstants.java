package com.bookingapp.constant;

public final class BookingConstants {
    private BookingConstants() {
        throw new IllegalStateException("Constants class");
    }

    public static final class ErrorMessages {
        private ErrorMessages() {
            throw new IllegalStateException("Constants class");
        }

        public static final String END_DATE_BEFORE_START = "End date must be after start date";
        public static final String START_DATE_IN_PAST = "Start date must be in the future";
        public static final String CAR_NOT_AVAILABLE = "Car is not available for booking";
        public static final String CAR_ALREADY_BOOKED = "Car is already booked for the selected period";
        public static final String BOOKING_NOT_FOUND = "Booking not found with id: %s";
        public static final String ACCESS_DENIED = "Access denied. You don't have permission to perform this action";
        public static final String CAR_NOT_FOUND = "Car not found";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred";
        public static final String UNEXPECTED_ERROR = "An unexpected error occurred";
        public static final String CANNOT_CANCEL_COMPLETED = "Cannot cancel not pending payment booking";
        public static final String BOOKING_MUST_BE_CONFIRMED = "Booking must be in CONFIRMED status to complete";
        public static final String BOOKING_NOT_PENDING_PAYMENT = "Booking is not in PENDING_PAYMENT status";
    }
} 