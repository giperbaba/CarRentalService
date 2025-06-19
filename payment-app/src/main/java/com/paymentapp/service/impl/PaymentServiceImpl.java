package com.paymentapp.service.impl;

import com.paymentapp.dto.*;
import com.paymentapp.entity.Payment;
import com.paymentapp.exception.PaymentException;
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
        log.info("Initializing payment for booking: {}", request.getBookingId());
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .userId(request.getUserId())
                .carId(request.getCarId())
                .status(PaymentStatus.NEW)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment initialized successfully: {}", savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(Long id) {
        log.info("Getting payment: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " + id));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getAllPayments(Pageable pageable) {
        log.info("Getting all payments with pagination");
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(Long id, PaymentProcessRequestDto request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.NEW) {
            throw new PaymentException("Payment is not in NEW status");
        }

        log.info("Processing payment transaction for amount: {}", payment.getAmount());
        String transactionId = UUID.randomUUID().toString();
        log.info("Transaction completed with ID: {}", transactionId);
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PAID);

        Payment savedPayment = paymentRepository.save(payment);

        paymentEventService.sendPaymentEvent(new PaymentEvent(savedPayment.getId(), savedPayment.getBookingId(), savedPayment.getCarId(),
                savedPayment.getUserId(), savedPayment.getStatus()));

        log.info("Payment processed successfully: {}", savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto cancelPayment(Long id) {
        log.info("Cancelling payment: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " + id));

        if (payment.getStatus() != PaymentStatus.NEW) {
            throw new PaymentException("Can only cancel payments in NEW status");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Payment cancelled successfully: {}", savedPayment.getId());
        return paymentMapper.toDto(savedPayment);
    }
} 