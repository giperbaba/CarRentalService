package com.userapp.dto.request;

import java.util.Date;
import java.util.UUID;

public record RefreshTokenDto(String token, Date expiresIn, UUID userId) {
}
