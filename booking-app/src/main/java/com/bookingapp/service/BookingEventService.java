package com.bookingapp.service;

import com.bookingapp.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String BOOKING_TOPIC = "booking-events-v2";

    public void sendBookingEvent(BookingEvent bookingEvent) {
        kafkaTemplate.send(BOOKING_TOPIC, bookingEvent);
    }
}