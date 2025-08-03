package com.beaver.auth.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AuthCookieService {

    @Value("${jwt.access-token-validity:15}")
    private int accessTokenValidityMinutes;

    @Value("${jwt.refresh-token-validity:10080}")
    private int refreshTokenValidityMinutes;

    @Value("${auth.cookies.secure:true}")
    private boolean secureCookies;

    @Value("${auth.cookies.domain:}")
    private String cookieDomain;

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    public ResponseCookie createAccessTokenCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .path("/")
                .maxAge(accessTokenValidityMinutes * 60L); // Convert minutes to seconds

        if (!cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshTokenValidityMinutes * 60L); // Convert minutes to seconds

        if (!cookieDomain.isEmpty()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
    }

    /**
     * Extract access token from ServerHttpRequest (Reactive environments)
     */
    public String extractAccessToken(ServerHttpRequest request) {
        return extractTokenFromReactiveRequest(request);
    }

    /**
     * Extract refresh token from HttpServletRequest (Servlet environments)
     */
    public String extractRefreshToken(HttpServletRequest request) {
        return extractTokenFromServletRequest(request);
    }

    private String extractTokenFromServletRequest(HttpServletRequest request) {
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

    private String extractTokenFromReactiveRequest(ServerHttpRequest request) {
        if (request.getCookies().containsKey(AuthCookieService.ACCESS_TOKEN_COOKIE)) {
            return request.getCookies().getFirst(AuthCookieService.ACCESS_TOKEN_COOKIE).getValue();
        }
        return null;
    }
}
