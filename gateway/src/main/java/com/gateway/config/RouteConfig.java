package com.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/user/**")
                        .filters(f -> f
                            .preserveHostHeader()
                            .retry(retryConfig -> retryConfig
                                .setRetries(1)
                                .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                            )
                        )
                        .uri("http://localhost:8081"))
                .route("car-service", r -> r
                        .path("/api/cars/**")
                        .filters(f -> f
                            .preserveHostHeader()
                            .retry(retryConfig -> retryConfig
                                .setRetries(1)
                                .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR)
                            )
                        )
                        .uri("http://localhost:8082"))
                .build();
    }
} 