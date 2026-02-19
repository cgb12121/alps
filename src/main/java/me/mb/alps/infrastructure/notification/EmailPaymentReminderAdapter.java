package me.mb.alps.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.PaymentReminderNotificationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnBooleanProperty(name = "alps.notification.mail.enabled")
public class EmailPaymentReminderAdapter implements PaymentReminderNotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendReminder(UUID loanApplicationId, UUID customerId, String customerEmail, String customerName,
                             int installmentNumber, LocalDate dueDate, BigDecimal totalAmount) {
        if (customerEmail == null || customerEmail.isBlank()) {
            return;
        }
        String subject = "Nhắc nhở thanh toán kỳ %d - Khoản vay %s".formatted(installmentNumber, loanApplicationId);
        String body = """
                Kính gửi %s,
                Nhắc nhở: Kỳ %d của khoản vay (mã %s) sẽ đến hạn vào ngày %s.
                Số tiền cần thanh toán: %s VNĐ.
                "Vui lòng thanh toán đúng hạn để tránh phí phạt.
                "Trân trọng.",
                """.formatted(
                        customerName != null ? customerName : "Quý khách",
                        installmentNumber,
                        loanApplicationId,
                        dueDate,
                        totalAmount
                );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customerEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
