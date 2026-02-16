package me.mb.alps.application.port.out;

import me.mb.alps.domain.enums.LoanStatus;

import java.util.UUID;

/**
 * Outbound port: gửi thông báo khi có quyết định duyệt vay (email/Slack).
 */
public interface NotificationPort {
    void notifyDecision(UUID applicationId, LoanStatus decision, String customerEmail, String customerName);
}
