package com.bookingapp.scheduler;

import com.bookingapp.client.CarServiceClient;
import com.bookingapp.client.PaymentServiceClient;
import com.bookingapp.domain.Booking;
import com.bookingapp.domain.enums.BookingStatus;
import com.bookingapp.dto.car.CarBookingStatusRequest;
import com.bookingapp.service.BookingService;
import com.carapp.enums.CarStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;
    private final CarServiceClient carServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelUnpaidBookings() {
        LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);

        List<Booking> unpaidBookings = bookingService.findUnpaidBookingsOlderThan(fifteenMinutesAgo);
        log.info("Found {} unpaid bookings older than 15 minutes", unpaidBookings.size());

        for (Booking booking : unpaidBookings) {
            try {
                log.info("Cancelling unpaid booking: {}", booking.getId());

                if (booking.getPaymentId() != null) {
                    paymentServiceClient.cancelPayment(booking.getPaymentId());
                }

                CarBookingStatusRequest request = CarBookingStatusRequest.builder()
                        .status(CarStatus.AVAILABLE)
                        .build();
                carServiceClient.updateCarStatus(booking.getCarId(), request);

                booking.setStatus(BookingStatus.CANCELLED);
                bookingService.save(booking);

                log.info("Successfully cancelled booking: {}", booking.getId());
            }
            catch (Exception e) {
                log.error("Error cancelling booking {}: {}", booking.getId(), e.getMessage());
            }
        }
    }
} 