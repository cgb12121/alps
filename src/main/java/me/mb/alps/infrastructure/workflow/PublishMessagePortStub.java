package me.mb.alps.infrastructure.workflow;

import me.mb.alps.application.port.out.PublishMessagePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnMissingBean(ZeebePublishMessageAdapter.class)
@ConditionalOnBooleanProperty(name = "camunda.client.enabled")
public class PublishMessagePortStub implements PublishMessagePort {
    @Override
    public void publish(String messageName, String correlationKey, Map<String, Object> variables) {
        throw new UnsupportedOperationException("Camunda is disabled; cannot publish message.");
    }
}
