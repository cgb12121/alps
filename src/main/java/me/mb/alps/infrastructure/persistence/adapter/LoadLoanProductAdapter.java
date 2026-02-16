package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.LoadLoanProductPort;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.infrastructure.persistence.jpa.LoanProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadLoanProductAdapter implements LoadLoanProductPort {

    private final LoanProductJpaRepository jpaRepository;

    @Override
    public Optional<LoanProduct> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
