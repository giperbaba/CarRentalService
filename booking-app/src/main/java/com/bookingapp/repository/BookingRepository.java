package com.bookingapp.repository;

import com.bookingapp.domain.Booking;
import com.bookingapp.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Booking> findByCarIdOrderByCreatedAtDesc(UUID carId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :timeout")
    List<Booking> findExpiredBookings(
            @Param("status") BookingStatus status, 
            @Param("timeout") LocalDateTime timeout
    );

    @Query("SELECT b FROM Booking b WHERE b.carId = :carId " +
           "AND b.status NOT IN (:cancelledStatus, :completedStatus) " +
           "AND ((b.startDate BETWEEN :startDate AND :endDate) " +
           "OR (b.endDate BETWEEN :startDate AND :endDate) " +
           "OR (b.startDate <= :startDate AND b.endDate >= :endDate))")
    List<Booking> findOverlappingBookings(
            @Param("carId") UUID carId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("cancelledStatus") BookingStatus cancelledStatus,
            @Param("completedStatus") BookingStatus completedStatus
    );
} 