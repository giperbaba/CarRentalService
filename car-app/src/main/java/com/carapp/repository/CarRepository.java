package com.carapp.repository;

import com.carapp.entity.Car;
import com.carapp.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findByStatus(CarStatus status);

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);
} 