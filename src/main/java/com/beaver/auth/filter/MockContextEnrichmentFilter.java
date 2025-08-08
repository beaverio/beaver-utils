package com.beaver.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Mock Context Filter for local development.
 * Simulates the gateway's header injection behavior when running services independently.
 * Updated for RBAC system - injects role instead of permissions.
 * Only active when beaver.dev.mock-context.enabled=true
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "beaver.dev.mock-context.enabled", havingValue = "true")
public class MockContextEnrichmentFilter extends OncePerRequestFilter {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_WORKSPACE_ID = "X-Workspace-Id";
    private static final String X_USER_ROLE = "X-User-Role";
    private static final String X_GATEWAY_SECRET = "X-Gateway-Secret";

    private static final String DEFAULT_MOCK_ROLE = "OWNER";

    @PostConstruct
    public void init() {
        log.info("🚀 MockContextEnrichmentFilter ENABLED - RBAC Local development mode active");
        log.info("Mock headers will be injected: X-User-Id, X-Workspace-Id, X-User-Role, X-Gateway-Secret");
        log.info("Default mock role: {}", DEFAULT_MOCK_ROLE);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip auth endpoints (they don't need context headers)
        if (path.startsWith("/auth/") || path.startsWith("/users/auth/")) {
            log.debug("Skipping mock gateway headers for auth endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        MockGatewayRequestWrapper wrappedRequest = new MockGatewayRequestWrapper(request);

        log.debug("Added mock gateway headers for path: {} - User: {}, Workspace: {}, Role: {}",
                 path, wrappedRequest.getHeader(X_USER_ID), wrappedRequest.getHeader(X_WORKSPACE_ID),
                 wrappedRequest.getHeader(X_USER_ROLE));

        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * Request wrapper that adds mock gateway headers if they don't already exist
     */
    private static class MockGatewayRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> mockHeaders = new HashMap<>();
        private final HttpServletRequest request;

        public MockGatewayRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;

            if (request.getHeader(X_USER_ID) == null) {
                mockHeaders.put(X_USER_ID, "550e8400-e29b-41d4-a716-446655440001");
            }

            if (request.getHeader(X_WORKSPACE_ID) == null) {
                mockHeaders.put(X_WORKSPACE_ID, "550e8400-e29b-41d4-a716-446655440002");
            }

            if (request.getHeader(X_USER_ROLE) == null) {
                mockHeaders.put(X_USER_ROLE, DEFAULT_MOCK_ROLE);
            }

            if (request.getHeader(X_GATEWAY_SECRET) == null) {
                mockHeaders.put(X_GATEWAY_SECRET, "local-gateway");
            }
        }

        @Override
        public String getHeader(String name) {
            // Return mock header if it exists, otherwise delegate to original request
            if (mockHeaders.containsKey(name)) {
                return mockHeaders.get(name);
            }
            return request.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (mockHeaders.containsKey(name)) {
                return Collections.enumeration(Collections.singletonList(mockHeaders.get(name)));
            }
            return request.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> headerNames = new HashSet<>();
            headerNames.addAll(mockHeaders.keySet());

            Enumeration<String> originalHeaders = request.getHeaderNames();
            while (originalHeaders.hasMoreElements()) {
                headerNames.add(originalHeaders.nextElement());
            }

            return Collections.enumeration(headerNames);
        }
    }
}
