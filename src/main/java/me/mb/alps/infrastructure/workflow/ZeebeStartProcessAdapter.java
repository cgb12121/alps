package me.mb.alps.infrastructure.workflow;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.StartProcessPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements StartProcessPort using Camunda Java Client. Deploys BPMN on first use; starts process with variables.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "camunda.client.enabled", havingValue = "true")
public class ZeebeStartProcessAdapter implements StartProcessPort {

    private static final String BPMN_RESOURCE = "bpmn/loan-approval.bpmn";

    private final CamundaClient camundaClient;
    private final boolean deployOnStartup;
    private volatile boolean deployed;
    private final ReentrantLock deployLock = new ReentrantLock();

    @Autowired
    public ZeebeStartProcessAdapter(CamundaClient camundaClient, @Value("${camunda.workflow.deploy-on-startup}") boolean deployOnStartup) {
        this.camundaClient = camundaClient;
        this.deployOnStartup = deployOnStartup;
    }

    @Override
    public long startProcess(String processId, Map<String, Object> variables) {
        ensureDeployed();
        ProcessInstanceEvent event = camundaClient.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();
        return event.getProcessInstanceKey();
    }

    private void ensureDeployed() {
        if (deployed) {
            return;
        }
        deployLock.lock();
        try {
            if (deployed) {
                return;
            }
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(BPMN_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("BPMN not found: " + BPMN_RESOURCE);
                }
                camundaClient.newDeployResourceCommand()
                        .addResourceStream(in, "loan-approval.bpmn")
                        .send()
                        .join();
                deployed = true;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to deploy BPMN", e);
            }
        } finally {
            deployLock.unlock();
        }
    }

    /** Deploy BPMN khi app ready; nếu thất bại (Zeebe chưa chạy/sai phiên bản) chỉ log, không crash app. */
    @EventListener(ApplicationReadyEvent.class)
    public void deployIfEnabled() {
        if (!deployOnStartup) {
            return;
        }
        try {
            ensureDeployed();
            log.info("BPMN deployed successfully.");
        } catch (Exception e) {
            log.warn("Deploy BPMN on startup failed (Zeebe có thể chưa chạy hoặc dùng REST gây lỗi). Sẽ thử lại khi submit loan. Lỗi: {}", e.getMessage());
        }
    }
}
