package com.beaver.auth.cookie;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class ReactiveTokenExtractor {

    /**
     * Extract access token from ServerHttpRequest (for Reactive services)
     */
    public String extractAccessToken(ServerHttpRequest request) {
        if (request.getCookies().containsKey(AuthCookieService.ACCESS_TOKEN_COOKIE)) {
            return request.getCookies().getFirst(AuthCookieService.ACCESS_TOKEN_COOKIE).getValue();
        }
        return null;
    }

    /**
     * Extract refresh token from ServerHttpRequest (for Reactive services)
     */
    public String extractRefreshToken(ServerHttpRequest request) {
        if (request.getCookies().containsKey(AuthCookieService.REFRESH_TOKEN_COOKIE)) {
            return request.getCookies().getFirst(AuthCookieService.REFRESH_TOKEN_COOKIE).getValue();
        }
        return null;
    }
}
