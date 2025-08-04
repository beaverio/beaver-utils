package com.beaver.auth.permissions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {
    DENY_ALL("deny:all"),

    USER_READ("user:read"),
    USER_WRITE("user:write"),

    TRANSACTION_READ("transaction:read"),
    TRANSACTION_WRITE("transaction:write"),
    BUDGET_READ("budget:read"),
    BUDGET_WRITE("budget:write"),
    REPORT_READ("report:read"),

    WORKSPACE_OWNER("workspace:owner"),
    WORKSPACE_READ("workspace:read"),
    WORKSPACE_WRITE("workspace:write");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
