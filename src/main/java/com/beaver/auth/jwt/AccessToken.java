package com.beaver.auth.jwt;

import lombok.Builder;

@Builder
public record AccessToken(
        String userId,
        String email,
        String name,
        String workspaceId,
        String role
) {
}