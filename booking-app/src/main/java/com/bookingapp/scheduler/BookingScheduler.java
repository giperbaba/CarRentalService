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
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;
    private final CarServiceClient carServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @Value("${app.internal-secret}")
    private String internalSecret;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelUnpaidBookings() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        List<Booking> unpaidBookings = bookingService.findUnpaidBookingsOlderThan(fiveMinutesAgo);
        log.info("Found {} unpaid bookings older than 5 minutes", unpaidBookings.size());

        for (Booking booking : unpaidBookings) {
            try {
                log.info("Cancelling unpaid booking: {}", booking.getId());

                CarBookingStatusRequest request = CarBookingStatusRequest.builder()
                        .status(CarStatus.AVAILABLE)
                        .build();
                carServiceClient.updateCarStatusWithSecret(booking.getCarId(), request, internalSecret);

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