package com.carapp.controller;

import com.carapp.dto.CarCreateRequest;
import com.carapp.dto.CarResponse;
import com.carapp.dto.CarStatusUpdateRequest;
import com.carapp.dto.CarUpdateRequest;
import com.carapp.service.ICarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<CarResponse> createCar(
            @Valid @RequestBody CarCreateRequest request) {
        return new ResponseEntity<>(carService.createCar(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CarResponse> getCar(@PathVariable UUID id) {
        return ResponseEntity.ok(carService.getCar(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CarResponse>> getAvailableCars() {
        return ResponseEntity.ok(carService.getAvailableCars());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponse> updateCar(
            @PathVariable UUID id,
            @Valid @RequestBody CarUpdateRequest request) {
        return ResponseEntity.ok(carService.updateCar(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponse> updateCarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CarStatusUpdateRequest request) {
        return ResponseEntity.ok(carService.updateCarStatus(id, request));
    }
} 