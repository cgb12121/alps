package me.mb.alps.application.dto.response;

import me.mb.alps.domain.enums.LoanStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO cho lịch sử duyệt loan.
 */
public record ApprovalHistoryResponse(
        UUID id,
        UUID loanApplicationId,
        UUID approvedById,
        String approvedByUsername,
        LoanStatus oldStatus,
        LoanStatus newStatus,
        String comment,
        LocalDateTime createdAt
) {}
