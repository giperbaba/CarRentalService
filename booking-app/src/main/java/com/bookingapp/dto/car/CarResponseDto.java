package com.bookingapp.dto.car;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CarResponseDto {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private BigDecimal pricePerDay;
    private CarStatus status;
} 