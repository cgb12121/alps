package me.mb.alps.application.service.approver;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.approver.ListPendingApprovalsUseCase;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListPendingApprovalsService implements ListPendingApprovalsUseCase {

    private final LoanApplicationPersistencePort persistencePort;

    @Override
    public List<LoanApplicationSummaryResponse> list() {
        return persistencePort.findByStatus(LoanStatus.REVIEW_REQUIRED).stream()
                .map(this::toSummary)
                .toList();
    }

    private LoanApplicationSummaryResponse toSummary(LoanApplication a) {
        return new LoanApplicationSummaryResponse(
                a.getId(),
                a.getCustomer().getId(),
                a.getProduct().getId(),
                a.getAmount(),
                a.getTermMonths(),
                a.getStatus(),
                a.getCreatedAt()
        );
    }
}
