package me.mb.alps.application.port.in.user;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case: Customer trả loan (có thể trả trước hạn hoặc trả một phần).
 * Thông tin caller (userId) được truyền qua command.
 */
public interface MakePaymentUseCase {
    PaymentResult makePayment(MakePaymentCommand command);

    record MakePaymentCommand(
            UUID loanApplicationId,
            UUID repaymentScheduleId,  // null = trả tất cả schedules còn PENDING
            BigDecimal amount,         // Số tiền trả
            UUID userId                // Caller userId
    ) {}

    record PaymentResult(
            boolean success,
            String message,
            UUID paymentId
    ) {}
}
