package me.mb.alps.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.NotificationPort;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gửi thông báo quyết định (APPROVED/REJECTED) qua email.
 * Bật bằng alps.notification.mail.enabled=true và cấu hình spring.mail.* (host, port, username, password).
 * {@link JavaMailSender} do Spring Boot Mail auto-config tạo khi có spring.mail.*.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "alps.notification.mail.enabled")
@ConditionalOnBean(JavaMailSender.class)
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;

    @Override
    public void notifyDecision(UUID applicationId, LoanStatus decision, String customerEmail, String customerName) {
        if (customerEmail == null || customerEmail.isBlank()) {
            return;
        }
        String subject = decision == LoanStatus.APPROVED
                ? "Kết quả hồ sơ vay vốn – Chấp thuận"
                : "Kết quả hồ sơ vay vốn – Từ chối";
        String body = String.format(
                "Kính gửi %s,\n\nHồ sơ vay vốn (mã %s) đã được xử lý: %s.\n\nTrân trọng.",
                customerName != null ? customerName : "Quý khách",
                applicationId,
                decision == LoanStatus.APPROVED ? "Chấp thuận" : "Từ chối"
        );
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customerEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
