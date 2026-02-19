package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.dto.response.RepaymentScheduleItemResponse;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.user.GetRepaymentScheduleUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRepaymentScheduleService implements GetRepaymentScheduleUseCase {

    private final LoadUserPort loadUserPort;
    private final LoanApplicationPersistencePort loanPort;
    private final RepaymentSchedulePersistencePort schedulePort;

    @Override
    public List<RepaymentScheduleItemResponse> getSchedule(UUID applicationId, UUID userId, UserRole role) {
        if (role != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only CUSTOMER can view repayment schedule");
        }
        LoanApplication loan = loanPort.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("LoanApplication", applicationId));
        // Verify ownership
        var user = loadUserPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getCustomerId() == null || !user.getCustomerId().equals(loan.getCustomer().getId())) {
            throw new IllegalStateException("Loan application does not belong to current user");
        }
        return schedulePort.findByLoanApplicationIdOrderByInstallmentNumber(applicationId).stream()
                .map(s -> new RepaymentScheduleItemResponse(
                        s.getInstallmentNumber(),
                        s.getDueDate(),
                        s.getPrincipalAmount(),
                        s.getInterestAmount(),
                        s.getTotalAmount(),
                        s.getPaidAmount(),
                        s.getStatus(),
                        s.getPaidDate()
                ))
                .toList();
    }
}
