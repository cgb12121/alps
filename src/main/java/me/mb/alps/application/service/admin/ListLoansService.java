package me.mb.alps.application.service.admin;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.admin.ListLoansUseCase;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListLoansService implements ListLoansUseCase {

    private final LoanApplicationPersistencePort persistencePort;

    @Override
    public List<LoanApplicationSummaryResponse> list(ListLoansCommand command) {
        List<LoanApplication> loans = persistencePort.findAllWithFilters(
                command.status(),
                command.reviewedById(),
                command.fromDate(),
                command.toDate()
        );

        return loans.stream()
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
