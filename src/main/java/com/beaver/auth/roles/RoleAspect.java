package com.beaver.auth.roles;

import com.beaver.auth.exceptions.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAspect {

    private final RoleService roleService;

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new AccessDeniedException("Missing or invalid JWT authentication");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();

        String workspaceId = jwt.getClaimAsString("workspace_id");
        String userId = jwt.getClaimAsString("user_id");
        String userRoleString = jwt.getClaimAsString("role");

        if (workspaceId == null || userId == null) {
            throw new AccessDeniedException("Missing workspace or user context in JWT");
        }

        if (userRoleString == null || userRoleString.trim().isEmpty()) {
            throw new AccessDeniedException("Missing user role context in JWT");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(userRoleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}' for user {} in workspace {}", userRoleString, userId, workspaceId);
            throw new AccessDeniedException("Invalid user role");
        }

        Role requiredRole = requiresRole.value();

        if (!roleService.hasAccess(userRole, requiredRole)) {
            log.warn("Access denied for user {} in workspace {}. User role: {}, Required role: {}",
                    userId, workspaceId, userRole, requiredRole);
            throw new AccessDeniedException("Insufficient role level. Required: " + requiredRole + ", User has: " + userRole);
        }

        log.debug("Access granted for user {} in workspace {} with role {} for operation requiring {}",
                userId, workspaceId, userRole, requiredRole);

        return joinPoint.proceed();
    }
}
