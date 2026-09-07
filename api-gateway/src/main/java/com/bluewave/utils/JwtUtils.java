package com.bluewave.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:${JWT_SECRET_KEY:asdkjf1343adsfjlajfyuhb45ndahbxm43dfsdsf}}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public String extractUserId(Claims claims) {
        Object userId = claims.get("userId");
        return userId != null ? String.valueOf(userId) : null;
    }

    public String extractSessionId(Claims claims) {
        Object sessionId = claims.get("sessionId");
        return sessionId != null ? String.valueOf(sessionId) : null;
    }

    @SuppressWarnings("unchecked")
    public String extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?> roleList) {
            return String.join(",", roleList.stream().map(Object::toString).toList());
        }
        Object roleSingle = claims.get("role");
        return roleSingle != null ? String.valueOf(roleSingle) : "";
    }
}
