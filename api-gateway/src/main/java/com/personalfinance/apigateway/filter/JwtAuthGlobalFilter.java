package com.personalfinance.apigateway.filter;

import com.personalfinance.apigateway.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/users/register",
            "/api/v1/users/health",
            "/api/v1/wallets/health",
            "/actuator/health"
    );

    public JwtAuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Allow public endpoints through without auth
        if (isPublicPath(path)) {
            // Strip any spoofed X-User-Id header on public paths too
            ServerHttpRequest cleanRequest = exchange.getRequest().mutate()
                    .headers(h -> h.remove("X-User-Id"))
                    .headers(h -> h.remove("X-User-Roles"))
                    .build();
            return chain.filter(exchange.mutate().request(cleanRequest).build());
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract userId from token and forward as trusted header
        String userId = jwtUtil.extractUserId(token);

        // Mutate request: add X-User-Id, strip Authorization (downstream doesn't need it)
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(h -> h.remove("X-User-Id"))    // strip any spoofed header
                .headers(h -> h.remove("X-User-Roles"))  // strip any spoofed header
                .header("X-User-Id", userId)
                .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
