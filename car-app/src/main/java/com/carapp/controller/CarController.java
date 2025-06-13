package com.carapp.controller;

import com.carapp.dto.*;
import com.carapp.service.ICarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {
    private final ICarService carService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponse> createCar(@Valid @RequestBody CarCreateRequest request) {
        return ResponseEntity.ok(carService.createCar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCar(@PathVariable UUID id) {
        return ResponseEntity.ok(carService.getCar(id));
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isCarAvailable(@PathVariable UUID id) {
        return ResponseEntity.ok(carService.isCarAvailable(id));
    }

    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CarResponse>> getAvailableCars() {
        return ResponseEntity.ok(carService.getAvailableCars());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable UUID id,
            @Valid @RequestBody CarUpdateRequest request) {
        return ResponseEntity.ok(carService.updateCar(id, request));
    }

    @PutMapping("/{id}/maintenance-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponse> updateMaintenanceStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CarMaintenanceStatusRequest request) {
        return ResponseEntity.ok(carService.updateMaintenanceStatus(id, request));
    }

    @PutMapping("/{id}/booking-status")
    public ResponseEntity<CarResponse> updateBookingStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CarBookingStatusRequest request) {
        return ResponseEntity.ok(carService.updateBookingStatus(id, request));
    }
} 