package com.bookingapp.controller;

import com.bookingapp.dto.BookingRequestDto;
import com.bookingapp.dto.BookingResponseDto;
import com.bookingapp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @AuthenticationPrincipal String userId,
            @RequestBody BookingRequestDto request
    ) {
        return ResponseEntity.ok(bookingService.createBooking(Long.valueOf(userId), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBooking(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getCurrentUserRentals() {
        return ResponseEntity.ok(bookingService.getCurrentUserRentals());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/car/{carId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponseDto>> getCarBookings(@PathVariable UUID carId) {
        return ResponseEntity.ok(bookingService.getCarBookings(carId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BookingResponseDto> completeBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<BookingResponseDto> processPayment(
            @PathVariable Long id,
            @RequestParam String paymentMethod
    ) {
        return ResponseEntity.ok(bookingService.processPayment(id, paymentMethod));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Booking Service is up and running!");
    }
} 