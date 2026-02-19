package me.mb.alps.infrastructure.workflow;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.in.automation.ScoreLoanUseCase;
import me.mb.alps.domain.enums.LoanStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Camunda job worker: handles "score-loan" tasks via @JobWorker (annotation-based).
 * Framework đăng ký worker và gọi method này khi có job; không cần registerWorker/close thủ công.
 * <p>Class không được reference trực tiếp trong code; Camunda starter quét {@link JobWorker} và tự đăng ký worker.
 * Cảnh báo "never used" của IDE là false positive.
 */
@SuppressWarnings("unused")
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class ScoreLoanJobWorker {

    private static final String VAR_APPLICATION_ID = "applicationId";

    private final ScoreLoanUseCase scoreLoanUseCase;

    @JobWorker(type = "score-loan")
    public void scoreLoan(JobClient jobClient, ActivatedJob job) {
        String applicationIdStr = (String) job.getVariablesAsMap().get(VAR_APPLICATION_ID);
        if (applicationIdStr == null) {
            jobClient.newFailCommand(job.getKey())
                    .retries(0)
                    .errorMessage("Variable " + VAR_APPLICATION_ID + " required")
                    .send();
            return;
        }
        try {
            UUID applicationId = UUID.fromString(applicationIdStr);
            log.info("Processing score-loan job for applicationId={}", applicationId);
            LoanStatus decision = scoreLoanUseCase.score(applicationId);
            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of("decision", decision.name()))
                    .send().join();
            log.info("Score-loan job completed for applicationId={}, decision={}", applicationId, decision);
        } catch (Exception e) {
            log.error("Score loan job failed: {}", e.getMessage());
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }
}
