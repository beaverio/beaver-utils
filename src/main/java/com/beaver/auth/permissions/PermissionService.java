package com.beaver.auth.permissions;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionService {

    public boolean hasPermission(Set<String> userPermissions, String requiredPermission) {
        return userPermissions.contains(requiredPermission);
    }
}
