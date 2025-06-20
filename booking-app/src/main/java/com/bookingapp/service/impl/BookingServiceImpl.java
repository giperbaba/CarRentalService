package com.bookingapp.service.impl;

import com.bookingapp.client.CarServiceClient;
import com.bookingapp.client.PaymentServiceClient;
import com.bookingapp.constant.BookingConstants;
import com.bookingapp.domain.Booking;
import com.bookingapp.domain.enums.BookingStatus;
import com.bookingapp.dto.booking.BookingRequestDto;
import com.bookingapp.dto.booking.BookingResponseDto;
import com.bookingapp.dto.car.CarBookingStatusRequest;
import com.bookingapp.dto.payment.*;
import com.bookingapp.event.BookingEventType;
import com.bookingapp.exception.BookingException;
import com.bookingapp.mapper.BookingMapper;
import com.bookingapp.repository.BookingRepository;
import com.bookingapp.service.BookingEventService;
import com.bookingapp.service.BookingService;
import com.carapp.enums.CarStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CarServiceClient carServiceClient;
    private final PaymentServiceClient paymentServiceClient;

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
        Booking savedBooking = bookingRepository.save(booking);

        try {

            PaymentInitRequestDto paymentRequest = new PaymentInitRequestDto();
            paymentRequest.setBookingId(savedBooking.getId());
            paymentRequest.setAmount(savedBooking.getTotalPrice());
            paymentRequest.setUserId(userId);
            paymentRequest.setCarId(request.getCarId());

            PaymentResponseDto payment = paymentServiceClient.initPayment(paymentRequest);
            log.info("Payment initialized for booking: {}, paymentId: {}", savedBooking.getId(), payment.getId());

            savedBooking.setPaymentId(payment.getId());

            try {
                carServiceClient.updateCarStatus(request.getCarId(), CarBookingStatusRequest.builder()
                        .status(CarStatus.BOOKED)
                        .build());
            }
            catch (Exception e) {
                throw new BookingException("Failed to update car status: " + e.getMessage());
            }
            return bookingMapper.toDto(savedBooking);

        } catch (Exception e) {
            log.error("Error during booking creation: {}", e.getMessage());

            savedBooking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(savedBooking);
            throw new BookingException("Failed to create booking: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long id, UUID userId) {
        Booking booking = findBookingById(id);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && 
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getUserId().equals(userId)) {
            log.warn("Access denied: bookingId={}, bookingUserId={}, requestUserId={}", 
                    id, booking.getUserId(), userId);
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(UUID userId) {
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
    public BookingResponseDto completeBooking(Long id, UUID userId) {
        Booking booking = findBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException(BookingConstants.ErrorMessages.BOOKING_MUST_BE_CONFIRMED);
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setActualEndDate(LocalDateTime.now());

        carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                .status(CarStatus.AVAILABLE)
                .build());
        
        Booking savedBooking = bookingRepository.save(booking);

        try {
            carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                    .status(CarStatus.AVAILABLE)
                    .build());
        } catch (Exception e) {
            log.error("Failed to update car status for booking: {}", booking.getId(), e);
        }
        
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long id, UUID userId) {
        Booking booking = findBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingException(BookingConstants.ErrorMessages.CANNOT_CANCEL_COMPLETED);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                .status(CarStatus.AVAILABLE)
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        try {
            carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                    .status(CarStatus.AVAILABLE)
                    .build());
        } catch (Exception e) {
            log.error("Failed to update car status for booking: {}", booking.getId(), e);
        }
        
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

    private BigDecimal calculateTotalPrice(LocalDateTime startDate, LocalDateTime endDate, BigDecimal pricePerDay) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 1) {
            days = 1;
        }
        return pricePerDay.multiply(BigDecimal.valueOf(days));
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException(
                    String.format(BookingConstants.ErrorMessages.BOOKING_NOT_FOUND, id)
                ));
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processExpiredBookings() {
        try {
            LocalDateTime timeout = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
            List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PENDING_PAYMENT, timeout);
            
            for (Booking booking : expiredBookings) {
                try {
                    log.info("Processing expired booking: id={}", booking.getId());
                    
                    booking.setStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    
                    try {
                        paymentServiceClient.cancelPayment(booking.getPaymentId());
                    } catch (Exception e) {
                        log.error("Failed to cancel payment for booking: {}", booking.getId(), e);
                    }
                    
                    try {
                        carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                                .status(CarStatus.AVAILABLE)
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to update car status for booking: {}", booking.getId(), e);
                    }
                    
                } catch (Exception e) {
                    log.error("Error processing expired booking: id={}, error={}", 
                            booking.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in processExpiredBookings: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto confirmBooking(Long id, UUID userId, Long paymentId) {
        Booking booking = findBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingException(BookingConstants.ErrorMessages.BOOKING_NOT_PENDING_PAYMENT);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentId(paymentId);
        Booking savedBooking = bookingRepository.save(booking);

        try {
            carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                    .status(CarStatus.RENTED)
                    .build());
        } catch (Exception e) {
            log.error("Failed to update car status for booking: {}", booking.getId(), e);
        }
        
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findUnpaidBookingsOlderThan(LocalDateTime time) {
        return bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, time);
    }
}