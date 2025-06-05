package com.userapp.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userapp.dto.response.ErrorResponseDto;
import com.userapp.exception.DeactivatedUserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

import io.jsonwebtoken.ExpiredJwtException;

import static com.userapp.constants.ConstantStrings.*;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String errorMessage;

        if (authException instanceof BadCredentialsException) {
            errorMessage = INVALID_CREDENTIALS_OR_LOGIN_ERROR;
            status = HttpStatus.UNAUTHORIZED;
        } else if (authException.getCause() instanceof ExpiredJwtException) {
            errorMessage = JWT_TOKEN_EXPIRED;
            status = HttpStatus.UNAUTHORIZED;
        }
        else if (authException.getCause() instanceof DeactivatedUserException) {
            errorMessage = USER_ACCOUNT_DEACTIVATED;
            status = HttpStatus.FORBIDDEN;
        }
        else {
            errorMessage = authException.getMessage() != null ? authException.getMessage() : "Authentication failed due to an unknown reason.";
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDto errorResponse = new ErrorResponseDto(errorMessage);
        OutputStream out = response.getOutputStream();
        objectMapper.writeValue(out, errorResponse);
        out.flush();
    }
}