package me.mb.alps.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Outbound port: gửi nhắc nợ (payment reminder) cho khách hàng.
 */
public interface PaymentReminderNotificationPort {
    void sendReminder(UUID loanApplicationId, UUID customerId, String customerEmail, String customerName,
                      int installmentNumber, LocalDate dueDate, BigDecimal totalAmount);
}
