package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persist loan application. Implemented by infrastructure.persistence adapter.
 */
public interface LoanApplicationPersistencePort {
    LoanApplication save(LoanApplication application);
    Optional<LoanApplication> findById(UUID id);
    List<LoanApplication> findByStatus(LoanStatus status);
}
