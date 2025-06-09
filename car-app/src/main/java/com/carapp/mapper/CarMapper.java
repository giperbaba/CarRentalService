package com.carapp.mapper;

import com.carapp.dto.CarCreateRequest;
import com.carapp.dto.CarResponse;
import com.carapp.dto.CarUpdateRequest;
import com.carapp.entity.Car;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface CarMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "rentalPrice", source = "dailyRate")
    Car toEntity(CarCreateRequest request);

    CarResponse toResponse(Car car);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "rentalPrice", source = "dailyRate")
    void updateCarFromDto(CarUpdateRequest request, @MappingTarget Car car);
} 