package com.bookingapp.service;

import com.bookingapp.dto.BookingRequestDto;
import com.bookingapp.dto.BookingResponseDto;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingRequestDto request);
    BookingResponseDto getBooking(Long id);
    List<BookingResponseDto> getCurrentUserRentals();
    List<BookingResponseDto> getUserBookings(Long userId);
    List<BookingResponseDto> getCarBookings(UUID carId);
    BookingResponseDto completeBooking(Long id);
    void cancelBooking(Long id);
    void processExpiredBookings();
    BookingResponseDto processPayment(Long id, String paymentMethod);
} 