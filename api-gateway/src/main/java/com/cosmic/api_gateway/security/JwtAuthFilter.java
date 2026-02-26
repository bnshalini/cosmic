package com.cosmic.api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("JWT FILTER EXECUTING");

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // =========================
        // PUBLIC ENDPOINTS
        // =========================
        if (path.startsWith("/api/auth/")
                || (path.startsWith("/api/products") && method == HttpMethod.GET)) {
            return chain.filter(exchange);
        }
        System.out.println("Incoming path: " + path);

        // =========================
        // AUTHORIZATION HEADER CHECK
        // =========================
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // =========================
        // TOKEN VALIDATION
        // =========================
        try {
            jwtUtil.validateToken(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // =========================
        // EXTRACT ROLE
        // =========================
        String role;
        try {
            role = jwtUtil.extractRole(token);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // =========================
        // PRODUCT WRITE → ADMIN ONLY
        // =========================
        if (path.startsWith("/api/products") && method != HttpMethod.GET) {
            if (!"ROLE_ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // =========================
        // ORDER STATUS UPDATE → ADMIN ONLY
        // Example: PUT /api/orders/5/status
        // =========================
        if (method == HttpMethod.PUT && path.matches("/api/orders/\\d+/status")) {
            if (!"ROLE_ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // =========================
        // CART / ORDERS / PAYMENT → CUSTOMER OR ADMIN
        // =========================
        if (path.startsWith("/api/cart")
                || path.startsWith("/api/orders")
                || path.startsWith("/api/payments")) {

            if (!"ROLE_CUSTOMER".equals(role) && !"ROLE_ADMIN".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        // =========================
        // BLACKLIST VALIDATION (USER SERVICE)
        // =========================
        return webClientBuilder.build()
                .get()
                .uri("http://USER-SERVICE/api/auth/validate-token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> chain.filter(exchange))
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
