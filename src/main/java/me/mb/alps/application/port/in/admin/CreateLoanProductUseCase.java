package me.mb.alps.application.port.in.admin;

import java.math.BigDecimal;
import java.util.UUID;

public interface CreateLoanProductUseCase {

    UUID create(CreateLoanProductCommand command);

    record CreateLoanProductCommand(
            String code,
            String name,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int minTermMonths,
            int maxTermMonths,
            BigDecimal interestRateAnnual,
            boolean active
    ) {}
}
