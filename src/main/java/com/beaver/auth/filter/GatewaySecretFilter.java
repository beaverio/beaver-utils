package com.beaver.auth.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Order(1)
@Slf4j
public class GatewaySecretFilter implements Filter {

    private final String gatewaySecret;

    public GatewaySecretFilter(String gatewaySecret) {
        this.gatewaySecret = gatewaySecret;
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

        // Validate gateway secret header
        String requestSecret = httpRequest.getHeader("X-Gateway-Secret");
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
