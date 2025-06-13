package com.bookingapp.config;

import feign.RequestInterceptor;
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                requestTemplate.header("X-Username", authentication.getName());
                requestTemplate.header("X-Roles", String.join(",", 
                    authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .toList()
                ));
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        if (headerName.startsWith("X-") && 
                            !headerName.equals("X-Username") && 
                            !headerName.equals("X-Roles")) {
                            String headerValue = request.getHeader(headerName);
                            requestTemplate.header(headerName, headerValue);
                        }
                    }
                }
            }
        };
    }
} 