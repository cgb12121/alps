package me.mb.alps.infrastructure.workflow;

import io.camunda.client.CamundaClient;
import lombok.RequiredArgsConstructor;
import me.mb.alps.application.port.out.PublishMessagePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class ZeebePublishMessageAdapter implements PublishMessagePort {

    private final CamundaClient camundaClient;

    @Override
    public void publish(String messageName, String correlationKey, Map<String, Object> variables) {
        camundaClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationKey)
                .variables(variables)
                .send()
                .join();
    }
}
