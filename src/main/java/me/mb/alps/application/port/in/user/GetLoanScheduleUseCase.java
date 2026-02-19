package me.mb.alps.application.port.in.user;

import me.mb.alps.application.service.loan.LoanScheduleCalculator;
import me.mb.alps.domain.enums.UserRole;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * CUSTOMER xem lịch trả nợ (payment schedule) cho loan application của mình.
 * Thông tin caller (userId + role) được truyền từ controller.
 */
public interface GetLoanScheduleUseCase {
    List<LoanScheduleCalculator.PaymentScheduleItem> getSchedule(
            UUID applicationId,
            LocalDate firstPaymentDate,
            UUID userId,
            UserRole role
    );
}
