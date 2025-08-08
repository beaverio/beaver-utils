package com.beaver.auth.roles;

import org.springframework.stereotype.Service;

@Service
public class RoleService {

    /**
     * Check if the user's role has access to perform operations requiring the specified role
     * @param userRole The role of the current user
     * @param requiredRole The minimum role required for the operation
     * @return true if the user has sufficient access
     */
    public boolean hasAccess(Role userRole, Role requiredRole) {
        return userRole.hasAccess(requiredRole);
    }

    /**
     * Check if the manager role can assign/remove the target role
     * @param managerRole The role of the user trying to manage roles
     * @param targetRole The role being assigned/removed
     * @return true if the manager can manage the target role
     */
    public boolean canManageRole(Role managerRole, Role targetRole) {
        return managerRole.canManageRole(targetRole);
    }

    /**
     * Get the hierarchy level of a role (useful for comparisons)
     * @param role The role to get the level for
     * @return The numeric level of the role
     */
    public int getRoleLevel(Role role) {
        return role.getLevel();
    }
}
