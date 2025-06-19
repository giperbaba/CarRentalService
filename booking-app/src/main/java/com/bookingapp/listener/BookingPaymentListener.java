package com.bookingapp.listener;
import com.bookingapp.service.BookingService;
import com.bookingapp.dto.payment.PaymentEvent;
import com.bookingapp.dto.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingPaymentListener {
    private final BookingService bookingService;

    @KafkaListener(topics = "payment-events-v2", groupId = "booking-service")
    public void handlePaymentEvent(PaymentEvent payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            bookingService.confirmBooking(payment.getBookingId(), payment.getUserId(), payment.getPaymentId());
        } else {
            bookingService.cancelBooking(payment.getBookingId(), payment.getUserId());
        }
    }
}