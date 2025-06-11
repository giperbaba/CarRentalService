package com.bookingapp.client;

import com.bookingapp.dto.car.CarResponseDto;
import com.bookingapp.dto.car.CarStatusUpdateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
    name = "car-service",
    url = "${services.car-service.url}"
)
public interface CarServiceClient {
    @GetMapping("/api/v1/cars/{id}")
    CarResponseDto getCar(@PathVariable UUID id);

    @PatchMapping("/api/v1/cars/{id}/status")
    void updateCarStatus(@PathVariable UUID id, @RequestBody CarStatusUpdateRequestDto request);
} 