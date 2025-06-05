package com.userapp.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is invalid")
        String phone
) {}
