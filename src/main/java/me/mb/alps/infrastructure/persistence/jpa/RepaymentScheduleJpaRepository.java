package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RepaymentScheduleJpaRepository extends JpaRepository<@NonNull RepaymentSchedule, @NonNull UUID> {
    List<RepaymentSchedule> findByLoanApplication_IdOrderByInstallmentNumber(@NonNull UUID loanApplicationId);
    List<RepaymentSchedule> findByDueDateBetweenAndStatus(@NonNull LocalDate startDate, @NonNull LocalDate endDate, @NonNull PaymentStatus status);
}
