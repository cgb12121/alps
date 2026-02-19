package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterRequest(
        @NotBlank @Size(min = 1, max = 128) String username,
        @NotBlank @Size(min = 4, max = 128) String password,
        String displayName,
        String email,
        UUID customerId
) {}
