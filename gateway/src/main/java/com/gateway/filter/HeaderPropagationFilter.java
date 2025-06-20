package com.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class HeaderPropagationFilter extends AbstractGatewayFilterFactory<HeaderPropagationFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(HeaderPropagationFilter.class);

    public HeaderPropagationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String userId = request.getHeaders().getFirst("X-User-ID");
            String roles = request.getHeaders().getFirst("X-Roles");
            String username = request.getHeaders().getFirst("X-Username");
            
            logger.debug("Propagating headers - UserId: {}, Roles: {}, Username: {}", userId, roles, username);

            if (userId == null || roles == null) {
                logger.warn("Missing required headers: userId={}, roles={}", userId, roles);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .header("X-Roles", roles)
                    .header("X-Username", username)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
    }
} 