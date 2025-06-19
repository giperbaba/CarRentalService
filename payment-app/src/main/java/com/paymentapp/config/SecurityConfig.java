package com.paymentapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/payments/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        logger.error("Authentication error: {}", authException.getMessage());
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("timestamp", LocalDateTime.now());
                        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                        errorResponse.put("error", "Unauthorized");
                        errorResponse.put("message", "Authentication failed: " + authException.getMessage());
                        errorResponse.put("path", request.getRequestURI());
                        objectMapper.writeValue(response.getOutputStream(), errorResponse);
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        logger.error("Access denied: {}", accessDeniedException.getMessage());
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("timestamp", LocalDateTime.now());
                        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
                        errorResponse.put("error", "Forbidden");
                        errorResponse.put("message", "Access Denied: You don't have permission to access this resource");
                        errorResponse.put("path", request.getRequestURI());
                        objectMapper.writeValue(response.getOutputStream(), errorResponse);
                    }))
                .build();
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setMaxPayloadLength(10000);
        return loggingFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-User-ID",
                "X-Roles",
                "X-Username",
                "X-Request-ID"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "X-User-ID",
                "X-Roles",
                "X-Username",
                "X-Request-ID"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 