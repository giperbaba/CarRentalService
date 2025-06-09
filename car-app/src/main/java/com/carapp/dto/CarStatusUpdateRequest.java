package com.carapp.dto;

import com.carapp.constant.ValidationConstants;
import com.carapp.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarStatusUpdateRequest {
    @NotNull(message = ValidationConstants.STATUS_REQUIRED)
    private CarStatus status;
} 