package com.beaver.auth.jwt;

import lombok.Builder;

@Builder
public class RefreshToken {
    String userId;
    String workspaceId;
}
