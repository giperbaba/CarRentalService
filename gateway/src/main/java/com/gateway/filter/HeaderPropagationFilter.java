package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class HeaderPropagationFilter extends AbstractGatewayFilterFactory<HeaderPropagationFilter.Config> {

    public HeaderPropagationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            String roles = exchange.getRequest().getHeaders().getFirst("X-Roles");
            String username = exchange.getRequest().getHeaders().getFirst("X-Username");

            var modifiedRequest = exchange.getRequest().mutate()
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