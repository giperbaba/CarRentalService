package com.userapp.service;

import com.userapp.dto.*;
import com.userapp.dto.request.UserRegisterRequestDto;
import com.userapp.dto.request.UserUpdateRequestDto;
import com.userapp.dto.response.AuthResponseDto;
import com.userapp.dto.response.ErrorResponseDto;
import com.userapp.dto.request.AuthRequestDto;
import com.userapp.dto.request.RefreshTokenRequestDto;
import com.userapp.entity.RefreshToken;
import com.userapp.entity.Role;
import com.userapp.entity.User;
import com.userapp.enums.UserRole;
import com.userapp.exception.DeactivatedUserException;
import com.userapp.mapper.UserMapper;
import com.userapp.repository.IRefreshTokenRepository;
import com.userapp.repository.IRoleRepository;
import com.userapp.repository.IUserRepository;
import com.userapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.userapp.constants.ConstantStrings.*;

@Service
public class UserService implements IUserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       IRefreshTokenRepository refreshTokenRepository, IUserRepository userRepository,
                       IRoleRepository roleRepository, UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> login(AuthRequestDto authRequest) {
        try {
            Authentication authentication = authenticateUser(authRequest.login(), authRequest.password());
            User user = (User) authentication.getPrincipal();

            invalidateUserRefreshTokens(user);

            AuthResponseDto authResponse = generateAndSaveTokens(authentication, user);

            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, INVALID_CREDENTIALS_OR_LOGIN_ERROR);
        }
    }

    public ResponseEntity<?> register(UserRegisterRequestDto registerRequest) {
        try {
            if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
                return buildErrorResponse(HttpStatus.CONFLICT, USER_ALREADY_EXISTS);
            }

            User newUser = createUserFromRegistration(registerRequest);
            assignDefaultRole(newUser);
            userRepository.save(newUser);

            Authentication authentication = authenticateUser(newUser.getEmail(), registerRequest.password());
            AuthResponseDto authResponse = generateAndSaveTokens(authentication, newUser);

            return ResponseEntity.ok(authResponse);
        } catch (IllegalStateException ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_ROLE_NOT_FOUND);
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, REGISTRATION_FAILED);
        }
    }

    @Transactional
    public ResponseEntity<?> refresh(RefreshTokenRequestDto refreshTokenRequest) {
        try {
            RefreshToken validRefreshTokenEntity = validateAndRetrieveRefreshToken(refreshTokenRequest.refreshToken());
            User user = validRefreshTokenEntity.getUser();

            AuthResponseDto authResponse = updateAndSaveRefreshToken(validRefreshTokenEntity, user);

            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_TOKEN);
        }
    }

    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                jwtUtil.blacklistToken(token);
                String email = jwtUtil.getEmailFromToken(token);
                refreshTokenRepository.deleteByUserEmail(email);
                return ResponseEntity.ok(LOGOUT_SUCCESS);
            }
            return buildErrorResponse(HttpStatus.BAD_REQUEST, TOKEN_EXTRACTION_FAILED);
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, LOGOUT_FAILED);
        }
    }

    public ResponseEntity<?> getMyProfile() {
        try {
            User currentUser = findAuthenticatedUser();
            return ResponseEntity.ok(userMapper.userToUserResponseDto(currentUser));
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, USER_PROFILE_RETRIEVAL_FAILED);
        }
    }

    public ResponseEntity<?> getUserProfile(UUID userId) {
        try {
            User user = findUserById(userId);
            return ResponseEntity.ok(userMapper.userToUserResponseDto(user));
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, USER_PROFILE_RETRIEVAL_FAILED);
        }
    }


    @Transactional
    public ResponseEntity<?> updateMyProfile(UserUpdateRequestDto updateRequest) {
        try {
            User currentUser = findAuthenticatedUser();
            userMapper.updateUserFromDto(updateRequest, currentUser);
            userRepository.save(currentUser);
            return ResponseEntity.ok(userMapper.userToUserResponseDto(currentUser));
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, USER_PROFILE_UPDATE_FAILED);
        }
    }

    @Transactional
    public ResponseEntity<?> deactivateUser(UUID userId) {
        try {
            User user = findUserById(userId);
            if (!user.isActive()) {
                return buildErrorResponse(HttpStatus.CONFLICT, USER_ALREADY_DEACTIVATED);
            }

            user.setActive(false);
            userRepository.save(user);

            invalidateUserRefreshTokens(user);
            return ResponseEntity.ok(USER_DEACTIVATION_SUCCESS);
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, USER_DEACTIVATION_FAILED);
        }
    }


    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(message));
    }

    private Authentication authenticateUser(String login, String password) {
        // BadCredentialsException (если неверные учетные данные) будет перехвачено вызывающим методом
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
    }

    private void invalidateUserRefreshTokens(User user) {
        List<RefreshToken> oldTokens = refreshTokenRepository.findByUser(user);
        if (!oldTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(oldTokens);
        }
    }

    private AuthResponseDto generateAndSaveTokens(Authentication authentication, User user) {
        JwtDto jwt = jwtUtil.generateToken(authentication);
        RefreshToken refreshTokenEntity = createRefreshTokenEntity(jwt.getRefreshToken(), user);
        refreshTokenRepository.save(refreshTokenEntity);
        return new AuthResponseDto(jwt.getAccessToken(), jwt.getRefreshToken());
    }

    private RefreshToken createRefreshTokenEntity(String token, User user) {
        return new RefreshToken(token, Date.from(Instant.now().plusMillis(jwtUtil.getRefreshExpiration())), user);
    }

    private User createUserFromRegistration(UserRegisterRequestDto registerRequest) {
        User user = userMapper.registerRequestToUser(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setActive(true);
        return user;
    }

    private void assignDefaultRole(User user) {
        Role userRole = roleRepository.findByRole(UserRole.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(DEFAULT_ROLE_NOT_FOUND));
        user.setRoles(Set.of(userRole));
    }

    private RefreshToken validateAndRetrieveRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenFromDb = refreshTokenRepository.findByToken(token);

        if (refreshTokenFromDb.isEmpty() ||
                refreshTokenFromDb.get().getExpiryDate().before(new Date()) ||
                refreshTokenFromDb.get().isRevoked()) {
            throw new IllegalArgumentException(INVALID_REFRESH_TOKEN);
        }
        return refreshTokenFromDb.get();
    }

    private AuthResponseDto updateAndSaveRefreshToken(RefreshToken refreshTokenEntity, User user) {
        JwtDto jwt = jwtUtil.generateToken(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        refreshTokenEntity.setToken(jwt.getRefreshToken());
        refreshTokenEntity.setExpiryDate(Date.from(Instant.now().plusMillis(jwtUtil.getRefreshExpiration())));
        refreshTokenEntity.setRevoked(false);
        refreshTokenRepository.save(refreshTokenEntity);
        return new AuthResponseDto(jwt.getAccessToken(), jwt.getRefreshToken());
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private User findAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));
    }

    private void updateUserFromDto(User user, UserUpdateRequestDto updateRequest) {
        userMapper.updateUserFromDto(updateRequest, user);
    }

    //TODO: Если пользователь деактивирован что он не может делать?
    private void checkUserActive(User user) {
        if (!user.isActive()) {
            throw new DeactivatedUserException(USER_ACCOUNT_DEACTIVATED);
        }
    }
}