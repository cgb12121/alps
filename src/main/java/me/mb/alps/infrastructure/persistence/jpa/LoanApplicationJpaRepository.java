package me.mb.alps.infrastructure.persistence.jpa;

import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository. Used only by persistence adapter; application layer uses port interface.
 */
@Repository
public interface LoanApplicationJpaRepository extends JpaRepository<@NonNull LoanApplication, @NonNull UUID> {
    List<LoanApplication> findByStatusOrderByCreatedAtDesc(@NonNull LoanStatus status);
}
