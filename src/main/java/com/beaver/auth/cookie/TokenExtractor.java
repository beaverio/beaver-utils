package com.beaver.auth.cookie;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    /**
     * Extract access token from WebFlux ServerHttpRequest (for Gateway)
     */
    public String extractAccessToken(ServerHttpRequest request) {
        if (request.getCookies().containsKey(AuthCookieService.ACCESS_TOKEN_COOKIE)) {
            return request.getCookies().getFirst(AuthCookieService.ACCESS_TOKEN_COOKIE).getValue();
        }
        return null;
    }

    /**
     * Extract refresh token from WebFlux ServerHttpRequest (for Gateway)
     */
    public String extractRefreshToken(ServerHttpRequest request) {
        if (request.getCookies().containsKey(AuthCookieService.REFRESH_TOKEN_COOKIE)) {
            return request.getCookies().getFirst(AuthCookieService.REFRESH_TOKEN_COOKIE).getValue();
        }
        return null;
    }
}
