package com.bookingapp.controller;

import com.bookingapp.dto.booking.BookingRequestDto;
import com.bookingapp.dto.booking.BookingResponseDto;
import com.bookingapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @RequestBody BookingRequestDto request,
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok(bookingService.createBooking(UUID.fromString(userId), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String userId
    ) {
        UUID userUuid = UUID.fromString(userId);
        return ResponseEntity.ok(bookingService.getBooking(id, userUuid));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getCurrentUserRentals(
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(UUID.fromString(userId)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(bookingService.getUserBookings(UUID.fromString(userId)));
    }

    @GetMapping("/car/{carId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getCarBookings(
            @PathVariable String carId
    ) {
        return ResponseEntity.ok(bookingService.getCarBookings(UUID.fromString(carId)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponseDto> completeBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok(bookingService.completeBooking(id, UUID.fromString(userId)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponseDto> confirmBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(required = false) Long paymentId) {
        return ResponseEntity.ok(bookingService.confirmBooking(id, UUID.fromString(userId), paymentId));
    }
} 