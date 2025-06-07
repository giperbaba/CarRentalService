package com.carapp.controller;

import com.carapp.dto.CarDto;
import com.carapp.enums.CarStatus;
import com.carapp.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public List<CarDto> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/available")
    public List<CarDto> getAvailableCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/{id}")
    public CarDto getCarById(@PathVariable UUID id) {
        return carService.getCarById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto createCar(@Valid @RequestBody CarDto carDto) {
        return carService.createCar(carDto);
    }

    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable UUID id, @Valid @RequestBody CarDto carDto) {
        return carService.updateCar(id, carDto);
    }

    @PatchMapping("/{id}/status")
    public CarDto updateCarStatus(@PathVariable UUID id, @RequestParam CarStatus status) {
        return carService.updateCarStatus(id, status);
    }
} 