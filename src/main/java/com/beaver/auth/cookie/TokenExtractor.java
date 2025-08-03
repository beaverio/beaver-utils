package com.beaver.auth.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
     * Extract refresh token from WebFlux ServerHttpRequest (for User-Service)
     */
    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthCookieService.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
