package com.bookingapp.security;

import com.bookingapp.constant.MessageConstants;
import com.bookingapp.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(TokenBlacklistService tokenBlacklistService, ObjectMapper objectMapper) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String rolesHeader = request.getHeader("X-Roles");
        String usernameHeader = request.getHeader("X-Username");

        logger.debug(MessageConstants.PROCESSING_REQUEST, request.getMethod(), request.getRequestURI());
        logger.debug(MessageConstants.ROLES_HEADER, rolesHeader);
        logger.debug(MessageConstants.USERNAME_HEADER, usernameHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (tokenBlacklistService.isBlacklisted(token)) {
                logger.debug(MessageConstants.TOKEN_BLACKLISTED_DEBUG, token);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("timestamp", LocalDateTime.now());
                errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                errorResponse.put("error", "Unauthorized");
                errorResponse.put("message", MessageConstants.TOKEN_BLACKLISTED);
                errorResponse.put("path", request.getRequestURI());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }

            if (rolesHeader != null && usernameHeader != null) {
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                        .collect(Collectors.toList());

                var authentication = new UsernamePasswordAuthenticationToken(
                        usernameHeader,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            logger.debug(MessageConstants.CURRENT_AUTH, SecurityContextHolder.getContext().getAuthentication());
        }

        filterChain.doFilter(request, response);

        logger.debug(MessageConstants.FINAL_AUTH_STATE, SecurityContextHolder.getContext().getAuthentication());
    }
} 