package com.carapp.entity;

import com.carapp.enums.CarStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "cars")
public class Car extends BaseEntity {

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private BigDecimal rentalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus status;

    private String color;
    
    @Column(length = 1000)
    private String description;
} 