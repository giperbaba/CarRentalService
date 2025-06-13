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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long id, UUID userId) {
        log.info("Getting booking: id={}, userId={}", id, userId);
        Booking booking = findBookingById(id);
        log.info("Found booking: id={}, bookingUserId={}, requestUserId={}", 
                id, booking.getUserId(), userId);
        
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
        bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_COMPLETED);
        
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long id, UUID userId) {
        Booking booking = findBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingException(BookingConstants.ErrorMessages.CANNOT_CANCEL_COMPLETED);
        }

        booking.setStatus(BookingStatus.CANCELLED);

        carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                .status(CarStatus.AVAILABLE)
                .build());

        Booking savedBooking = bookingRepository.save(booking);
        bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_CANCELLED);
        
        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto processPayment(Long id, String paymentMethod, UUID userId) {
        Booking booking = findBookingById(id);
        if (!booking.getUserId().equals(userId)) {
            throw new BookingException(BookingConstants.ErrorMessages.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BookingException(BookingConstants.ErrorMessages.BOOKING_NOT_PENDING_PAYMENT);
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
            carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                    .status(CarStatus.AVAILABLE)
                    .build());
            bookingEventService.sendBookingEvent(booking, BookingEventType.PAYMENT_FAILED);
        }

        return bookingMapper.toDto(bookingRepository.save(booking));
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
                    log.info("Processing expired booking: id={}, userId={}, carId={}", 
                            booking.getId(), booking.getUserId(), booking.getCarId());
                    
                    booking.setStatus(BookingStatus.CANCELLED);

                    try {
                        // Устанавливаем системного пользователя для фоновой задачи
                        SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                "system",
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                            )
                        );

                        carServiceClient.updateCarStatus(booking.getCarId(), CarBookingStatusRequest.builder()
                                .status(CarStatus.AVAILABLE)
                                .build());
                        log.info("Successfully updated car status for booking: id={}", booking.getId());
                    } catch (Exception e) {
                        log.error("Failed to update car status for booking: id={}, error={}", 
                                booking.getId(), e.getMessage());
                    } finally {
                        // Очищаем контекст безопасности
                        SecurityContextHolder.clearContext();
                    }

                    Booking savedBooking = bookingRepository.save(booking);
                    bookingEventService.sendBookingEvent(savedBooking, BookingEventType.BOOKING_EXPIRED);
                    log.info("Successfully cancelled expired booking: id={}", booking.getId());
                } catch (Exception e) {
                    log.error("Error processing expired booking: id={}, error={}", 
                            booking.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in processExpiredBookings: {}", e.getMessage());
        }
    }
} 