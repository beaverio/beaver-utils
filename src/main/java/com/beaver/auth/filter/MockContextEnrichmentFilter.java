package com.beaver.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "beaver.dev.mock-context.enabled", havingValue = "true")
@EnableConfigurationProperties(MockContextEnrichmentFilter.MockContextProperties.class)
public class MockContextEnrichmentFilter extends OncePerRequestFilter {

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_WORKSPACE_ID = "X-Workspace-Id";
    private static final String X_USER_ROLE = "X-User-Role";
    private static final String X_GATEWAY_SECRET = "X-Gateway-Secret";
    private static final String DEFAULT_MOCK_ROLE = "OWNER";

    private final MockContextProperties mockContextProperties;

    @PostConstruct
    public void init() {
        log.info("🚀 MockContextEnrichmentFilter ENABLED - RBAC Local development mode active");
        log.info("Mock headers will be injected: X-User-Id, X-Workspace-Id, X-User-Role, X-Gateway-Secret");

        if (mockContextProperties.getUsers().isEmpty()) {
            log.warn("No users configured in beaver.dev.mock-context.users - using defaults");
        } else {
            int selectedIndex = mockContextProperties.getDefaultUser();
            if (selectedIndex >= 0 && selectedIndex < mockContextProperties.getUsers().size()) {
                MockUser selectedUser = mockContextProperties.getUsers().get(selectedIndex);
                log.info("Using configured mock user: {} (role: {}) in workspace: {}",
                        selectedUser.getUserId(), selectedUser.getRole(), selectedUser.getWorkspaceId());
            } else {
                log.warn("Invalid selected-user index: {} (max: {}). Using first user or defaults.",
                        selectedIndex, mockContextProperties.getUsers().size() - 1);
            }
        }
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

        MockGatewayRequestWrapper wrappedRequest = new MockGatewayRequestWrapper(request, mockContextProperties);

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

        public MockGatewayRequestWrapper(HttpServletRequest request, MockContextProperties config) {
            super(request);
            this.request = request;

            MockUser selectedUser = getSelectedUser(config);

            if (request.getHeader(X_USER_ID) == null) {
                mockHeaders.put(X_USER_ID, selectedUser.getUserId());
            }

            if (request.getHeader(X_WORKSPACE_ID) == null) {
                mockHeaders.put(X_WORKSPACE_ID, selectedUser.getWorkspaceId());
            }

            if (request.getHeader(X_USER_ROLE) == null) {
                mockHeaders.put(X_USER_ROLE, selectedUser.getRole());
            }

            if (request.getHeader(X_GATEWAY_SECRET) == null) {
                String gatewaySecret = config.getGateway() != null && config.getGateway().getSecret() != null
                        ? config.getGateway().getSecret()
                        : "local-gateway";
                mockHeaders.put(X_GATEWAY_SECRET, gatewaySecret);
            }
        }

        private MockUser getSelectedUser(MockContextProperties config) {
            if (config.getUsers().isEmpty()) {
                // Return default values if no users configured
                return new MockUser("550e8400-e29b-41d4-a716-446655440001",
                        "550e8400-e29b-41d4-a716-446655440002",
                        DEFAULT_MOCK_ROLE);
            }

            int selectedIndex = config.getDefaultUser();
            if (selectedIndex >= 0 && selectedIndex < config.getUsers().size()) {
                return config.getUsers().get(selectedIndex);
            } else {
                // Invalid index, use first user
                return config.getUsers().get(0);
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

    @Data
    @ConfigurationProperties(prefix = "beaver.dev.mock-context")
    public static class MockContextProperties {
        private boolean enabled = false;
        private int defaultUser = 0;
        private Gateway gateway = new Gateway();
        private List<MockUser> users = new ArrayList<>();

        @Data
        public static class Gateway {
            private String secret;
        }
    }

    @Data
    public static class MockUser {
        private String userId;
        private String workspaceId;
        private String role;

        public MockUser() {}

        public MockUser(String userId, String workspaceId, String role) {
            this.userId = userId;
            this.workspaceId = workspaceId;
            this.role = role;
        }
    }
}
