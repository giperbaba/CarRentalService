package com.userapp.service;

import com.userapp.dto.request.AuthRequestDto;
import com.userapp.dto.request.RefreshTokenRequestDto;
import com.userapp.dto.request.UserRegisterRequestDto;
import com.userapp.dto.request.UserUpdateRequestDto;
import com.userapp.dto.response.AuthResponseDto;
import com.userapp.dto.response.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID; 

public interface IUserService {

    ResponseEntity<AuthResponseDto> login(AuthRequestDto authRequest);

    ResponseEntity<AuthResponseDto> register(UserRegisterRequestDto registerRequest);

    ResponseEntity<AuthResponseDto> refresh(RefreshTokenRequestDto refreshTokenRequest);

    ResponseEntity<String> logout(HttpServletRequest request);

    ResponseEntity<UserResponseDto> getMyProfile();

    ResponseEntity<UserResponseDto> getUserProfile(UUID userId);

    ResponseEntity<UserResponseDto> updateMyProfile(UserUpdateRequestDto updateRequest);

    ResponseEntity<String> deactivateUser(UUID userId);

    List<UserResponseDto> getAllUsers();
}