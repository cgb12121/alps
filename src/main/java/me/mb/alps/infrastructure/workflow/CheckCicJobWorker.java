package me.mb.alps.infrastructure.workflow;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.CICCheckPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Worker: check CIC (giả lập). Trả biến cicClean vào process để bước sau có thể dùng.
 * <p>Class không được reference trực tiếp trong code; Camunda starter quét {@link JobWorker} và tự đăng ký worker.
 * Cảnh báo "never used" của IDE là false positive.
 */
@SuppressWarnings("unused")
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class CheckCicJobWorker {

    private static final String VAR_APPLICATION_ID = "applicationId";

    private final CICCheckPort cicCheckPort;

    @JobWorker(type = "check-cic")
    public void checkCic(JobClient jobClient, ActivatedJob job) {
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
            CICCheckPort.CICResult result = cicCheckPort.check(applicationId);
            jobClient.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "cicClean", result.clean(),
                            "cicReportId", result.reportId()
                    ))
                    .send()
                    .join();
        } catch (Exception e) {
            jobClient.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send();
        }
    }
}
