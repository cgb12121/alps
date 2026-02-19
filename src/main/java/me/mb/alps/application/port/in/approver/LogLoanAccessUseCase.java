package me.mb.alps.application.port.in.approver;

import java.util.UUID;

/**
 * Use case: Ghi log khi approver truy cập vào loan của customer (audit trail).
 */
public interface LogLoanAccessUseCase {
    void logAccess(UUID loanApplicationId, UUID approverId, String action);
}
