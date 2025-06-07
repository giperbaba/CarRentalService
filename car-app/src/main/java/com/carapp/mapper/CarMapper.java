package com.carapp.mapper;

import com.carapp.dto.CarDto;
import com.carapp.entity.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarDto toDto(Car car);
    Car toEntity(CarDto carDto);
    void updateEntity(CarDto carDto, @MappingTarget Car car);
} 