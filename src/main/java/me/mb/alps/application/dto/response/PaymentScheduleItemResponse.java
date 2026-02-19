package me.mb.alps.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO response cho một kỳ trả nợ trong lịch.
 */
public record PaymentScheduleItemResponse(
        int period,
        LocalDate paymentDate,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalPayment,
        BigDecimal remainingPrincipal
) {}
