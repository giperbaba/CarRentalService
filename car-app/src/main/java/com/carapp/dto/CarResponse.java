package com.carapp.dto;

import com.carapp.constant.ValidationConstants;
import com.carapp.enums.CarStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CarResponse {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private BigDecimal dailyRate;
    private String description;
    private CarStatus status;
    
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
} 