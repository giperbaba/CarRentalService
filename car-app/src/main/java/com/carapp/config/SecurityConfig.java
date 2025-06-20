package com.carapp.config;

import com.carapp.security.PreAuthenticatedUser;
import com.carapp.security.JwtAuthenticationEntryPoint;
import com.carapp.filter.TokenValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final TokenValidationFilter tokenValidationFilter;
    private final ObjectMapper objectMapper;
    @Value("${app.internal-secret}")
    private String internalSecret;

    public SecurityConfig(JwtAuthenticationEntryPoint authenticationEntryPoint,
                        TokenValidationFilter tokenValidationFilter,
                        ObjectMapper objectMapper) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.tokenValidationFilter = tokenValidationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(authentication -> {
            String username = (String) authentication.getPrincipal();
            String rolesHeader = (String) authentication.getCredentials();
            
            logger.debug("Authenticating user: {} with roles: {}", username, rolesHeader);
            
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(role -> {
                        // Ensure role has ROLE_ prefix
                        String normalizedRole = role.trim().toUpperCase();
                        if (!normalizedRole.startsWith("ROLE_")) {
                            normalizedRole = "ROLE_" + normalizedRole;
                        }
                        return new SimpleGrantedAuthority(normalizedRole);
                    })
                    .toList();
            
            logger.debug("User '{}' authenticated with roles: {}", username, authorities);
            return new PreAuthenticatedUser(username, authorities);
        });

        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader("X-Username");
        filter.setCredentialsRequestHeader("X-Roles");
        filter.setAuthenticationManager(provider::authenticate);
        filter.setExceptionIfHeaderMissing(false);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilter(filter)
            .addFilterBefore(tokenValidationFilter, BasicAuthenticationFilter.class)
            .authenticationProvider(provider)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/cars/{id}/available").permitAll()
                .requestMatchers("/api/cars/{id}/status").permitAll()
                .anyRequest().authenticated());

        return http.build();
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
} 