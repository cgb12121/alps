package me.mb.alps.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.PaymentReminderNotificationPort;
import me.mb.alps.domain.entity.LoanApplication;
import me.mb.alps.domain.entity.RepaymentSchedule;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReminderService {

    private final LoanApplicationPersistencePort loanPort;
    private final List<PaymentReminderNotificationPort> reminderPorts;

    public void sendReminder(RepaymentSchedule schedule) {
        LoanApplication loan = loanPort.findById(schedule.getLoanApplication().getId())
                .orElseThrow(() -> new IllegalStateException("Loan not found: " + schedule.getLoanApplication().getId()));

        if (reminderPorts == null || reminderPorts.isEmpty()) {
            log.debug("No PaymentReminderNotificationPort adapters configured, skipping reminder for schedule {}", schedule.getId());
            return;
        }

        for (PaymentReminderNotificationPort port : reminderPorts) {
            try {
                port.sendReminder(
                        loan.getId(),
                        loan.getCustomer().getId(),
                        loan.getCustomer().getEmail(),
                        loan.getCustomer().getFullName(),
                        schedule.getInstallmentNumber(),
                        schedule.getDueDate(),
                        schedule.getTotalAmount()
                );
                log.debug("Sent reminder for schedule {} via {}", schedule.getId(), port.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Failed to send reminder via {}: {}", port.getClass().getSimpleName(), e.getMessage());
                // Continue with other adapters
            }
        }
    }
}
