package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.mb.alps.domain.enums.UserRole;

public record CreateAccountRequest(
        @NotBlank @Size(min = 1, max = 128) String username,
        @NotBlank @Size(min = 4, max = 128) String password,
        String displayName,
        String email,
        @NotNull UserRole role
) {}
