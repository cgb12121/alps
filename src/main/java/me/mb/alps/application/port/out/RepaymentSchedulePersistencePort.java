package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persist repayment schedule (pre-computed khi giải ngân).
 */
public interface RepaymentSchedulePersistencePort {
    RepaymentSchedule save(RepaymentSchedule schedule);
    List<RepaymentSchedule> saveAll(List<RepaymentSchedule> schedules);
    List<RepaymentSchedule> findByLoanApplicationIdOrderByInstallmentNumber(UUID loanApplicationId);
    List<RepaymentSchedule> findByDueDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, PaymentStatus status);
    Optional<RepaymentSchedule> findById(UUID id);
}
