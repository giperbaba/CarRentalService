package com.userapp.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.userapp.enums.Gender;
import jakarta.validation.constraints.*;

import java.util.Date;

public record RegisterRequestDto(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,

        @NotNull(message = "Birthday is required")
        @JsonFormat(pattern = "yyyy-MM-dd")
        Date birthday,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is invalid")
        String phone
) {}