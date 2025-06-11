package com.carapp.filter;

import com.carapp.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(TokenValidationFilter.class);

    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    public TokenValidationFilter(TokenBlacklistService tokenBlacklistService, ObjectMapper objectMapper) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String rolesHeader = request.getHeader("X-Roles");
        String usernameHeader = request.getHeader("X-Username");
        
        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        logger.debug("X-Roles header: {}", rolesHeader);
        logger.debug("X-Username header: {}", usernameHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (tokenBlacklistService.isBlacklisted(token)) {
                logger.debug("Token is blacklisted: {}", token);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("timestamp", LocalDateTime.now());
                errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                errorResponse.put("error", "Unauthorized");
                errorResponse.put("message", "Token is blacklisted");
                errorResponse.put("path", request.getRequestURI());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }

            logger.debug("Current authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        }
        
        filterChain.doFilter(request, response);

        logger.debug("Final authentication state: {}", SecurityContextHolder.getContext().getAuthentication());
    }
} 