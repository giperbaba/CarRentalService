package com.paymentapp.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignConfig {
    private static final Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Передаем токен авторизации
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                    logger.debug("Propagating Authorization header");
                }

                // Передаем заголовки с информацией о пользователе
                String userId = request.getHeader("X-User-ID");
                String roles = request.getHeader("X-Roles");
                String username = request.getHeader("X-Username");
                
                if (userId != null) {
                    requestTemplate.header("X-User-ID", userId);
                    logger.debug("Propagating X-User-ID header: {}", userId);
                }
                if (roles != null) {
                    requestTemplate.header("X-Roles", roles);
                    logger.debug("Propagating X-Roles header: {}", roles);
                }
                if (username != null) {
                    requestTemplate.header("X-Username", username);
                    logger.debug("Propagating X-Username header: {}", username);
                }

                // Если заголовки не были переданы в запросе, берем из контекста безопасности
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()) {
                    if (userId == null) {
                        requestTemplate.header("X-User-ID", authentication.getName());
                        logger.debug("Setting X-User-ID from authentication: {}", authentication.getName());
                    }
                    if (roles == null) {
                        String rolesString = authentication.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .reduce((a, b) -> a + "," + b)
                                .orElse("");
                        requestTemplate.header("X-Roles", rolesString);
                        logger.debug("Setting X-Roles from authentication: {}", rolesString);
                    }
                    if (username == null) {
                        requestTemplate.header("X-Username", authentication.getName());
                        logger.debug("Setting X-Username from authentication: {}", authentication.getName());
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