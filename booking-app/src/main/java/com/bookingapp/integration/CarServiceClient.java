package com.bookingapp.integration;

import com.bookingapp.dto.booking.BookingStatusUpdateRequest;
import com.bookingapp.dto.car.CarResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
    name = "car-service",
    url = "${integration.car-service.url}"
)
public interface CarServiceClient {
    
    @GetMapping("/api/v1/cars/{id}")
    CarResponseDto getCar(@PathVariable UUID id);

    @GetMapping("/api/v1/cars/available")
    CarResponseDto[] getAvailableCars();

    @PutMapping("/api/v1/cars/{id}/booking-status")
    CarResponseDto updateCarBookingStatus(
        @PathVariable UUID id,
        @RequestBody BookingStatusUpdateRequest request
    );
} 