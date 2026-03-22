package com.personalfinance.apigateway.filter;

import com.personalfinance.apigateway.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BLOCKLIST_PREFIX = "blocklist:";

    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/users/register",
            "/api/v1/users/health",
            "/api/v1/wallets/health",
            "/api/v1/transactions/health",
            "/actuator/health"
    );

    public JwtAuthGlobalFilter(JwtUtil jwtUtil, ReactiveStringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            ServerHttpRequest cleanRequest = exchange.getRequest().mutate()
                    .headers(h -> h.remove("X-User-Id"))
                    .headers(h -> h.remove("X-User-Roles"))
                    .headers(h -> h.remove("X-Token-Jti"))
                    .build();
            return chain.filter(exchange.mutate().request(cleanRequest).build());
        }

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

        String jti = jwtUtil.extractJti(token);
        String userId = jwtUtil.extractUserId(token);

        return redisTemplate.hasKey(BLOCKLIST_PREFIX + jti)
                .flatMap(blocked -> {
                    if (Boolean.TRUE.equals(blocked)) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .headers(h -> h.remove("X-User-Id"))
                            .headers(h -> h.remove("X-User-Roles"))
                            .headers(h -> h.remove("X-Token-Jti"))
                            .header("X-User-Id", userId)
                            .header("X-Token-Jti", jti)
                            .headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    @Override
    public int getOrder() {
        return -1; // Run before other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
