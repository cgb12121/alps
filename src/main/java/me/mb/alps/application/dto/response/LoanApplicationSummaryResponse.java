package me.mb.alps.application.dto.response;

import me.mb.alps.domain.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary of a loan application for list/detail API. Application service or adapter maps from domain to this DTO.
 */
public record LoanApplicationSummaryResponse(
        UUID id,
        UUID customerId,
        UUID productId,
        BigDecimal amount,
        int termMonths,
        LoanStatus status,
        LocalDateTime createdAt
) {}
