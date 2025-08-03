package com.beaver.auth;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionService {

    public boolean hasPermission(Set<String> userPermissions, String requiredPermission) {
        return userPermissions.contains(requiredPermission);
    }

    public boolean isOwner(Set<String> userPermissions) {
        return userPermissions.contains(Permission.WORKSPACE_SETTINGS.getValue());
    }

    public boolean canRead(Set<String> userPermissions, String resource) {
        return userPermissions.contains(resource + ":read");
    }

    public boolean canWrite(Set<String> userPermissions, String resource) {
        return userPermissions.contains(resource + ":write");
    }
}
