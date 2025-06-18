package com.bookingapp.mapper;

import com.bookingapp.domain.Booking;
import com.bookingapp.dto.booking.BookingRequestDto;
import com.bookingapp.dto.booking.BookingResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "actualEndDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Booking toEntity(BookingRequestDto dto);

    @Mapping(target = "userId", source = "userId")
    BookingResponseDto toDto(Booking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(BookingRequestDto dto, @MappingTarget Booking entity);

    @Named("uuidToLong")
    default Long uuidToLong(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.getMostSignificantBits() & Long.MAX_VALUE;
    }
} 