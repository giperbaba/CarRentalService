package com.paymentapp.service.impl;

import com.paymentapp.constant.PaymentConstants.LogMessages;
import com.paymentapp.constant.PaymentConstants.ErrorMessages;
import com.paymentapp.dto.*;
import com.paymentapp.entity.Payment;
import com.paymentapp.exception.InvalidPaymentStatusException;
import com.paymentapp.exception.PaymentNotFoundException;
import com.paymentapp.exception.UnauthorizedPaymentAccessException;
import com.paymentapp.mapper.PaymentMapper;
import com.paymentapp.repository.PaymentRepository;
import com.paymentapp.service.PaymentEventService;
import com.paymentapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventService paymentEventService;

    @Override
    @Transactional
    public PaymentResponseDto initPayment(PaymentInitRequestDto request) {
        log.info(LogMessages.PAYMENT_INIT, request.getBookingId());
        
        Payment payment = createInitialPayment(request);
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info(LogMessages.PAYMENT_INIT_SUCCESS, savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(Long id, String userId) {
        log.info(LogMessages.GETTING_PAYMENT, id);
        Payment payment = findPaymentById(id);
        validatePaymentOwner(payment, userId);
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        log.info(LogMessages.GETTING_ALL_PAYMENTS);
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(Long id, PaymentProcessRequestDto request, String userId) {
        Payment payment = findPaymentById(id);
        validatePaymentStatus(payment);
        validatePaymentOwner(payment, userId);

        log.info(LogMessages.PROCESSING_PAYMENT, payment.getAmount());
        
        Payment processedPayment = processPaymentTransaction(payment);
        Payment savedPayment = paymentRepository.save(processedPayment);
        
        notifyPaymentProcessed(savedPayment);
        
        log.info(LogMessages.PAYMENT_PROCESSED, savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto cancelPayment(Long id, String userId) {
        Payment payment = findPaymentById(id);
        validatePaymentOwner(payment, userId);
        validatePaymentForCancellation(payment);

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info(LogMessages.PAYMENT_CANCELLED, savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    private Payment createInitialPayment(PaymentInitRequestDto request) {
        return Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .userId(request.getUserId())
                .carId(request.getCarId())
                .status(PaymentStatus.NEW)
                .build();
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(ErrorMessages.PAYMENT_NOT_FOUND));
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus() != PaymentStatus.NEW) {
            throw new InvalidPaymentStatusException(ErrorMessages.PAYMENT_NOT_IN_NEW_STATUS);
        }
    }

    private void validatePaymentForCancellation(Payment payment) {
        if (payment.getStatus() != PaymentStatus.NEW) {
            throw new InvalidPaymentStatusException(ErrorMessages.PAYMENT_CANCEL_INVALID_STATUS);
        }
    }

    private void validatePaymentOwner(Payment payment, String userId) {
        if (userId == null) {
            throw new UnauthorizedPaymentAccessException(ErrorMessages.UNAUTHORIZED_PAYMENT_ACCESS);
        }
        
        try {
            if (!payment.getUserId().equals(UUID.fromString(userId))) {
                throw new UnauthorizedPaymentAccessException(ErrorMessages.UNAUTHORIZED_PAYMENT_ACCESS);
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId: {}", userId);
            throw new UnauthorizedPaymentAccessException(ErrorMessages.UNAUTHORIZED_PAYMENT_ACCESS);
        }
    }

    private Payment processPaymentTransaction(Payment payment) {
        String transactionId = generateTransactionId();
        log.info(LogMessages.TRANSACTION_COMPLETED, transactionId);
        
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PAID);
        return payment;
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    private void notifyPaymentProcessed(Payment payment) {
        PaymentEvent event = new PaymentEvent(
                payment.getId(),
                payment.getBookingId(),
                payment.getCarId(),
                payment.getUserId(),
                payment.getStatus()
        );
        paymentEventService.sendPaymentEvent(event);
    }
} 