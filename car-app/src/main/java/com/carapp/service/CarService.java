package com.carapp.service;

import com.carapp.dto.CarDto;
import com.carapp.entity.Car;
import com.carapp.enums.CarStatus;
import com.carapp.mapper.CarMapper;
import com.carapp.repository.CarRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService implements ICarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarDto> getAvailableCars() {
        return carRepository.findByStatus(CarStatus.AVAILABLE).stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarDto getCarById(UUID id) {
        return carRepository.findById(id)
                .map(carMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
    }

    @Override
    @Transactional
    public CarDto createCar(CarDto carDto) {
        Car car = carMapper.toEntity(carDto);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarDto updateCar(UUID id, CarDto carDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
        
        carMapper.updateEntity(carDto, car);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarDto updateCarStatus(UUID id, CarStatus status) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
        
        car.setStatus(status);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }
} 