package me.mb.alps.application.port.in.approver;

import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;

import java.util.List;

/**
 * Inbound port: list loan applications waiting for manual approval (status REVIEW_REQUIRED).
 */
public interface ListPendingApprovalsUseCase {
    List<LoanApplicationSummaryResponse> list();
}
