package com.bookingapp.service;

import com.bookingapp.domain.Booking;
import com.bookingapp.dto.booking.BookingRequestDto;
import com.bookingapp.dto.booking.BookingResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDto createBooking(UUID userId, BookingRequestDto request);
    BookingResponseDto getBooking(Long id, UUID userId);
    List<BookingResponseDto> getUserBookings(UUID userId);
    List<BookingResponseDto> getCarBookings(UUID carId);
    BookingResponseDto completeBooking(Long id, UUID userId);
    BookingResponseDto cancelBooking(Long id, UUID userId);
    List<Booking> findUnpaidBookingsOlderThan(LocalDateTime time);
    Booking save(Booking booking);
    BookingResponseDto confirmBooking(Long id, UUID userId, Long paymentId);
} 