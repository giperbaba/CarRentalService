package com.bookingapp.service.impl;

import com.bookingapp.domain.Booking;
import com.bookingapp.event.BookingEvent;
import com.bookingapp.event.BookingEventType;
import com.bookingapp.service.BookingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventServiceImpl implements BookingEventService {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    
    @Value("${spring.kafka.topics.booking-events}")
    private String bookingEventsTopic;

    @Override
    public void sendBookingEvent(Booking booking, BookingEventType eventType) {
        BookingEvent event = BookingEvent.builder()
                .eventId(UUID.randomUUID())
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .carId(booking.getCarId())
                .eventType(eventType)
                .status(booking.getStatus())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .totalPrice(booking.getTotalPrice())
                .eventTime(LocalDateTime.now())
                .build();

        try {
            kafkaTemplate.send(bookingEventsTopic, booking.getId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Sent booking event: type={}, bookingId={}, offset={}",
                                    eventType, booking.getId(), result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send booking event: type={}, bookingId={}, error={}",
                                    eventType, booking.getId(), ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            log.error("Error while sending booking event: type={}, bookingId={}, error={}",
                    eventType, booking.getId(), e.getMessage());
        }
    }
} 