package com.beaver.auth.jwt;

import lombok.Builder;

import java.util.Set;

@Builder
public record AccessToken(
        String userId,
        String email,
        String name,
        String workspaceId,
        Set<String> permissions
) {
}