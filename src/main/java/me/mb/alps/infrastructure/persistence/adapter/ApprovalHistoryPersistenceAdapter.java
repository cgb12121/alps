package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.ApprovalHistoryPersistencePort;
import me.mb.alps.domain.entity.ApprovalHistory;
import me.mb.alps.infrastructure.persistence.jpa.ApprovalHistoryJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApprovalHistoryPersistenceAdapter implements ApprovalHistoryPersistencePort {

    private final ApprovalHistoryJpaRepository jpaRepository;

    @Override
    public ApprovalHistory save(ApprovalHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public List<ApprovalHistory> findByLoanApplicationIdOrderByCreatedAtDesc(UUID loanApplicationId) {
        return jpaRepository.findByLoanApplication_IdOrderByCreatedAtDesc(loanApplicationId);
    }
}
