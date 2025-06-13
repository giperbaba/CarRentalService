package com.carapp.service;

import com.carapp.dto.CarCreateRequest;
import com.carapp.dto.CarResponse;
import com.carapp.dto.CarUpdateRequest;
import com.carapp.dto.CarMaintenanceStatusRequest;
import com.carapp.dto.CarBookingStatusRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ICarService {
    CarResponse createCar(@Valid CarCreateRequest request);

    CarResponse getCar(UUID id);

    boolean isCarAvailable(UUID id);

    List<CarResponse> getAllCars();

    List<CarResponse> getAvailableCars();

    CarResponse updateCar(UUID id, @Valid CarUpdateRequest request);

    CarResponse updateMaintenanceStatus(UUID id, CarMaintenanceStatusRequest request);

    CarResponse updateBookingStatus(UUID id, CarBookingStatusRequest request);
} 