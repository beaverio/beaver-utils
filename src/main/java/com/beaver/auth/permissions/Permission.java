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
    WORK_READ("work:read"),
    WORK_WRITE("work:write");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
