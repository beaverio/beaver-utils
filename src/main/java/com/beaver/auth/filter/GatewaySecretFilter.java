package com.beaver.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Order(2)
@Slf4j
@Component
@ConditionalOnProperty(name = "beaver.auth.gateway-filter.enabled", havingValue = "true", matchIfMissing = true)
public class GatewaySecretFilter implements Filter {

    private final String gatewaySecret;

    public GatewaySecretFilter(@Value("${gateway.secret}") String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
    }

    @PostConstruct
    public void init() {
        log.info("🔒 GatewaySecretFilter ENABLED - Gateway secret validation is active");
        log.info("Gateway secret configured: {}", gatewaySecret != null ? "[CONFIGURED]" : "[NOT SET]");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Skip validation for health/actuator endpoints
        if (path.startsWith("/actuator") || path.equals("/health")) {
            chain.doFilter(request, response);
            return;
        }

        String requestSecret = httpRequest.getHeader("X-Gateway-Secret");

        log.debug("Expected secret: [{}]", gatewaySecret);
        log.debug("Received secret: [{}]", requestSecret);

        if (requestSecret == null) {
            log.warn("Missing X-Gateway-Secret header for request to: {}", path);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden: Missing gateway secret header\"}");
            return;
        }

        if (!gatewaySecret.equals(requestSecret)) {
            log.warn("Invalid service secret for request to: {}", path);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Forbidden: Invalid service secret\"}");
            return;
        }

        log.debug("Gateway secret validated for request to: {}", path);
        chain.doFilter(request, response);
    }
}
