package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.enums.LoanStatus;
import me.mb.alps.infrastructure.persistence.jpa.LoanApplicationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements application port using Spring Data JPA. Application layer does not depend on this class.
 */
@Component
@RequiredArgsConstructor
public class LoanApplicationPersistenceAdapter implements LoanApplicationPersistencePort {

    private final LoanApplicationJpaRepository jpaRepository;

    @Override
    public LoanApplication save(LoanApplication application) {
        return jpaRepository.save(application);
    }

    @Override
    public Optional<LoanApplication> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<LoanApplication> findByStatus(LoanStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
    }
}
