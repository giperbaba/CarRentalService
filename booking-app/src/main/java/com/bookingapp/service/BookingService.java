package com.bookingapp.service;

import com.bookingapp.dto.BookingRequestDto;
import com.bookingapp.dto.BookingResponseDto;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDto createBooking(UUID userId, BookingRequestDto request);
    BookingResponseDto getBooking(Long id, UUID userId);
    List<BookingResponseDto> getUserBookings(UUID userId);
    List<BookingResponseDto> getCarBookings(UUID carId);
    BookingResponseDto completeBooking(Long id, UUID userId);
    BookingResponseDto cancelBooking(Long id, UUID userId);
    BookingResponseDto processPayment(Long id, String paymentMethod, UUID userId);
    /*BookingResponseDto processPayment(Long id, String paymentMethod);*/
} 