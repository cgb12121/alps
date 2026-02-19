package me.mb.alps.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request body for making a payment.
 */
public record MakePaymentRequest(
        @NotNull UUID loanApplicationId,
        UUID repaymentScheduleId,  // null = trả tất cả schedules còn PENDING
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {}
