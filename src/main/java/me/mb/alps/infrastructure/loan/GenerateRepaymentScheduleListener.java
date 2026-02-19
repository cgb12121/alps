package me.mb.alps.infrastructure.loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.event.LoanApplicationDecidedEvent;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.service.loan.RepaymentScheduleGeneratorService;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

/**
 * Khi loan được APPROVED (có interestRate), tự động generate RepaymentSchedule (pre-computed).
 * Chạy async sau khi transaction commit để không block response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "alps.loan.auto-generate-schedule")
public class GenerateRepaymentScheduleListener {

    private final LoanApplicationPersistencePort loanPort;
    private final RepaymentScheduleGeneratorService scheduleGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanApproved(LoanApplicationDecidedEvent event) {
        LoanApplication loan = loanPort.findById(event.applicationId())
                .orElse(null);
        if (loan == null) {
            return;
        }
        // Chỉ generate khi APPROVED và đã có interestRate (đã được score)
        if (loan.getStatus() == LoanStatus.APPROVED && loan.getInterestRateAnnual() != null) {
            try {
                // Disbursement date = ngày hiện tại (hoặc có thể lấy từ event/field khác)
                LocalDate disbursementDate = LocalDate.now();
                scheduleGenerator.generateAndSave(loan, disbursementDate);
                log.info("Generated repayment schedule for loan {}", event.applicationId());
            } catch (Exception e) {
                log.error("Failed to generate repayment schedule for loan {}: {}", event.applicationId(), e.getMessage());
            }
        }
    }
}
