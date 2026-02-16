package me.mb.alps.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.SaveLoanProductPort;
import me.mb.alps.domain.entity.LoanProduct;
import me.mb.alps.infrastructure.persistence.jpa.LoanProductJpaRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveLoanProductAdapter implements SaveLoanProductPort {

    private final LoanProductJpaRepository jpaRepository;

    @Override
    public LoanProduct save(LoanProduct product) {
        return jpaRepository.save(product);
    }
}
