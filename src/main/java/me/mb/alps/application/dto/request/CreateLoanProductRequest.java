package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for creating a loan product. ID is generated (UUIDv7).
 */
public record CreateLoanProductRequest(
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "name is required") String name,
        @NotNull @DecimalMin("0") BigDecimal minAmount,
        @NotNull @DecimalMin("0") BigDecimal maxAmount,
        @Positive int minTermMonths,
        @Positive int maxTermMonths,
        BigDecimal interestRateAnnual,
        Boolean active
) {}
