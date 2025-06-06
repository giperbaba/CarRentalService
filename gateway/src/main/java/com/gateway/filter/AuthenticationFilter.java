package com.gateway.filter;

import com.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class AuthenticationFilter implements GatewayFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh"
    );

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Generate and set request ID
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        // Log incoming request
        logger.info("Incoming request: {} {}", request.getMethod(), path);

        // Skip authentication for open endpoints
        if (OPEN_ENDPOINTS.stream().anyMatch(path::endsWith)) {
            return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doFinally(signalType -> MDC.clear());
        }

        // Check for Authorization header
        List<String> authHeaders = request.getHeaders().get(AUTHORIZATION_HEADER);
        if (authHeaders == null || authHeaders.isEmpty()) {
            return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        // Continue with the valid request
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> MDC.clear());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        logger.error("Authentication error: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
} 