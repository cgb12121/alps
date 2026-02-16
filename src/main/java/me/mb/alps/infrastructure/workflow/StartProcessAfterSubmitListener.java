package me.mb.alps.infrastructure.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.event.LoanApplicationSubmittedEvent;
import me.mb.alps.application.port.out.LoanApplicationPersistencePort;
import me.mb.alps.application.port.out.StartProcessPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Sau khi submit loan (transaction commit), chạy async để start Camunda process và cập nhật processInstanceKey.
 * User nhận response ngay với status SUBMITTED; không phải đợi Zeebe start process (đặc biệt khi deploy lần đầu chậm).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "camunda.client.enabled", havingValue = "true")
public class StartProcessAfterSubmitListener {

    private final StartProcessPort startProcessPort;
    private final LoanApplicationPersistencePort persistencePort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLoanApplicationSubmitted(LoanApplicationSubmittedEvent event) {
        var applicationId = event.applicationId();
        try {
            long processKey = startProcessPort.startProcess(
                    "loan-approval",
                    Map.of("applicationId", applicationId.toString())
            );
            persistencePort.findById(applicationId).ifPresent(app -> {
                app.setProcessInstanceKey(processKey);
                persistencePort.save(app);
                log.debug("Started process for application {} -> processInstanceKey={}", applicationId, processKey);
            });
        } catch (Exception e) {
            log.error("Failed to start process for application {}: {}", applicationId, e.getMessage());
        }
    }
}
