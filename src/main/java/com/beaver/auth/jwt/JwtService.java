package com.beaver.auth.jwt;

import com.beaver.auth.exceptions.InvalidRefreshTokenException;
import com.beaver.auth.exceptions.AuthenticationFailedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(AccessToken claims) {
        return generateToken(Map.of(
                "userId", claims.userId(),
                "email", claims.email(),
                "name", claims.name(),
                "workspaceId", claims.workspaceId(),
                "role", claims.role(),  // Changed from permissions() to role()
                "type", "access"
        ), jwtConfig.getAccessTokenValidity() * 60 * 1000);
    }

    public String generateRefreshToken(RefreshToken claims) {
        return generateToken(Map.of(
            "userId", claims.userId,
            "workspaceId", claims.workspaceId,
            "type", "refresh"
        ), jwtConfig.getRefreshTokenValidity() * 60 * 1000); // minutes to milliseconds
    }

    private String generateToken(Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public Mono<String> extractUserId(String token) {
        return extractAllClaims(token)
                .map(claims -> claims.get("userId", String.class))
                .filter(Objects::nonNull);
    }

    public Mono<String> extractWorkspaceId(String token) {
        return extractAllClaims(token)
                .map(claims -> claims.get("workspaceId", String.class))
                .filter(Objects::nonNull);
    }

    public Mono<String> extractRole(String token) {
        return extractAllClaims(token)
                .map(claims -> claims.get("role", String.class))
                .filter(Objects::nonNull);
    }

    public Mono<String> extractTokenType(String token) {
        return extractAllClaims(token)
                .map(claims -> claims.get("type", String.class))
                .filter(Objects::nonNull);
    }

    public Mono<Date> extractExpiration(String token) {
        return extractAllClaims(token)
                .map(Claims::getExpiration)
                .filter(Objects::nonNull);
    }

    public Mono<Claims> extractAllClaims(String token) {
        return Mono.fromCallable(() ->
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
        ).onErrorMap(Exception.class, ex -> new RuntimeException("Invalid JWT token", ex));
    }

    public Mono<Boolean> isTokenExpired(String token) {
        return extractExpiration(token)
                .map(expiration -> expiration.before(new Date()));
    }

    public Mono<Boolean> isValidAccessToken(String token) {
        return validateTokenType(token, "access");
    }

    public Mono<Boolean> isValidRefreshToken(String token) {
        return validateTokenType(token, "refresh");
    }

    private Mono<Boolean> validateTokenType(String token, String expectedType) {
        return extractTokenType(token)
                .filter(expectedType::equals)
                .flatMap(type -> isTokenExpired(token)
                        .map(expired -> !expired)
                        .onErrorReturn(false))
                .defaultIfEmpty(false)
                .onErrorReturn(false);
    }

    public Mono<Void> validateRefreshToken(String token) {
        return isValidRefreshToken(token)
                .filter(isValid -> isValid)
                .switchIfEmpty(Mono.error(new InvalidRefreshTokenException("Invalid refresh token: token is invalid or expired")))
                .then();
    }

    public Mono<String> extractUserIdFromToken(String token) {
        return extractUserId(token)
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Token does not contain userId claim")));
    }

    public Mono<String> extractWorkspaceIdFromToken(String token) {
        return extractWorkspaceId(token)
                .switchIfEmpty(Mono.error(new AuthenticationFailedException("Token does not contain workspaceId claim")));
    }
}
