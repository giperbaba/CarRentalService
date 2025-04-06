package com.userapp.dto;

import lombok.Data;

@Data
public class JwtDto {
    private String accessToken;
    private String refreshToken;
}
