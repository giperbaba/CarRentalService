package com.carapp.entity;

import com.carapp.constant.ValidationConstants;
import com.carapp.enums.CarStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cars")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = ValidationConstants.MAKE_REQUIRED)
    @Size(min = ValidationConstants.MIN_NAME_LENGTH,
            max = ValidationConstants.MAX_NAME_LENGTH,
            message = ValidationConstants.MAKE_SIZE)
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = ValidationConstants.MODEL_REQUIRED)
    @Size(min = ValidationConstants.MIN_NAME_LENGTH,
            max = ValidationConstants.MAX_NAME_LENGTH,
            message = ValidationConstants.MODEL_SIZE)
    @Column(nullable = false)
    private String model;

    @NotNull(message = ValidationConstants.YEAR_REQUIRED)
    @Min(value = ValidationConstants.MIN_YEAR, message = ValidationConstants.YEAR_MIN)
    @Max(value = ValidationConstants.MAX_YEAR, message = ValidationConstants.YEAR_MAX)
    @Column(nullable = false)
    private Integer year;

    @NotBlank(message = ValidationConstants.LICENSE_PLATE_REQUIRED)
    @Pattern(regexp = ValidationConstants.LICENSE_PLATE_PATTERN,
            message = ValidationConstants.LICENSE_PLATE_FORMAT)
    @Column(nullable = false, unique = true)
    private String licensePlate;


    @NotNull(message = ValidationConstants.RENTAL_PRICE_REQUIRED)
    @DecimalMin(value = ValidationConstants.MIN_DAILY_RATE, message = ValidationConstants.RENTAL_PRICE_MIN)
    @DecimalMax(value = ValidationConstants.MAX_DAILY_RATE, message = ValidationConstants.RENTAL_PRICE_MAX)
    @Column(name = "rental_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal rentalPrice;

    @NotNull(message = ValidationConstants.DAILY_RATE_REQUIRED)
    @DecimalMin(value = ValidationConstants.MIN_DAILY_RATE, message = ValidationConstants.DAILY_RATE_MIN)
    @DecimalMax(value = ValidationConstants.MAX_DAILY_RATE, message = ValidationConstants.DAILY_RATE_MAX)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column
    private String color;

    @Size(max = ValidationConstants.MAX_DESCRIPTION_LENGTH,
            message = ValidationConstants.DESCRIPTION_SIZE)
    @Column(length = ValidationConstants.MAX_DESCRIPTION_LENGTH)
    private String description;

    @NotNull(message = ValidationConstants.STATUS_REQUIRED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status = CarStatus.AVAILABLE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by", nullable = false)
    private String updatedBy;
}