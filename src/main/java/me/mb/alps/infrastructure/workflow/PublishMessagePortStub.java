package me.mb.alps.infrastructure.workflow;

import me.mb.alps.application.port.out.PublishMessagePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "camunda.client.enabled", havingValue = "false")
public class PublishMessagePortStub implements PublishMessagePort {
    @Override
    public void publish(String messageName, String correlationKey, Map<String, Object> variables) {
        throw new UnsupportedOperationException("Camunda is disabled; cannot publish message.");
    }
}
