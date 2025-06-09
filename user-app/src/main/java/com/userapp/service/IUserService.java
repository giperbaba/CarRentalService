package com.userapp.service;

import com.userapp.dto.request.AuthRequestDto;
import com.userapp.dto.request.RefreshTokenRequestDto;
import com.userapp.dto.request.UserRegisterRequestDto;
import com.userapp.dto.request.UserUpdateRequestDto;
import com.userapp.dto.response.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID; 

public interface IUserService {

    ResponseEntity<?> login(AuthRequestDto authRequest);

    ResponseEntity<?> register(UserRegisterRequestDto registerRequest);

    ResponseEntity<?> refresh(RefreshTokenRequestDto refreshTokenRequest);

    ResponseEntity<?> logout(HttpServletRequest request);

    ResponseEntity<?> getMyProfile();

    ResponseEntity<?> getUserProfile(UUID userId);

    ResponseEntity<?> updateMyProfile(UserUpdateRequestDto updateRequest);

    ResponseEntity<?> deactivateUser(UUID userId);

    List<UserResponseDto> getAllUsers();
}