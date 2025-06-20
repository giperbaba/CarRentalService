package com.bookingapp.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }

                String userId = request.getHeader("X-User-ID");
                String roles = request.getHeader("X-Roles");
                String username = request.getHeader("X-Username");
                
                if (userId != null) {
                    requestTemplate.header("X-User-ID", userId);
                }
                if (roles != null) {
                    requestTemplate.header("X-Roles", roles);
                }
                if (username != null) {
                    requestTemplate.header("X-Username", username);
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    if (userId == null) {
                        requestTemplate.header("X-User-ID", authentication.getName());
                    }
                    if (roles == null) {
                        requestTemplate.header("X-Roles", authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .reduce((a, b) -> a + "," + b)
                                .orElse(""));
                    }
                    if (username == null) {
                        requestTemplate.header("X-Username", authentication.getName());
                    }
                }
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
} 