package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import me.mb.alps.infrastructure.persistence.jpa.RepaymentScheduleJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RepaymentSchedulePersistenceAdapter implements RepaymentSchedulePersistencePort {

    private final RepaymentScheduleJpaRepository jpaRepository;

    @Override
    public RepaymentSchedule save(RepaymentSchedule schedule) {
        return jpaRepository.save(schedule);
    }

    @Override
    public List<RepaymentSchedule> saveAll(List<RepaymentSchedule> schedules) {
        return jpaRepository.saveAll(schedules);
    }

    @Override
    public List<RepaymentSchedule> findByLoanApplicationIdOrderByInstallmentNumber(UUID loanApplicationId) {
        return jpaRepository.findByLoanApplication_IdOrderByInstallmentNumber(loanApplicationId);
    }

    @Override
    public List<RepaymentSchedule> findByDueDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, PaymentStatus status) {
        return jpaRepository.findByDueDateBetweenAndStatus(startDate, endDate, status);
    }

    @Override
    public Optional<RepaymentSchedule> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
