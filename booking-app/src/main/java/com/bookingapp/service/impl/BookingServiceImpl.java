package com.bookingapp.service.impl;

import com.bookingapp.client.CarServiceClient;
import com.bookingapp.client.PaymentServiceClient;
import com.bookingapp.constant.BookingConstants;
import com.bookingapp.domain.Booking;
import com.bookingapp.domain.enums.BookingStatus;
import com.bookingapp.dto.BookingRequestDto;
import com.bookingapp.dto.BookingResponseDto;
import com.bookingapp.dto.car.CarResponseDto;
import com.bookingapp.dto.car.CarBookingStatusRequest;
import com.bookingapp.dto.payment.PaymentProcessRequestDto;
import com.bookingapp.dto.payment.PaymentProcessResponseDto;
import com.bookingapp.event.BookingEventType;
import com.bookingapp.exception.BookingException;
import com.bookingapp.mapper.BookingMapper;
import com.bookingapp.repository.BookingRepository;
import com.bookingapp.service.BookingEventService;
import com.bookingapp.service.BookingService;
import com.carapp.enums.CarStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CarServiceClient carServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final BookingEventService bookingEventService;

    @Value("${booking.payment.timeout-minutes}")
    private int paymentTimeoutMinutes;

    @Override
    @Transactional
    public BookingResponseDto createBooking(UUID userId, BookingRequestDto request) {
        validateBookingRequest(request);

        if (!carServiceClient.isCarAvailable(request.getCarId())) {
            throw new BookingException(BookingConstants.ErrorMessages.CAR_NOT_AVAILABLE);
        }

        Booking booking = bookingMapper.toEntity(request);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalPrice(calculatePrice(carServiceClient.getCar(request.getCarId()).getDailyRate(), 
            request.getStartDate(), request.getEndDate()));
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        carServiceClient.updateCarStatus(request.getCarId(), CarBookingStatusRequest.builder()
                .status(CarStatus.BOOKED)
                .build());

        Booking savedBooking = bookingRepository.save(booking);
        bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_CREATED);
        
        return bookingMapper.toDto(savedBooking);
    }

    private void validateBookingRequest(BookingRequestDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BookingException(BookingConstants.ErrorMessages.END_DATE_BEFORE_START);
        }

        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BookingException(BookingConstants.ErrorMessages.START_DATE_IN_PAST);
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getCarId(),
                request.getStartDate(),
                request.getEndDate(),
                BookingStatus.CANCELLED,
                BookingStatus.COMPLETED
        );

        if (!overlappingBookings.isEmpty()) {
            throw new BookingException(BookingConstants.ErrorMessages.CAR_ALREADY_BOOKED);
        }
    }

    private BigDecimal calculatePrice(BigDecimal pricePerDay, LocalDateTime startDate, LocalDateTime endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) {
            days = 1;
        }
        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format(BookingConstants.ErrorMessages.BOOKING_NOT_FOUND, id)
                ));
    }

    /*@Override
    @Transactional
    public BookingResponseDto processPayment(Long id, String paymentMethod) {
        Booking booking = findBookingById(id);
        
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking is not in PENDING_PAYMENT status");
        }

        PaymentProcessRequestDto paymentRequest = PaymentProcessRequestDto.builder()
                .bookingId(booking.getId())
                .amount(booking.getTotalPrice().doubleValue())
                .paymentMethod(paymentMethod)
                .build();

        PaymentProcessResponseDto paymentResponse = paymentServiceClient.processPayment(paymentRequest);

        if ("COMPLETED".equals(paymentResponse.getStatus())) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentId(paymentResponse.getPaymentId());
            bookingEventService.sendBookingEvent(booking, BookingEventType.BOOKING_CONFIRMED);
        } else {
            booking.setStatus(BookingStatus.PAYMENT_FAILED);
            carServiceClient.updateCarStatus(booking.getCarId(), CarStatusUpdateRequestDto.builder()
                    .status(CarStatus.AVAILABLE)
                    .build());
            bookingEventService.sendBookingEvent(booking, BookingEventType.PAYMENT_FAILED);
        }

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long id) {
        return bookingMapper.toDto(findBookingById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getCarBookings(UUID carId) {
        return bookingRepository.findByCarIdOrderByCreatedAtDesc(carId)
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponseDto completeBooking(Long id) {
        Booking booking = findBookingById(id);
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking must be in CONFIRMED status to complete");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setActualEndDate(LocalDateTime.now());

        carServiceClient.updateCarStatus(booking.getCarId(), CarStatusUpdateRequestDto.builder()
                .status(CarStatus.AVAILABLE)
                .build());
        
        Booking savedBooking = bookingRepository.save(booking);
        bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_COMPLETED);
        
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = findBookingById(id);
        
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        carServiceClient.updateCarStatus(booking.getCarId(), CarStatusUpdateRequestDto.builder()
                .status(CarStatus.AVAILABLE)
                .build());

        Booking savedBooking = bookingRepository.save(booking);
        bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_CANCELLED);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processExpiredBookings() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PENDING_PAYMENT, timeout);
        
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);

            carServiceClient.updateCarStatus(booking.getCarId(), CarStatusUpdateRequestDto.builder()
                    .status(CarStatus.AVAILABLE)
                    .build());

            Booking savedBooking = bookingRepository.save(booking);
            bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_EXPIRED);
            log.info("Cancelled expired booking: {}", booking.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getCurrentUserRentals() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserBookings(userId);
    }*/
} 