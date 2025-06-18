package com.paymentapp.repository;

import com.paymentapp.dto.PaymentStatus;
import com.paymentapp.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAll(Pageable pageable);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusAndUserId(PaymentStatus status, UUID userId);
    List<Payment> findByBookingId(Long bookingId);
}