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
 * Mock Gateway Filter for local development.
 * Simulates the gateway's header injection behavior when running services independently.
 * Only active when beaver.dev.mock-gateway.enabled=true
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "beaver.dev.mock-gateway.enabled", havingValue = "true")
public class MockGatewayFilter extends OncePerRequestFilter {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_WORKSPACE_ID = "X-Workspace-Id";
    private static final String X_USER_PERMISSIONS = "X-User-Permissions";
    private static final String X_GATEWAY_SECRET = "X-Gateway-Secret";

    // Base permissions that every mock user should have
    private final Set<String> basePermissions = Set.of(
        "user:read", "user:write",
        "workspace:read", "workspace:write", "workspace:owner"
    );

    // Additional permissions registered by services
    private final Set<String> additionalPermissions = new HashSet<>();

    @PostConstruct
    public void init() {
        log.info("🚀 MockGatewayFilter ENABLED - Local development mode active");
        log.info("Mock headers will be injected: X-User-Id, X-Workspace-Id, X-User-Permissions, X-Gateway-Secret");
        log.info("Base permissions: {}", basePermissions);
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

        // Only add headers if they're not already present (e.g., from manual testing)
        MockGatewayRequestWrapper wrappedRequest = new MockGatewayRequestWrapper(request, getAllPermissions());

        log.debug("Added mock gateway headers for path: {} - User: {}, Workspace: {}, Permissions: {}",
                 path, wrappedRequest.getHeader(X_USER_ID), wrappedRequest.getHeader(X_WORKSPACE_ID),
                 wrappedRequest.getHeader(X_USER_PERMISSIONS));

        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * Allow services to register additional permissions for local development
     */
    public void addServicePermissions(Collection<String> permissions) {
        additionalPermissions.addAll(permissions);
        log.info("Added {} additional permissions to mock gateway: {}", permissions.size(), permissions);
    }

    private Set<String> getAllPermissions() {
        Set<String> allPermissions = new HashSet<>(basePermissions);
        allPermissions.addAll(additionalPermissions);
        return allPermissions;
    }

    /**
     * Request wrapper that adds mock gateway headers if they don't already exist
     */
    private static class MockGatewayRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> mockHeaders = new HashMap<>();
        private final HttpServletRequest request;

        public MockGatewayRequestWrapper(HttpServletRequest request, Set<String> permissions) {
            super(request);
            this.request = request;

            if (request.getHeader(X_USER_ID) == null) {
                mockHeaders.put(X_USER_ID, "550e8400-e29b-41d4-a716-446655440001");
            }

            if (request.getHeader(X_WORKSPACE_ID) == null) {
                mockHeaders.put(X_WORKSPACE_ID, "550e8400-e29b-41d4-a716-446655440002");
            }

            if (request.getHeader(X_USER_PERMISSIONS) == null) {
                mockHeaders.put(X_USER_PERMISSIONS, String.join(",", permissions));
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
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            // If we have a mock header, return it as a single-value enumeration
            if (mockHeaders.containsKey(name)) {
                return Collections.enumeration(List.of(mockHeaders.get(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            // Combine original headers with mock headers
            Set<String> headerNames = new HashSet<>();

            // Add original headers
            Enumeration<String> originalHeaders = super.getHeaderNames();
            while (originalHeaders.hasMoreElements()) {
                headerNames.add(originalHeaders.nextElement());
            }

            // Add mock headers
            headerNames.addAll(mockHeaders.keySet());

            return Collections.enumeration(headerNames);
        }
    }
}
