package me.mb.alps.application.port.out;

import me.mb.alps.domain.entity.LoanProduct;

/**
 * Outbound port: persist a loan product. Returns the saved entity (with id set).
 */
public interface SaveLoanProductPort {

    LoanProduct save(LoanProduct product);
}
