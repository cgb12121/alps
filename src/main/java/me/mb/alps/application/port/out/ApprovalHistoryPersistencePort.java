package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.ApprovalHistory;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port: persist approval history.
 */
public interface ApprovalHistoryPersistencePort {
    ApprovalHistory save(ApprovalHistory history);
    List<ApprovalHistory> findByLoanApplicationIdOrderByCreatedAtDesc(UUID loanApplicationId);
}
