package com.carapp.service;

import com.carapp.constant.ValidationConstants;
import com.carapp.dto.*;
import com.carapp.entity.Car;
import com.carapp.enums.CarStatus;
import com.carapp.exception.CarNotFoundException;
import com.carapp.exception.InvalidCarOperationException;
import com.carapp.mapper.CarMapper;
import com.carapp.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.carapp.constant.ValidationConstants.LogMessages.*;
import static com.carapp.constant.ValidationConstants.Security.ROLE_ADMIN;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CarService implements ICarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional
    public CarResponse createCar(CarCreateRequest request) {
        validateLicensePlateUniqueness(request.getLicensePlate());
        Car car = carMapper.toEntity(request);
        car.setStatus(CarStatus.AVAILABLE);
        Car savedCar = carRepository.save(car);
        log.info(CAR_CREATED, savedCar.getId());
        return carMapper.toResponse(savedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponse getCar(UUID id) {
        Car car = findCarById(id);
        
        if (!isCurrentUserAdmin() && car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarOperationException(ValidationConstants.CAR_NOT_AVAILABLE);
        }
        
        return carMapper.toResponse(car);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarResponse> getAvailableCars() {
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(carMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CarResponse updateCar(UUID id, CarUpdateRequest request) {
        Car car = findCarById(id);
        
        if (request.getLicensePlate() != null && 
            !request.getLicensePlate().equals(car.getLicensePlate())) {
            validateLicensePlateUniqueness(request.getLicensePlate());
        }

        carMapper.updateCarFromDto(request, car);
        Car updatedCar = carRepository.save(car);
        log.info(CAR_UPDATED, id);
        return carMapper.toResponse(updatedCar);
    }

    @Override
    @Transactional
    public CarResponse updateMaintenanceStatus(UUID id, CarMaintenanceStatusRequest request) {
        Car car = findCarById(id);
        
        if (!request.isValidStatus()) {
            throw new InvalidCarOperationException(ValidationConstants.INVALID_MAINTENANCE_STATUS_UPDATE);
        }

        if (car.getStatus() != CarStatus.MAINTENANCE && car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarOperationException(ValidationConstants.MAINTENANCE_STATUS_CHANGE_NOT_ALLOWED);
        }

        car.setStatus(request.getStatus());
        Car updatedCar = carRepository.save(car);
        log.info(CAR_MAINTENANCE_STATUS_UPDATED, id, request.getStatus());
        return carMapper.toResponse(updatedCar);
    }

    @Override
    @Transactional
    public CarResponse updateBookingStatus(UUID id, CarBookingStatusRequest request) {
        Car car = findCarById(id);
        
        if (!request.isValidStatus()) {
            throw new InvalidCarOperationException(ValidationConstants.INVALID_BOOKING_STATUS_UPDATE);
        }

        if (car.getStatus() == CarStatus.MAINTENANCE || car.getStatus() == CarStatus.UNAVAILABLE) {
            throw new InvalidCarOperationException(ValidationConstants.BOOKING_STATUS_CHANGE_NOT_ALLOWED);
        }

        car.setStatus(request.getStatus());
        Car updatedCar = carRepository.save(car);
        return carMapper.toResponse(updatedCar);
    }

    @Override
    public boolean isCarAvailable(UUID id) {
        Car car = findCarById(id);
        return car.getStatus() == CarStatus.AVAILABLE;
    }

    private Car findCarById(UUID id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException(ValidationConstants.CAR_NOT_FOUND));
    }

    private void validateLicensePlateUniqueness(String licensePlate) {
        if (carRepository.existsByLicensePlate(licensePlate)) {
            throw new InvalidCarOperationException(ValidationConstants.LICENSE_PLATE_EXISTS);
        }
    }

    private boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
    }
} 