package com.bookingapp.dto.car;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.bookingapp.enums.CarStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CarResponseDto {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private BigDecimal dailyRate;
    private String description;
    private CarStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private String createdBy;
    private String updatedBy;
} 