package com.carapp.dto;

import com.carapp.constant.ValidationConstants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarCreateRequest {
    @NotBlank(message = ValidationConstants.MAKE_REQUIRED)
    @Size(min = ValidationConstants.MIN_NAME_LENGTH, 
          max = ValidationConstants.MAX_NAME_LENGTH, 
          message = ValidationConstants.MAKE_SIZE)
    private String brand;

    @NotBlank(message = ValidationConstants.MODEL_REQUIRED)
    @Size(min = ValidationConstants.MIN_NAME_LENGTH, 
          max = ValidationConstants.MAX_NAME_LENGTH, 
          message = ValidationConstants.MODEL_SIZE)
    private String model;

    @NotNull(message = ValidationConstants.YEAR_REQUIRED)
    @Min(value = ValidationConstants.MIN_YEAR, message = ValidationConstants.YEAR_MIN)
    @Max(value = ValidationConstants.MAX_YEAR, message = ValidationConstants.YEAR_MAX)
    private Integer year;

    @NotBlank(message = ValidationConstants.LICENSE_PLATE_REQUIRED)
    @Pattern(regexp = ValidationConstants.LICENSE_PLATE_PATTERN, 
             message = ValidationConstants.LICENSE_PLATE_FORMAT)
    private String licensePlate;

    @NotNull(message = ValidationConstants.DAILY_RATE_REQUIRED)
    @DecimalMin(value = ValidationConstants.MIN_DAILY_RATE, message = ValidationConstants.DAILY_RATE_MIN)
    @DecimalMax(value = ValidationConstants.MAX_DAILY_RATE, message = ValidationConstants.DAILY_RATE_MAX)
    private BigDecimal dailyRate;

    @Size(max = ValidationConstants.MAX_DESCRIPTION_LENGTH, 
          message = ValidationConstants.DESCRIPTION_SIZE)
    private String description;
} 