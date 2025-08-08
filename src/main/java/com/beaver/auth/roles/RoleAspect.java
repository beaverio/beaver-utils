package com.beaver.auth.roles;

import com.beaver.auth.exceptions.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleAspect {

    private final RoleService roleService;

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String workspaceId = request.getHeader("X-Workspace-Id");
        String userId = request.getHeader("X-User-Id");
        String userRoleHeader = request.getHeader("X-User-Role");

        if (workspaceId == null || userId == null) {
            throw new AccessDeniedException("Missing workspace or user context");
        }

        if (userRoleHeader == null || userRoleHeader.trim().isEmpty()) {
            throw new AccessDeniedException("Missing user role context");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(userRoleHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}' for user {} in workspace {}", userRoleHeader, userId, workspaceId);
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
