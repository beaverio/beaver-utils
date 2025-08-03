package com.beaver.auth.cookie;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * Extract access token from Servlet HttpServletRequest (for Services)
     */
    public String extractAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthCookieService.ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Extract refresh token from Servlet HttpServletRequest (for Services)
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
