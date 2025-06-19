package com.carapp.dto;

import com.carapp.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarBookingStatusRequest {
    @NotNull(message = "Status cannot be null")
    private CarStatus status;

    public boolean isValidStatus() {
        return status == CarStatus.BOOKED || 
               status == CarStatus.RENTED || 
               status == CarStatus.AVAILABLE;
    }
} 