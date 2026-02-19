package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.user.GetLoanScheduleUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.service.loan.LoanScheduleCalculator;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetLoanScheduleService implements GetLoanScheduleUseCase {

    private final LoadUserPort loadUserPort;
    private final LoanApplicationPersistencePort persistencePort;
    private final LoanScheduleCalculator calculator;

    @Override
    public List<LoanScheduleCalculator.PaymentScheduleItem> getSchedule(
            UUID applicationId,
            LocalDate firstPaymentDate,
            UUID userId,
            UserRole role
    ) {
        if (role != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only CUSTOMER can view loan schedule");
        }
        LoanApplication application = persistencePort.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("LoanApplication", applicationId));
        // Verify: CUSTOMER chỉ xem được loan của mình (theo customerId)
        var user = loadUserPort.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getCustomerId() == null || !user.getCustomerId().equals(application.getCustomer().getId())) {
            throw new IllegalStateException("Loan application does not belong to current user");
        }
        if (application.getInterestRateAnnual() == null) {
            throw new IllegalStateException("Loan application not yet scored (no interest rate)");
        }
        return calculator.calculateSchedule(application, firstPaymentDate);
    }
}
