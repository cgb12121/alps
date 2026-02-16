package me.mb.alps.infrastructure.notification;

import me.mb.alps.application.event.LoanApplicationDecidedEvent;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.NotificationPort;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Khi có quyết định APPROVED/REJECTED (tự động hoặc duyệt tay), gửi thông báo (email/Slack).
 */
@Component
public class LoanApplicationDecidedNotificationListener {

    private final LoanApplicationPersistencePort persistencePort;
    private final List<NotificationPort> notificationPorts;

    public LoanApplicationDecidedNotificationListener(
            LoanApplicationPersistencePort persistencePort,
            @Autowired(required = false) List<NotificationPort> notificationPorts) {
        this.persistencePort = persistencePort;
        this.notificationPorts = notificationPorts != null ? notificationPorts : Collections.emptyList();
    }

    @Async
    @EventListener
    public void onLoanApplicationDecided(LoanApplicationDecidedEvent event) {
        if (event.newStatus() != LoanStatus.APPROVED && event.newStatus() != LoanStatus.REJECTED) {
            return;
        }
        persistencePort.findById(event.applicationId()).ifPresent(app -> {
            String email = app.getCustomer() != null ? app.getCustomer().getEmail() : null;
            String name = app.getCustomer() != null ? app.getCustomer().getFullName() : null;
            for (NotificationPort port : notificationPorts) {
                try {
                    port.notifyDecision(event.applicationId(), event.newStatus(), email, name);
                } catch (Exception e) {
                    // log and continue with other channels
                }
            }
        });
    }
}
