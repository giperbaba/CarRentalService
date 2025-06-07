package com.carapp.dto;

import com.carapp.enums.CarStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CarDto {
    private UUID id;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be positive")
    private Integer year;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Rental price is required")
    @Positive(message = "Rental price must be positive")
    private BigDecimal rentalPrice;

    @NotNull(message = "Status is required")
    private CarStatus status;

    private String color;
    private String description;
} 