package com.userapp.dto.response;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String phone,
        String name,
        String surname,
        boolean active
) {}
