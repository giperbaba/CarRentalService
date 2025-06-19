package com.carapp.listener;

import com.carapp.dto.CarBookingStatusRequest;
import com.carapp.dto.PaymentEvent;
import com.carapp.dto.PaymentStatus;
import com.carapp.enums.CarStatus;
import com.carapp.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarPaymentListener {
    private final CarService carService;

    @KafkaListener(topics = "payment-events-v2", groupId = "car-service")
    public void handlePaymentEvent(PaymentEvent payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            carService.updateBookingStatus(payment.getCarId(), new CarBookingStatusRequest(CarStatus.RENTED));
        }
        else {
            carService.updateBookingStatus(payment.getCarId(), new CarBookingStatusRequest(CarStatus.AVAILABLE));
        }
    }
}