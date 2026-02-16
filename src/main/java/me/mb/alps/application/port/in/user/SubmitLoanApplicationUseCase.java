package me.mb.alps.application.port.in.user;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Inbound port: use-case "submit loan application". Application layer exposes this; web adapter calls it.
 */
public interface SubmitLoanApplicationUseCase {

    /**
     * @return id of the created loan application
     */
    UUID submit(SubmitLoanCommand command);

    record SubmitLoanCommand(
            UUID customerId,
            UUID productId,
            BigDecimal amount,
            int termMonths,
            UUID submittedByUserId
    ) {}
}
