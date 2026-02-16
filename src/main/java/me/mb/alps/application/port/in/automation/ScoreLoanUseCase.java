package me.mb.alps.application.port.in.automation;

import me.mb.alps.domain.enums.LoanStatus;

import java.util.UUID;

/**
 * Inbound port: score a loan application (Drools) and persist result. Called by Camunda worker.
 * Returns the new status so the process can branch (Approved / Rejected / Review required).
 */
public interface ScoreLoanUseCase {
    LoanStatus score(UUID applicationId);
}
