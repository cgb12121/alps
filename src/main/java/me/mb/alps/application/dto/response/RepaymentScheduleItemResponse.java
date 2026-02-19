package me.mb.alps.application.dto.response;

import me.mb.alps.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO response cho một kỳ trong RepaymentSchedule (đã lưu trong DB).
 */
public record RepaymentScheduleItemResponse(
        int installmentNumber,
        LocalDate dueDate,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        PaymentStatus status,
        LocalDate paidDate
) {}
