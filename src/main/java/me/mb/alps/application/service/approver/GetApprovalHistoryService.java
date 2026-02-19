package me.mb.alps.application.service.approver;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.ApprovalHistoryResponse;
import me.mb.alps.application.port.in.approver.GetApprovalHistoryUseCase;
import me.mb.alps.application.port.out.ApprovalHistoryPersistencePort;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.domain.entity.ApprovalHistory;
import me.mb.alps.domain.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetApprovalHistoryService implements GetApprovalHistoryUseCase {

    private final ApprovalHistoryPersistencePort historyPort;
    private final LoadUserPort loadUserPort;

    @Override
    public List<ApprovalHistoryResponse> getHistory(UUID loanApplicationId) {
        List<ApprovalHistory> histories = historyPort.findByLoanApplicationIdOrderByCreatedAtDesc(loanApplicationId);

        return histories.stream()
                .map(this::toResponse)
                .toList();
    }

    private ApprovalHistoryResponse toResponse(ApprovalHistory history) {
        String username = loadUserPort.findById(history.getApprovedBy().getId())
                .map(User::getUsername)
                .orElse("Unknown");

        return new ApprovalHistoryResponse(
                history.getId(),
                history.getLoanApplication().getId(),
                history.getApprovedBy().getId(),
                username,
                history.getOldStatus(),
                history.getNewStatus(),
                history.getComment(),
                history.getCreatedAt()
        );
    }
}
