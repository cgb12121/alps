package me.mb.alps.application.service.user;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.exception.NotFoundException;
import me.mb.alps.application.port.in.user.MakePaymentUseCase;
import me.mb.alps.application.port.out.LoadUserPort;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.RepaymentSchedulePersistencePort;
import me.mb.alps.application.port.out.StartProcessPort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.RepaymentSchedule;
import me.mb.alps.domain.enums.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service: Customer trả loan (có thể trả trước hạn hoặc trả một phần).
 */
@Service
@RequiredArgsConstructor
public class MakePaymentService implements MakePaymentUseCase {

    private final RepaymentSchedulePersistencePort schedulePort;
    private final LoanApplicationPersistencePort loanPort;
    private final LoadUserPort loadUserPort;
    private final StartProcessPort startProcessPort;

    @Override
    @Transactional
    public PaymentResult makePayment(MakePaymentCommand command) {
        LoanApplication loan = loanPort.findById(command.loanApplicationId())
                .orElseThrow(() -> new NotFoundException("LoanApplication", command.loanApplicationId()));

        // Verify ownership: user.customerId phải match với loan.customer.id
        var user = loadUserPort.findById(command.userId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (user.getCustomerId() == null || !user.getCustomerId().equals(loan.getCustomer().getId())) {
            throw new IllegalStateException("You can only pay for your own loans");
        }

        BigDecimal amount = command.amount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (command.repaymentScheduleId() != null) {
            // Trả một schedule cụ thể
            RepaymentSchedule schedule = schedulePort.findById(command.repaymentScheduleId())
                    .orElseThrow(() -> new NotFoundException("RepaymentSchedule", command.repaymentScheduleId()));

            if (!schedule.getLoanApplication().getId().equals(loan.getId())) {
                throw new IllegalStateException("Schedule does not belong to this loan");
            }

            return processPayment(schedule, amount);
        } else {
            // Trả tất cả schedules còn PENDING (theo thứ tự)
            List<RepaymentSchedule> pendingSchedules = schedulePort
                    .findByLoanApplicationIdOrderByInstallmentNumber(loan.getId())
                    .stream()
                    .filter(s -> s.getStatus() == PaymentStatus.PENDING)
                    .toList();

            if (pendingSchedules.isEmpty()) {
                return new PaymentResult(false, "No pending schedules to pay", null);
            }

            BigDecimal remainingAmount = amount;
            UUID firstPaymentId = null;

            for (RepaymentSchedule schedule : pendingSchedules) {
                if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal scheduleRemaining = schedule.getRemainingAmount();
                BigDecimal paymentForThisSchedule = remainingAmount.min(scheduleRemaining);

                if (firstPaymentId == null) {
                    firstPaymentId = schedule.getId();
                }

                processPayment(schedule, paymentForThisSchedule);
                remainingAmount = remainingAmount.subtract(paymentForThisSchedule);
            }

            return new PaymentResult(true, "Payment processed", firstPaymentId);
        }
    }

    private PaymentResult processPayment(RepaymentSchedule schedule, BigDecimal amount) {
        LocalDate today = LocalDate.now();
        schedule.recordPayment(amount, today);
        schedulePort.save(schedule);

        boolean isFullyPaid = schedule.getStatus() == PaymentStatus.PAID;
        boolean isOnTime = !today.isAfter(schedule.getDueDate());

        // Trigger workflow cộng điểm nếu trả đúng hạn và đã trả đủ
        if (isFullyPaid && isOnTime && startProcessPort != null) {
            try {
                startProcessPort.startProcess(
                        "credit-score-reward",
                        Map.of("loanApplicationId", schedule.getLoanApplication().getId().toString())
                );
            } catch (Exception e) {
                // Log nhưng không fail payment nếu workflow không chạy được
                // (Camunda có thể không chạy trong môi trường dev)
            }
        }

        return new PaymentResult(true, "Payment processed successfully", schedule.getId());
    }
}
