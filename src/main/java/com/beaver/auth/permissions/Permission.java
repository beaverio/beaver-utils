package com.beaver.auth.permissions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {
    DENY_ALL("deny:all"),

    // Financial permissions
    TRANSACTION_READ("transaction:read"),
    TRANSACTION_WRITE("transaction:write"),
    BUDGET_READ("budget:read"),
    BUDGET_WRITE("budget:write"),
    REPORT_READ("report:read"),

    // Workspace management
    WORKSPACE_OWNER("workspace:owner"),
    WORKSPACE_READ("workspace:read"),
    WORKSPACE_WRITE("workspace:write");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
