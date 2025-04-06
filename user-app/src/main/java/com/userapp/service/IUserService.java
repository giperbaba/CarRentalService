package com.userapp.service;

import com.userapp.dto.request.AuthRequestDto;
import com.userapp.dto.request.RefreshTokenRequestDto;

import com.userapp.dto.request.RegisterRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface IUserService {

    public ResponseEntity<?> login(AuthRequestDto authRequest);

    public ResponseEntity<?> register(RegisterRequestDto registerRequest);

    public ResponseEntity<?> refresh(RefreshTokenRequestDto refreshTokenRequest);

    public ResponseEntity<?> logout(HttpServletRequest request);
}
