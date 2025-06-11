package com.bookingapp.dto.car;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarStatusUpdateRequestDto {
    private String status;
} 