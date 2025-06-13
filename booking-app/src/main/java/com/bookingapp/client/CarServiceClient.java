package com.bookingapp.client;

import com.bookingapp.config.FeignConfig;
import com.bookingapp.dto.car.CarResponseDto;
import com.bookingapp.dto.car.CarBookingStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
    name = "car-service",
    url = "${services.car-service.url}",
    configuration = FeignConfig.class
)
public interface CarServiceClient {
    @GetMapping("/api/cars/{id}")
    CarResponseDto getCar(@PathVariable UUID id);

    @GetMapping("/api/cars/{id}/available")
    boolean isCarAvailable(@PathVariable UUID id);

    @PutMapping("/api/cars/{id}/status")
    void updateCarStatus(@PathVariable UUID id, @RequestBody CarBookingStatusRequest request);
} 