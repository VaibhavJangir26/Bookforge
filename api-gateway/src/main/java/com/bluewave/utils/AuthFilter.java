package com.bluewave.utils;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final JwtUtils jwtUtils;
    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String REDIS_SESSION_PREFIX = "user:";

    public AuthFilter(JwtUtils jwtUtils, ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();
            if (!jwtUtils.isTokenValid(token)) {
                return onError(exchange, "JWT token is expired or invalid", HttpStatus.UNAUTHORIZED);
            }

            Claims claims;
            try {
                claims = jwtUtils.getClaims(token);
            } catch (Exception e) {
                return onError(exchange, "Failed to parse JWT claims", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.extractUsername(claims);
            String userId = jwtUtils.extractUserId(claims);
            String roles = jwtUtils.extractRoles(claims);
            String sessionId = jwtUtils.extractSessionId(claims);

            // If session ID is present, verify in Redis that session has not been revoked or logged out
            if (sessionId != null && !sessionId.isBlank() && username != null) {
                String sessionKey = REDIS_SESSION_PREFIX + username + ":" + sessionId;
                return redisTemplate.hasKey(sessionKey)
                        .flatMap(hasKey -> {
                            if (!Boolean.TRUE.equals(hasKey)) {
                                log.warn("Session revoked or expired in Redis for user: {}, session: {}", username, sessionId);
                                return onError(exchange, "Session has expired or been revoked", HttpStatus.UNAUTHORIZED);
                            }
                            return forwardWithUserHeaders(exchange, chain, userId, username, roles);
                        });
            }

            // Fallback for tokens without embedded sessionId
            return forwardWithUserHeaders(exchange, chain, userId, username, roles);
        };
    }

    private Mono<Void> forwardWithUserHeaders(ServerWebExchange exchange,
                                              org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
                                              String userId,
                                              String username,
                                              String roles) {

        // Strip any existing client-provided identity headers to prevent spoofing
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .headers(httpHeaders -> {
                    httpHeaders.remove("X-User-Id");
                    httpHeaders.remove("X-User-Name");
                    httpHeaders.remove("X-User-Roles");
                    httpHeaders.remove("X-User-Role");
                });

        if (userId != null) {
            requestBuilder.header("X-User-Id", userId);
        }
        if (username != null) {
            requestBuilder.header("X-User-Name", username);
        }
        if (roles != null) {
            requestBuilder.header("X-User-Roles", roles);
            requestBuilder.header("X-User-Role", roles);
        }

        ServerHttpRequest mutatedRequest = requestBuilder.build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"success\":false,\"status\":\"%d\",\"message\":\"%s\"}",
                status.value(), errorMessage);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Configuration properties if needed for routes
    }
}
