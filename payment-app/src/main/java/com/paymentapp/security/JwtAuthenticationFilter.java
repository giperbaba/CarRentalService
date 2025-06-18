package com.paymentapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentapp.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String rolesHeader = request.getHeader("X-Roles");
        String usernameHeader = request.getHeader("X-Username");
        String userIdHeader = request.getHeader("X-User-ID");
        
        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Headers - X-Roles: {}, X-Username: {}, X-User-ID: {}", 
                 rolesHeader, usernameHeader, userIdHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Проверяем наличие необходимых заголовков
            if (usernameHeader == null || rolesHeader == null) {
                log.warn("Missing required headers: username={}, roles={}", usernameHeader, rolesHeader);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Missing required authentication headers");
                return;
            }

            try {
                // Создаем аутентификацию на основе заголовков
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(role -> {
                        String normalizedRole = role.trim().toUpperCase();
                        if (!normalizedRole.startsWith("ROLE_")) {
                            normalizedRole = "ROLE_" + normalizedRole;
                        }
                        return new SimpleGrantedAuthority(normalizedRole);
                    })
                    .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(usernameHeader, token, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set for user: {} with roles: {}", 
                         usernameHeader, authorities);
            } catch (Exception e) {
                log.error("Error processing authentication: ", e);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Invalid authentication data");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status);
        errorResponse.put("error", status == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden");
        errorResponse.put("message", message);
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
} 