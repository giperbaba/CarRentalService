package com.bookingapp.dto.booking;

import com.bookingapp.constant.MessageConstants;
import com.bookingapp.dto.car.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusUpdateRequest {
    @NotNull(message = MessageConstants.STATUS_CANNOT_BE_NULL)
    private CarStatus status;

    public boolean isValidStatus() {
        return status == CarStatus.BOOKED || 
               status == CarStatus.RENTED || 
               status == CarStatus.AVAILABLE;
    }
} 