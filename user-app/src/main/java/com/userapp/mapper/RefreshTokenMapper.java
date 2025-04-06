package com.userapp.mapper;

import com.userapp.dto.request.RefreshTokenDto;
import com.userapp.entity.RefreshToken;
import com.userapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    RefreshTokenDto toDto(RefreshToken refreshToken);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userId")
    RefreshToken toEntity(RefreshTokenDto dto, User user);
}
