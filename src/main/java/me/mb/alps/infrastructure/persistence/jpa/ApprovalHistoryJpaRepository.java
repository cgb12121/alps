package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.ApprovalHistory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalHistoryJpaRepository extends JpaRepository<@NonNull ApprovalHistory, @NonNull UUID> {
    List<ApprovalHistory> findByLoanApplication_IdOrderByCreatedAtDesc(@NonNull UUID loanApplicationId);
}
