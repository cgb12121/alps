package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a user. ID is generated (UUIDv7).
 */
public record CreateUserRequest(
        @NotBlank(message = "username is required") String username,
        String displayName,
        String email,
        Boolean active
) {}
