package com.bookingapp.dto.car;

public enum CarStatus {
    AVAILABLE,    // доступен 
    RENTED,       // арендован
    BOOKED,       // забронирован
    MAINTENANCE,  // на ремонте/обслуживании
    UNAVAILABLE   // недоступен (продан, списан)
} 