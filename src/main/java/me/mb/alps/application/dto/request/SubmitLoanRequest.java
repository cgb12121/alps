package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * HTTP request body for submitting a loan application. Validated at web layer; controller maps to use-case command.
 */
public record SubmitLoanRequest(
        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "productId is required")
        UUID productId,

        @NotNull
        @DecimalMin(value = "0.01", message = "amount must be positive")
        BigDecimal amount,

        @Positive(message = "termMonths must be positive")
        int termMonths,

        UUID submittedByUserId
) {}
