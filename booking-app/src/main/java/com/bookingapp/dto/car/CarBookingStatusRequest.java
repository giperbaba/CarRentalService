package com.bookingapp.dto.car;

import com.carapp.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarBookingStatusRequest {
    @NotNull(message = "Status cannot be null")
    private CarStatus status;

    public boolean isValidStatus() {
        return status == CarStatus.BOOKED || 
               status == CarStatus.RENTED || 
               status == CarStatus.AVAILABLE;
    }
} 