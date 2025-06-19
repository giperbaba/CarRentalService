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
        
        try {
            String userId = request.getHeader("X-User-ID");
            String roles = request.getHeader("X-Roles");
            String username = request.getHeader("X-Username");

            log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
            log.debug("Headers - X-Roles: {}, X-Username: {}, X-User-ID: {}", roles, username, userId);

            if (userId != null && roles != null) {
                List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set for user: {}", userId);
            } else {
                log.debug("No authentication headers found");
            }
        } catch (Exception e) {
            log.error("Authentication error", e);
            SecurityContextHolder.clearContext();
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