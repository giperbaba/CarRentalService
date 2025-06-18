package com.gateway.filter;

import com.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String ROLES_HEADER = "X-Roles";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String USER_ID_HEADER = "X-User-ID";
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

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        logger.debug("Processing request: {} {}", request.getMethod(), path);
        logger.debug("Original headers: {}", request.getHeaders());

        if (OPEN_ENDPOINTS.stream().anyMatch(path::endsWith)) {
            logger.debug("Skipping authentication for open endpoint: {}", path);
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            logger.warn("No authorization header found in request");
            return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid authorization header format: {}", authHeader);
            return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            logger.warn("Invalid JWT token");
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }

        List<String> roles = jwtUtil.getRolesFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String userId = jwtUtil.getUserIdFromToken(token);

        logger.debug("Extracted from token - UserId: {}, Roles: {}, Username: {}", userId, roles, username);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .header(ROLES_HEADER, String.join(",", roles))
                .header(USERNAME_HEADER, username)
                .header(USER_ID_HEADER, userId)
                .build();

        logger.debug("Modified request headers: {}", modifiedRequest.getHeaders());
        logger.debug("Modified request path: {}", modifiedRequest.getPath());
        logger.debug("Modified request URI: {}", modifiedRequest.getURI());

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        logger.error("Authentication error: {}", message);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
} 