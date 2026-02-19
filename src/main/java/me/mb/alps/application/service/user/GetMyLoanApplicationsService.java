package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.LoanApplicationSummaryResponse;
import me.mb.alps.application.port.in.user.GetMyLoanApplicationsUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyLoanApplicationsService implements GetMyLoanApplicationsUseCase {

    private final LoadUserPort loadUserPort;
    private final LoanApplicationPersistencePort persistencePort;

    @Override
    public List<LoanApplicationSummaryResponse> listMyLoans(UUID userId, UserRole role) {
        if (role != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only CUSTOMER can list own loans");
        }
        var user = loadUserPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getCustomerId() == null) {
            return List.of();
        }
        return persistencePort.findByCustomerIdOrderByCreatedAtDesc(user.getCustomerId()).stream()
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
