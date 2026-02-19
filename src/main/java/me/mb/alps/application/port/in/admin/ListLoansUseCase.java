package me.mb.alps.application.port.in.admin;

import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.domain.enums.LoanStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Use case: List loans với filters (status, approverId, date range).
 */
public interface ListLoansUseCase {
    List<LoanApplicationSummaryResponse> list(ListLoansCommand command);

    record ListLoansCommand(
            LoanStatus status,           // null = all statuses
            UUID reviewedById,           // null = all approvers
            LocalDateTime fromDate,      // null = no lower bound
            LocalDateTime toDate         // null = no upper bound
    ) {}
}
