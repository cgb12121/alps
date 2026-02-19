package me.mb.alps.infrastructure.workflow;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.command.DeployResourceCommandStep1;
import io.camunda.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import lombok.extern.slf4j.Slf4j;
import me.mb.alps.application.port.out.StartProcessPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements StartProcessPort using Camunda Java Client. Deploys BPMN on first use; starts process with variables.
 */
@Slf4j
@Component
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class ZeebeStartProcessAdapter implements StartProcessPort {

    private static final String[] BPMN_RESOURCES = {
            "bpmn/loan-approval.bpmn",
            "bpmn/overdue-penalty.bpmn",
            "bpmn/credit-score-reward.bpmn"
    };

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

    /**
     * Init the camunda app when the spring application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void deployIfEnabled() {
        if (!deployOnStartup) {
            return;
        }
        try {
            ensureDeployed();
            log.info("BPMN deployed successfully.");
        } catch (Exception e) {
            log.warn("Deploy BPMN on startup failed (Zeebe có thể chưa chạy hoặc dùng REST gây lỗi). Sẽ thử lại khi submit loan.", e);
        }
    }

    private void ensureDeployed() {
        if (deployed) return;
        deployLock.lock();
        try {
            if (deployed) return;

            // Create command
            DeployResourceCommandStep1 commandStep1 = camundaClient.newDeployResourceCommand();
            
            // Variable that holds Step2 (that have sent method)
            DeployResourceCommandStep2 commandStep2 = null;

            for (String resource : BPMN_RESOURCES) {
                // If this call is the first time, call from Step1 else continue with Step2
                if (commandStep2 == null) {
                    commandStep2 = commandStep1.addResourceFromClasspath(resource);
                } else {
                    commandStep2 = commandStep2.addResourceFromClasspath(resource);
                }
                log.debug("Added BPMN resource: {}", resource);
            }

            // Send command (only send when have at least 1 resource
            if (commandStep2 != null) {
                commandStep2.send().join();
                deployed = true;
                log.info("Deployed BPMN resources successfully");
            } else {
                throw new IllegalStateException("No BPMN resources found to deploy");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to deploy BPMN", e);
        } finally {
            deployLock.unlock();
        }
    }
}
