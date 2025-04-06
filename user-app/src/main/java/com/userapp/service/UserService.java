package com.userapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userapp.dto.*;
import com.userapp.dto.request.RegisterRequestDto;
import com.userapp.dto.response.AuthResponseDto;
import com.userapp.dto.response.ErrorResponseDto;
import com.userapp.dto.request.AuthRequestDto;
import com.userapp.dto.request.RefreshTokenRequestDto;
import com.userapp.entity.RefreshToken;
import com.userapp.entity.Role;
import com.userapp.entity.User;
import com.userapp.enums.UserRole;
import com.userapp.mapper.UserMapper;
import com.userapp.repository.IRefreshTokenRepository;
import com.userapp.repository.IRoleRepository;
import com.userapp.repository.IUserRepository;
import com.userapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements IUserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IRefreshTokenRepository refreshTokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> login(AuthRequestDto authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.login(), authRequest.password()));

            User user = (User) authentication.getPrincipal();

            List<RefreshToken> oldTokens = refreshTokenRepository.findByUser(user);
            if (!oldTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(oldTokens);
            }

            JwtDto jwt = jwtUtil.generateToken(authentication);
            RefreshToken refreshTokenEntity = createRefreshTokenEntity(jwt.getRefreshToken(), user);
            refreshTokenRepository.save(refreshTokenEntity);

            return ResponseEntity.ok(new AuthResponseDto(jwt.getAccessToken(), jwt.getRefreshToken()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("Invalid credentails or login error"));
        }
    }

    public ResponseEntity<?> register(RegisterRequestDto registerRequest){
        try {
            if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDto("User already exists"));
            }
            User user = userMapper.registerRequestToUser(registerRequest);
            user.setPassword(passwordEncoder.encode(registerRequest.password()));

            Role userRole = roleRepository.findByRole(UserRole.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("Default role not found"));
            user.setRoles(Set.of(userRole));

            userRepository.save(user);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), registerRequest.password()));


            JwtDto jwt = jwtUtil.generateToken(authentication);
            RefreshToken refreshTokenEntity = createRefreshTokenEntity(jwt.getRefreshToken(), user);

            refreshTokenRepository.save(refreshTokenEntity);

            return ResponseEntity.ok(new AuthResponseDto(jwt.getAccessToken(), jwt.getRefreshToken()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("Registration failed"));
        }
    }

    private RefreshToken createRefreshTokenEntity(String token, User user) {
        return new RefreshToken(token, Date.from(Instant.now().plusMillis(jwtUtil.getRefreshExpiration())), user);
    }


    public ResponseEntity<?> refresh(RefreshTokenRequestDto refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.refreshToken();
            Optional<RefreshToken> refreshTokenFromDb = refreshTokenRepository.findByToken(refreshToken);

            if (refreshTokenFromDb.isEmpty() || refreshTokenFromDb.get().getExpiryDate().before(new Date()) || refreshTokenFromDb.get().isRevoked()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDto("Invalid refresh token"));
            }

            RefreshToken validRefreshTokenEntity = refreshTokenFromDb.get();
            User user = validRefreshTokenEntity.getUser();

            JwtDto jwt = jwtUtil.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

            validRefreshTokenEntity.setToken(jwt.getRefreshToken());
            validRefreshTokenEntity.setExpiryDate(Date.from(Instant.now().plusMillis(jwtUtil.getRefreshExpiration())));
            validRefreshTokenEntity.setRevoked(false);

            refreshTokenRepository.save(validRefreshTokenEntity);

            return ResponseEntity.ok(new AuthResponseDto(jwt.getAccessToken(), jwt.getRefreshToken()));
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDto("Invalid refresh token"));
        }
    }

    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.getEmailFromToken(token);

                List<RefreshToken> tokens = refreshTokenRepository.findByUserEmail(email);
                if (!tokens.isEmpty()) {
                    refreshTokenRepository.deleteAll(tokens);
                }
            }

            return ResponseEntity.ok("Logout successfully");
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto("Logout failed"));
        }
    }
}
