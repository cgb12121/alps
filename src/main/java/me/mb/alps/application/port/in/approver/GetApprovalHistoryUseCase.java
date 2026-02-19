package me.mb.alps.application.port.in.approver;

import me.mb.alps.application.dto.response.ApprovalHistoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Use case: Lấy lịch sử duyệt của một loan application.
 */
public interface GetApprovalHistoryUseCase {
    List<ApprovalHistoryResponse> getHistory(UUID loanApplicationId);
}
