package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.LoanProduct;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: load loan product by id. Implemented by infrastructure.persistence.
 */
public interface LoadLoanProductPort {
    Optional<LoanProduct> findById(UUID id);
}
