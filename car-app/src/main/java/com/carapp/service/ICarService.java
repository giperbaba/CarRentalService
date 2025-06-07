package com.carapp.service;

import com.carapp.dto.CarDto;
import com.carapp.enums.CarStatus;

import java.util.List;
import java.util.UUID;

public interface ICarService {
    List<CarDto> getAllCars();
    List<CarDto> getAvailableCars();
    CarDto getCarById(UUID id);
    CarDto createCar(CarDto carDto);
    CarDto updateCar(UUID id, CarDto carDto);
    CarDto updateCarStatus(UUID id, CarStatus status);
} 